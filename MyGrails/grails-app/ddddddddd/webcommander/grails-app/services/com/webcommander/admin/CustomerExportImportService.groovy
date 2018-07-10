package com.webcommander.admin

import com.webcommander.ApplicationTagLib
import com.webcommander.constants.NamedConstants
import com.webcommander.item.CustomerData
import com.webcommander.task.MultiLoggerTask
import com.webcommander.task.Task
import com.webcommander.task.TaskLogger
import com.webcommander.task.TaskService
import com.webcommander.webcommerce.Address
import com.webcommander.webcommerce.CommonImportService
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.apache.commons.lang.StringUtils
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.grails.plugins.web.taglib.RenderTagLib
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.MessageSource
import org.springframework.web.multipart.MultipartFile
import org.supercsv.io.CsvMapReader
import org.supercsv.prefs.CsvPreference

@Transactional
class CustomerExportImportService {
    CustomerService customerService
    @Autowired
    @Qualifier("org.grails.plugins.web.taglib.RenderTagLib")
    RenderTagLib g;
    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    ApplicationTagLib app
    CommonImportService commonImportService
    TaskService taskService
    MessageSource messageSource

    private void addHeaderRow(XSSFSheet sheet, Map header) {
        Row row = sheet.createRow(0)
        header.eachWithIndex { def entry, int i ->
            Cell cell = row.createCell(i)
            cell.setCellValue(entry.value)
        }
    }

    private void addRow(XSSFSheet sheet, Map header, Customer customer, int rowCount) {
        Row row = sheet.createRow(rowCount)
        header.eachWithIndex { def entry, int i ->
            Cell cell = row.createCell(i)
            def value = "";
            switch (entry.key) {
                case "customerType":
                    value = customer.isCompany ? "company" : "individual";
                    break;
                case "addressLine1":
                    value = customer.address.addressLine1
                    break;
                case "addressLine2":
                    value = customer.address.addressLine2 ? customer.address.addressLine2 : ""
                    break
                case "country":
                    value = customer.address.country.name
                    break;
                case "state":
                    value = customer.address.state?.name ?: ""
                    break;
                case "groups":
                    value = customer.groups.name.join(",")
                    break
                case "city":
                case "phone":
                case "mobile":
                case "fax":
                case "postCode":
                    value = customer.address[entry.key] ?: "";
                    break;
                default:
                    value = customer[entry.key] ?: "";
            }
            cell.setCellValue(value);
        }
    }

    def export(Map params, OutputStream stream) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet customerSheet = workbook.createSheet("Customer");
        Map header = [:];
        NamedConstants.CUSTOMER_EXPORT_IMPORT_FIELDS.each {
            if (params[it.key].toBoolean()) {
                header[it.key] = g.message(code: NamedConstants.CUSTOMER_EXPORT_IMPORT_FIELDS[it.key]);
            }
        }
        addHeaderRow(customerSheet, header);
        Map filterMap = [:];
        if(params.type == "active") {
            filterMap.status = 'A';
        } else if(params.type == "filter" && params.filter) {
            filterMap = JSON.parse(params.filter)
        }
        filterMap.max = "-1"
        filterMap.offset = "0"
        List customers = customerService.getCustomers(filterMap);
        Integer rowCount = 1;
        for (Customer customer : customers) {
            addRow(customerSheet, header, customer, rowCount++);
        }
        workbook.write(stream)
        stream.close()
    }

    /**********************IMPORT***********************/

    Map getHeaderMapping(def reader) {
        Map mapping = [:];
        NamedConstants.CUSTOMER_IMPORT_FIELD_MAPPING.values().each {
            mapping[it] = -1
        }
        if(reader instanceof Workbook) {
            reader = (Workbook) reader;
            Sheet sheet = reader.getSheetAt(0);
            Row header = sheet.getAt(0);
            Iterator<Cell> iterator = header.iterator();
            Integer i = 0;
            while (iterator.hasNext()) {
                String value = iterator.next().getStringCellValue().trim();
                if (mapping.containsKey(value)) {
                    mapping[value] = i
                }
                i++;
            }
        }
        return mapping;
    }

    List getCustomerDataList(def reader, Map fieldMapping) {
        List<CustomerData> customers = [];
        if (reader instanceof Workbook) {
            Sheet sheet = reader.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.rowIterator();
            Integer i = 0;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if(i == 0) {
                    i++;
                    continue;
                }
                CustomerData data = new CustomerData();
                NamedConstants.CUSTOMER_IMPORT_FIELD_MAPPING.each {
                    Integer index = fieldMapping[it.value]
                    data."$it.key" = index != -1 ? commonImportService.getCellValue(row, index) : null
                }
                customers.add(data)
                i++;
            }
        } else {
            reader = (CsvMapReader) reader
            String[] headers = reader.getHeader(true);
            Map<String, String> row = null
            while ((row = reader.read(headers)) != null) {
                CustomerData data = new CustomerData();
                Map filteredRow = [:];
                row.each {
                    filteredRow[it.key.trim()] = it.value;
                }
                NamedConstants.CUSTOMER_IMPORT_FIELD_MAPPING.each {
                    data."$it.key" = filteredRow[it.value];
                }
                customers.add(data);
            }
        }

        return customers;
    }

    def saveCustomerAddress(CustomerData data, Address address, MultiLoggerTask task) {
        address.firstName = data.firstName
        address.lastName = data.lastName
        if (!data.addressLine1) {
            throw new Exception("customer.address.not.found");
        }
        address.addressLine1 = data.addressLine1
        if(data.addressLine2){
            address.addressLine2 = data.addressLine2
        }
        address.phone = data.phone
        address.mobile = data.mobile
        address.fax = data.fax
        address.email = data.userName
        Country country = Country.findByNameOrCode(data.country, data.country);
        if (!country) {
            throw new Exception("country.not.found");
        }
        address.country = country;
        State state = State.findByNameAndCountry(data.state, country);
        address.state = state;
        if(state && (data.city || data.postCode)) {
            City city =  City.find {
                if(data.city) {
                    eq "name", data.city
                }
                if(data.postCode) {
                    eq "postCode", data.postCode
                }
                eq "state.id", state.id
            }
            if(!city) {
                task.taskLogger.warning(data.firstName + data.lastName  + " "+ data.city + ": " +  data.postCode, "city.postcode.not.match");
                task.meta.warningCount++
                data.postCode = null
                data.city = null
            } else {
                address.city = city.name
                address.postCode = city.postCode
            }
        } else {
            address.city = data.city
            address.postCode = data.postCode
        }

        if(address.id) {
            address.merge()
        } else {
            address.save();
        }
        return address;
    }

    def saveAll(Task task, List<CustomerData> dataList) {
        dataList.each {
            Customer.withNewTransaction {status ->
                try {
                    if (!it.userName) {
                        throw new Exception("email.not.found")
                    }
                    Customer customer = Customer.findByUserName(it.userName)
                    Address address
                    if (!customer) {
                        customer = new Customer()
                        address = new Address();
                    } else {
                        address = new Address();
                    }
                    if (!it.firstName) {
                        throw new Exception("first.name.not.found")
                    }
                    customer.firstName = it.firstName
                    customer.lastName = it.lastName
                    customer.userName = it.userName
                    customer.sex = it.sex
                    customer.companyName = it.companyName;
                    if (it.abn) {
                        if((!(it.abn ==~ /^[0-9 ]+$/) || it.abn.length() > 14) || it.abn.length() < 10) {
                            task.taskLogger.warning(customer.fullName, "invalid.abn");
                            task.meta.warningCount++
                        } else {
                            String formattedABN = new StringBuilder((it.abn).replaceAll("\\s+","")).insert(2, " ").insert(6, " ").insert(10, " ").toString();
                            customer.abn = formattedABN;
                        }
                    }

                    if(it.abnBranch && (!StringUtils.isNumeric(it.abnBranch) || it.abnBranch.length() > 3)) {
                        task.taskLogger.warning(customer.fullName, "invalid.abn.branch");
                        task.meta.warningCount++
                    } else {
                        customer.abnBranch = it.abnBranch
                    }
                    if(it.password) {
                        customer.password = it.password.encodeAsMD5();
                    } else if (!customer.id) {
                        customer.password = "change it".encodeAsMD5();
                    }
                    if(it.storeCredit) {
                        customer.storeCredit = it.storeCredit.toDouble();
                    }
                    if(!it.status) {
                        task.taskLogger.warning(customer.firstName + (customer.lastName ? " " + customer.lastName : ""), "customer.status.not.found");
                        task.meta.warningCount++
                        customer.status = 'A';
                    } else {
                        customer.status = it.status;
                    }
                    if (!it.customerType) {
                        task.taskLogger.warning(customer.firstName + (customer.lastName ? " " + customer.lastName : ""), "customer.type.not.found");
                        task.meta.warningCount ++;
                    } else if(it.customerType.toLowerCase() == 'company') {
                        customer.isCompany = true
                    } else {
                        customer.isCompany = false
                    }
                    List<CustomerGroup> groups= []
                    if (customer.id) {
                        customer.address = saveCustomerAddress(it, address, task);
                        if (customer.isInTrash) {
                            task.taskLogger.warning(customer.firstName + (customer.lastName ? " " + customer.lastName : ""), "this.customer.exists.in.trash");
                            task.meta.warningCount ++;
                        }
                    } else {
                        customer.address = saveCustomerAddress(it, address, task)
                        customer.activeBillingAddress = saveCustomerAddress(it, new Address(), task)
                        customer.activeShippingAddress = saveCustomerAddress(it, new Address(), task)
                        customer.addToBillingAddresses(customer.activeBillingAddress)
                        customer.addToShippingAddresses(customer.activeShippingAddress)
                    }
                    it.groups?.split(",").each {
                        String groupName = it.trim();
                        CustomerGroup group = CustomerGroup.findByName(groupName);
                        if(group == null) {
                            group = new CustomerGroup(name: groupName)
                            if(group.validate()) {
                                group.save()
                            } else {
                                task.taskLogger.warning(customer.firstName + (customer.lastName ? " " + customer.lastName : ""), "invalid.customer.group.name", [groupName]);
                                task.meta.warningCount ++;
                                group = null
                            }
                        }
                        if(group != null) {
                            groups.add(group)
                        }
                    }

                    List groupIds = customer.groups?.id ?: [0l]
                    CustomerGroup.createCriteria().list {
                        inList("id", groupIds)
                    }.each {
                        it.removeFromCustomers(customer)
                    }
                    groups.each {
                        it.addToCustomers(customer)
                    }
                    customer.save(flush: true)
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.success(customer.fullName, "customer.import.success")
                    task.meta.successCount++

                } catch (Exception e) {
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    String name = it.firstName ? it.firstName : (it.userName?: "customer");
                    task.taskLogger.error(name, e.message)
                    task.meta.errorCount++;
                    status.setRollbackOnly();
                }
            }

        }
    }

    def initImport(MultipartFile file, String fileExtension) {
        def reader;
        if(fileExtension == "xls") {
            reader = new HSSFWorkbook(file.inputStream)
        } else if(fileExtension == "xlsx") {
            reader = new XSSFWorkbook(file.inputStream)
        } else {
            reader = new CsvMapReader(new InputStreamReader(file.inputStream), CsvPreference.EXCEL_PREFERENCE)
        }
        MultiLoggerTask task = new MultiLoggerTask("Customer Import")
        task.detail_url = app.relativeBaseUrl() + "customerExportImport/progressView";
        task.detail_status_url = app.relativeBaseUrl() + "customerExportImport/progressStatus";
        task.detail_viewer = "app.tabs.customer.import_status_viewer"
        task.meta = [
                successCount : 0,
                warningCount : 0,
                errorCount : 0
        ]
        Map fieldMapping = getHeaderMapping(reader);
        List<CustomerData> customerDataList = getCustomerDataList(reader, fieldMapping)
        task.totalRecord =  customerDataList.size();
        task.onComplete {
            task.serialize(new File(task.meta.task_cache_location));
            saveLog(task);
            Thread.sleep(1000000);
        }

        task.onError {Throwable t ->
            task.serialize(new File(task.meta.task_cache_location));
            saveLog(task);
            Thread.sleep(1000000);
        }

        task.async {
            customer {
                saveAll(task, customerDataList)
            }
        }

        return task

    }

    private void addLogRow(XSSFSheet sheet, TaskLogger.Log log, int rowCount) {
        Row row = sheet.createRow(rowCount)
        Cell cell = row.createCell(0)
        cell.setCellValue(log.type)
        cell = row.createCell(1)
        cell.setCellValue(log.logFor)
        cell = row.createCell(2)
        cell.setCellValue(messageSource.getMessage(log.msg, log.args ?: [] as Object[], log.msg, Locale.getDefault()));
    }

    private void saveLog(MultiLoggerTask task) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Map header = [type: "Status", logFor: "Name", msg: "Remark"]
        XSSFSheet sheet = workbook.createSheet("Customer Import");
        addHeaderRow(sheet, header)
        task.taskLogger.logs.eachWithIndex{ TaskLogger.Log log, int i ->
            addLogRow(sheet, log, i + 1)
        }
        try {
            String path = Holders.servletContext.getRealPath("pub/customer-import")
            File dir = new File(path)
            if(!dir.exists()) {
                dir.mkdirs()
            }
            File file = new File(path + "/Customer Import -" + task.meta.logger_dump_file + ".xlsx")
            FileOutputStream out = new FileOutputStream(file)
            workbook.write(out)
            out.close()
        } catch(Throwable e) {
            log.warn("Import Log Dump Error", e)
        }
    }
}
