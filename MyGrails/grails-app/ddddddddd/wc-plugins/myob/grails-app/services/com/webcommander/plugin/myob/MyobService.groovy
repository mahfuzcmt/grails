package com.webcommander.plugin.myob

import com.webcommander.admin.ConfigService
import com.webcommander.admin.Customer
import com.webcommander.annotations.Initializable
import com.webcommander.constants.DomainConstants
import com.webcommander.events.AppEventManager
import com.webcommander.plugin.myob.constants.LinkComponent
import com.webcommander.plugin.myob.constants.MYOB
import com.webcommander.plugin.myob.exceptions.MyOBException
import com.webcommander.task.MultiLoggerTask
import com.webcommander.task.Task
import com.webcommander.task.TaskLogger
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Order
import com.webcommander.webcommerce.PaymentGateway
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.TaxCode
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.grails.web.json.JSONArray
import grails.web.servlet.mvc.GrailsParameterMap
import org.grails.web.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.MessageSource

@Initializable
@Transactional
class MyobService {
    @Autowired()
    @Qualifier("org.grails.plugins.web.taglib.ApplicationTagLib")
    ApplicationTagLib g;
    @Autowired()
    @Qualifier("com.webcommander.ApplicationTagLib")
    com.webcommander.ApplicationTagLib app;
    MyobSyncService myobSyncService
    ConfigService configService
    MessageSource messageSource

    static void initialize() {
        AppEventManager.on("before-product-delete", { id ->
            MyobLink.where {
                linkComponent == LinkComponent.TYPES.PRODUCT
                componentId == id
            }.deleteAll()
        })
        AppEventManager.on("before-productInventoryAdjustment-delete", { id ->
            MyobLink.where {
                linkComponent == LinkComponent.TYPES.PRODUCT_ADJUSTMENT
                componentId == id
            }.deleteAll()
        })
        AppEventManager.on("before-taxProfile-delete", { id ->
            MyobLink.where {
                linkComponent == LinkComponent.TYPES.TAX
                componentId == id
            }.deleteAll()
        })
        AppEventManager.on("before-customer-delete", { id ->
            MyobLink.where {
                linkComponent == LinkComponent.TYPES.CUSTOMER
                componentId == id
            }.deleteAll()
        })
    }

    public static Map<String, Map<String, String>> tabs = [
            tax: [
                    url: "myob/taxcode",
                    message_key: "tax",
                    endpoint: "GeneralLedger/TaxCode"
            ],
            product: [
                    url: "myob/product",
                    message_key: "product",
                    endpoint: "Inventory/Item",
                    domain: Product
            ],
            customer: [
                    url: "myob/customer",
                    message_key: "customer",
                    endpoint: "Contact/Customer",
                    domain: Customer,
            ],
            paymentAccount: [
                    url: "myob/paymentAccount",
                    message_key: "payment.account",
                    domain: PaymentGateway,
                    endpoint: "GeneralLedger/Account"
            ],
            order: [
                    url: "myob/order",
                    message_key: "order",
                    endpoint: "Sale/Invoice/Item",
                    domain: Order
            ]

    ];

    private static List<String> supportedItems = ["tax", "product", "customer", "paymentAccount", "order"];

    private MyOBClient cacheClient

    private MyOBClient getClient() {
        if (!cacheClient) {
            def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.MYOB)
            cacheClient = new MyOBClient(config.client_id, config.client_secret, config.redirect_uri, config.refresh_token)
            cacheClient.setCompanyFileProperties(config.company_file_uri, config.company_file_username, config.company_file_password)
        }
        return cacheClient
    }

    public void clearClientCache() {
        cacheClient = null;
    }

    public getAuthorizationUrl() {
        return client.authorizationUrl
    }

    public boolean processCallback(GrailsParameterMap params) {
        if (params.code) {
            def configs = []
            configs.add([type: "myob", configKey: "refresh_token", value: null]);
            configs.add([type: "myob", configKey: "auth_code", value: params.code]);
            configService.update(configs)
            clearClientCache()
            return client.setCode(params.code)
        } else {
            return false
        }
    }

    public List<JSONObject> getCompanyFiles() {
        List<JSONObject> response = []
        try {
            def result = client.performOperation(MYOB.READ)
            if (result instanceof JSONArray) {
                result.each {
                    response.add(it)
                }
            } else if(result) {
                response.add(result)
            }
        } catch (MyOBException ex) {
              log.error("Failed to get list of Company Files")
        }
        return response;
    }
    List<JSONObject> myobTaxes;
    public List<JSONObject> getTaxes() {
        if(!myobTaxes){
            myobTaxes = [];
            try {
                JSONObject result = client.performOperation(MYOB.READ, tabs.tax.endpoint)
                result.Items.each {
                    myobTaxes.add(it);
                }
            } catch (MyOBException ex) {
                log.error("Failed to get Tax List")
            }
        }

        return myobTaxes;
    }

    List<JSONObject> myobAccounts;
    public List<JSONObject> getAccounts() {
        if(!myobAccounts){
            myobAccounts = [];
            try {
                JSONObject result = client.performOperation(MYOB.READ, tabs.paymentAccount.endpoint)
                result.Items.each {
                    myobAccounts.add(it);
                }
            } catch (MyOBException ex) {
                log.error("Failed to get Account List")
            }
        }

        return myobAccounts;
    }

    public Task initImport(Map params) {
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.MYOB)
        MultiLoggerTask myobImportTask = new MultiLoggerTask(g.message(code: "myob.import"))
        myobImportTask.detail_url = app.relativeBaseUrl() + "myob/progressView";
        myobImportTask.detail_status_url = app.relativeBaseUrl() + "myob/progressStatus";
        myobImportTask.detail_viewer = "app.tabs.myob.status_viewer"
        myobImportTask.meta.operation = "import";
        myobImportTask.meta.taskItems = [];
        List<String> taskItems = [];

        Map<String, JSONArray> retrievedDataMap = [:]

        supportedItems.each { String item ->
            if (params["use_${item}"].toBoolean()) {
                taskItems.add(item);
                myobImportTask.meta.taskItems.add(item)
            }
            ["total${item.capitalize()}Count", "${item}Complete", "${item}Progress", "${item}SuccessCount", "${item}WarningCount", "${item}ErrorCount"].each { String key ->
                myobImportTask.meta.put(key, 0);
            }
        }

        taskItems.each { String item ->
            MyOBClient client = Holders.grailsApplication.mainContext.getBean(MyobService).client
            String endPoint = MyobService.tabs[item].endpoint
            List retrievedItems = [];
            if(params[item]?.items) {
                retrievedItems = JSON.parse(params[item].items)
            } else {
                Integer top = 1000;
                Integer skip = 0;
                while (true) {
                    String ep = endPoint + '/?$top=' + top + '&$skip=' + skip
                    def result = client.performOperation(MYOB.READ, ep);
                    retrievedItems.addAll(result.Items)
                    if(retrievedItems.size() >= result.Count) {
                        break;
                    }
                    skip += 1000
                }
            }
            myobImportTask.totalRecord += retrievedItems.size();
            myobImportTask.meta."total${item.capitalize()}Count" = retrievedItems.size();
            retrievedDataMap[item] = retrievedItems
        }
        if (myobImportTask.totalRecord <= 0) {
            throw new ApplicationRuntimeException("no.item.found.to.import")
        }

        myobImportTask.onError { Throwable t ->
            if(myobImportTask.meta.task_cache_location) {
                myobImportTask.serialize(new File(myobImportTask.meta.task_cache_location))
            }
            dumpImportStatus(myobImportTask);
            log.error("MYOB Import Exception Occurred", t);
            Thread.sleep(5000);
        }

        myobImportTask.onComplete {
            if(myobImportTask.meta.task_cache_location) {
                myobImportTask.serialize(new File(myobImportTask.meta.task_cache_location))
            }
            dumpImportStatus(myobImportTask);
            Thread.sleep(5000);
        }

        myobImportTask.async {

            MyobSyncService myobSyncService = Holders.grailsApplication.mainContext.getBean(MyobSyncService)
            retrievedDataMap.each { dataEntry ->
                String keyName = dataEntry.key.capitalize()
                if (myobSyncService.respondsTo("import${keyName}", [config, dataEntry.value, myobImportTask] as Object[])) {
                    "$dataEntry.key" {
                        myobSyncService."import${keyName}"(config, dataEntry.value, myobImportTask)
                    }
                }
            }
        }

        return myobImportTask
    }

    public List<Order> getExportableOrders() {
        return Order.where {
            ne("orderStatus", DomainConstants.ORDER_STATUS.CANCELLED)
            eq("paymentStatus", DomainConstants.ORDER_PAYMENT_STATUS.PAID)
            def o = Order
            notExists MyobLink.where {
                def l = MyobLink
                eqProperty("l.componentId", "o.id")
                eq("l.linkComponent", LinkComponent.TYPES.ORDER)
            }.id()
        }.list()
    }

    public List<Customer> getExportableCustomers() {
        def updateCustomer = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.MYOB, "update_customer")?.toBoolean()
        return Customer.where {
            if(!updateCustomer) {
                eq("isInTrash", false)
                def c = Customer
                notExists MyobLink.where {
                    def l = MyobLink
                    eqProperty("l.componentId", "c.id")
                    eq("l.linkComponent", LinkComponent.TYPES.CUSTOMER)
                }.id()
            }
        }.list()
    }

    public Task initExport(Map params) {
        Map<String, List> resultMap = [:];
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.MYOB)
        MultiLoggerTask myobExportTask = new MultiLoggerTask(g.message(code: "myob.export"))
        myobExportTask.detail_url = app.relativeBaseUrl() + "myob/progressView";
        myobExportTask.detail_status_url = app.relativeBaseUrl() + "myob/progressStatus";
        myobExportTask.detail_viewer = "app.tabs.myob.status_viewer"
        myobExportTask.meta.operation = "export"
        myobExportTask.meta.taskItems = []

        List<String> taskItems = [];
        supportedItems.each { String item ->
            if (item != "tax" && params["use_${item}"].toBoolean()) {
                taskItems.add(item);
                myobExportTask.meta.taskItems.add(item)
            }
            ["total${item.capitalize()}Count", "${item}Complete", "${item}Progress", "${item}SuccessCount", "${item}WarningCount", "${item}ErrorCount"].each { String key ->
                myobExportTask.meta.put(key, 0);
            }
        }

        taskItems.each { String item ->
            String keyName = item.capitalize();
            List results
            if(this.respondsTo("getExportable${keyName}s")) {
                results = this."getExportable${keyName}s"()
            } else {
                results = tabs[item].domain.list()
            }
            myobExportTask.totalRecord += results.size();
            myobExportTask.meta."total${item.capitalize()}Count" = results.size();
            resultMap[item] = results
        }

        if(myobExportTask.totalRecord <= 0) {
            throw new ApplicationRuntimeException("no.item.found.to.export")
        }

        myobExportTask.onError { Throwable t ->
            myobExportTask.serialize(new File(myobExportTask.meta.task_cache_location))
            dumpImportStatus(myobExportTask);
            log.error("MYOB Export Exception Occurred", t);
            Thread.sleep(5000);
        }

        myobExportTask.onComplete {
            myobExportTask.serialize(new File(myobExportTask.meta.task_cache_location))
            dumpImportStatus(myobExportTask);
            Thread.sleep(5000);
        }

        myobExportTask.async {
            MyobLink.withNewSession {
                MyobService myobService = Holders.grailsApplication.mainContext.getBean(MyobService)

                MyobSyncService myobSyncService = Holders.grailsApplication.mainContext.getBean(MyobSyncService)
                MyOBClient client = Holders.grailsApplication.mainContext.getBean(MyobService).client
                resultMap.each { dataEntry ->
                    String keyName = dataEntry.key.capitalize();
                    if (myobSyncService.respondsTo("export${keyName}", [config, dataEntry.value, client, myobExportTask] as Object[])) {
                        "$dataEntry.key" {
                            myobSyncService."export${keyName}"(config, dataEntry.value, client, myobExportTask);
                        }
                    }
                }
            }
        }
        return myobExportTask
    }

    public int countTotalSuccess(Task task) {
        int count = 0;
        MyobService.tabs.keySet().each { item ->
            if ((task.meta.operation == "import" && item != "order") || (task.meta.operation == "export" && item != "tax")) {
                count += task.meta."${item}SuccessCount"
            }
        }
        return count
    }

    public int countTotalWarning(Task task) {
        int count = 0;
        MyobService.tabs.keySet().each { item ->
            if ((task.meta.operation == "import" && item != "order") || (task.meta.operation == "export" && item != "tax")) {
                count += task.meta."${item}WarningCount"
            }
        }
        return count
    }

    public int countTotalError(Task task) {
        int count = 0;
        MyobService.tabs.keySet().each { item ->
            if ((task.meta.operation == "import" && item != "order") || (task.meta.operation == "export" && item != "tax")) {
                count += task.meta."${item}ErrorCount"
            }
        }
        return count
    }

    public Map getLogs(Task task, String type = null) {
        MultiLoggerTask multiLoggerTask = (MultiLoggerTask) task
        if(!multiLoggerTask) {
            return [:]
        }
        Map<String, List<Map>> importLogs = [:]
        TaskLogger customerLogger = multiLoggerTask.getLogger("customer")
        if(customerLogger) {
            importLogs.customer = [];
            List customerLogs = type ? customerLogger.logVsType[type] : customerLogger.logs;
            for(TaskLogger.Log log : customerLogs) {
                importLogs.customer.add(log)
            }
        }
        TaskLogger productLogger = multiLoggerTask.getLogger("product")
        if(productLogger) {
            importLogs.product = [];
            List productLogs = type ? productLogger.logVsType[type] : productLogger.logs
            for(TaskLogger.Log log : productLogs) {
                importLogs.product.add(log)
            }
        }
        TaskLogger taxLogger = multiLoggerTask.getLogger("tax")
        if(taxLogger) {
            importLogs.tax = [];
            List taxLogs = type ? taxLogger.logVsType[type] : taxLogger.logs
            for(TaskLogger.Log log : taxLogs) {
                importLogs.tax.add(log)
            }
        }
        TaskLogger orderLogger = multiLoggerTask.getLogger("order")
        if(orderLogger) {
            importLogs.order = [];
            List orderLogs = type ? orderLogger.logVsType[type] : orderLogger.logs
            for(TaskLogger.Log log : orderLogs) {
                importLogs.order.add(log)
            }
        }
        return importLogs
    }

    private void addLogHeaderRow(XSSFSheet sheet, Map header) {
        Row row = sheet.createRow(0)
        Cell cell = row.createCell(0)
        cell.setCellValue(header.type)
        cell = row.createCell(1)
        cell.setCellValue(header.logFor)
        cell = row.createCell(2)
        cell.setCellValue(header.msg)
    }

    private void addLogRow(XSSFSheet sheet, TaskLogger.Log log, int rowCount) {
        Row row = sheet.createRow(rowCount)
        Cell cell = row.createCell(0)
        cell.setCellValue(log.type)
        cell = row.createCell(1)
        cell.setCellValue(log.logFor)
        cell = row.createCell(2)
        cell.setCellValue(messageSource.getMessage(log.msg, log.args ?: [] as Object[], log.msg, Locale.getDefault()))
    }

    public synchronized void dumpImportStatus(MultiLoggerTask multiLoggerTask) {
        try {
            String path = Holders.servletContext.getRealPath("pub/myob-import-export")
            File file = new File(path + "/" + multiLoggerTask.meta.logger_dump_file)
            XSSFWorkbook workbook;
            if(file.exists()) {
                file.withInputStream { istream ->
                    workbook = new XSSFWorkbook(istream)
                }
            } else {
                workbook = new XSSFWorkbook()
            }
            Map header = [type: "Stauts", logFor: "Name", msg: "Remark"]
            Map logs = getLogs(multiLoggerTask)
            logs.each {
                XSSFSheet sheet
                int startRowCount = 0
                if(file.exists()) {
                    sheet = workbook.getSheet(it.key)
                    startRowCount = sheet.getLastRowNum() - 1
                }
                if(!sheet) {
                    sheet = workbook.createSheet(it.key)
                    addLogHeaderRow(sheet, header)
                }
                it.value.eachWithIndex { TaskLogger.Log log, int i ->
                    addLogRow(sheet, log, startRowCount + i + 1)
                }
            }
            File dir = new File(path)
            if(!dir.exists()) {
                dir.mkdirs()
            }
            if(!file.exists()) {
                file.createNewFile();
            }
            file.withOutputStream { stream ->
                workbook.write(stream)
            }
        } catch(Throwable e) {
            log.warn("Import Log Dump Error", e)
        }
    }

    def collectCustomers(start) {
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.MYOB)
        boolean updateCustomer = config.update_customer.toBoolean();
        HashMap data = new HashMap();
        def customers = [];
        data.put("customers", customers);
        List<Customer> toExport
        int total
        if(updateCustomer) {
            toExport = Customer.createCriteria().list(offset: start, max: 100) {
                eq "isInTrash", false
            }
            total = Customer.createCriteria().count {
                eq "isInTrash", false
            }
        } else {
            toExport = Customer.executeQuery("from Customer c where c.isInTrash = false and c.id not in (select l.componentId from MyobLink l where l.linkComponent = '$LinkComponent.TYPES.CUSTOMER')", [offset: start, max: 100])
            total = Customer.executeQuery("select count(c) from Customer c where c.isInTrash = false and c.id not in (select l.componentId from MyobLink l where l.linkComponent = '$LinkComponent.TYPES.CUSTOMER')")[0]
        }
        data.put("total", total)
        toExport.each { exportee ->
            def customer = new HashMap();
            customer.put("CMSID", exportee.id);
            String mySideId = MyobLink.createCriteria().get {
                projections {
                    property "uid"
                }
                eq "linkComponent", LinkComponent.TYPES.CUSTOMER
                eq "componentId", exportee.id
            }
            if(mySideId && updateCustomer) {
                customer.put("MYOBID", mySideId.substring(9).toInteger());
            }
            customer.put("FIRSTNAME", exportee.firstName);
            customer.put("ISINDIVIDUAL", exportee.isCompany ? "N" : "Y");
            if (exportee.isCompany) {
                customer.put("LASTNAME", exportee.fullName());
            } else {
                customer.put("LASTNAME", exportee.lastName ?: exportee.firstName);
                if (!exportee.lastName) {
                    customer.put("FIRSTNAME", null);
                }
            }
            customer.put("ISINACTIVE", exportee.status == "A" ? "N" : "Y");
            customer.put("ABN", exportee.abn ?: "");
            customer.put("ABNBRANCH", exportee.abnBranch ?: "");

            def groups = "";
            exportee.groups.each { gr ->
                groups += "," + gr.name;
            }
            if (groups.length() > 0) {
                groups = groups.substring(1);
            }
            customer.put("CUSTOMERGROUP", groups);

            def addresses = [];
            customer.put("ADDRESS", addresses);
            [exportee.activeBillingAddress, exportee.activeShippingAddress, exportee.address].eachWithIndex { cmsAddress, index ->
                def address = new HashMap();
                address.put("STREET", cmsAddress.addressLine1 + (cmsAddress.addressLine2 ? "\n" + cmsAddress.addressLine2 : ""));
                address.put("CITY", cmsAddress.city ?: "");
                address.put("POSTCODE", cmsAddress.postCode ?: "");
                address.put("COUNTRY", cmsAddress.country?.name ?: "");
                address.put("STATE", cmsAddress.state?.name ?: "");
                address.put("PHONE", cmsAddress.phone ?: "");
                address.put("MOBILE", cmsAddress.mobile ?: "");
                address.put("FAX", cmsAddress.fax ?: "");
                address.put("EMAIL", cmsAddress.email ?: "");
                address.put("LOCATION", index + 1);
                addresses.push(address);
            }

            customers.push(customer);
        }
        return data;
    }

    def collectOrders(start) {
        HashMap data = new HashMap();
        def orders = [];
        data.put("orders", orders);
        List<Order> toExport = Order.executeQuery("from Order c where c.shipping is not null and c.orderStatus != '$DomainConstants.ORDER_STATUS.CANCELLED' and c.paymentStatus = '$DomainConstants.ORDER_PAYMENT_STATUS.PAID' and c.id not in (select l.componentId from MyobLink l where l.linkComponent = '$LinkComponent.TYPES.ORDER') and (c.customerId is null or exists (select m.id from MyobLink m where m.linkComponent = '$LinkComponent.TYPES.CUSTOMER' and m.componentId = c.customerId))", [offset: start, max: 100])

        int total = Order.executeQuery("select count(c) from Order c where c.shipping is not null and c.orderStatus != '$DomainConstants.ORDER_STATUS.CANCELLED' and c.paymentStatus = '$DomainConstants.ORDER_PAYMENT_STATUS.PAID' and c.id not in (select l.componentId from MyobLink l where l.linkComponent = '$LinkComponent.TYPES.ORDER') and (c.customerId is null or exists (select m.id from MyobLink m where m.linkComponent = '$LinkComponent.TYPES.CUSTOMER' and m.componentId = c.customerId))")[0]
        data.put("total", total)
        toExport.each { exportee ->
            def order = new HashMap();
            if (exportee.customerId) {
                order.put("CUSTOMERID", MyobLink.createCriteria().get {
                    projections {
                        property "uid"
                    }
                    eq "linkComponent", LinkComponent.TYPES.CUSTOMER
                    eq "componentId", exportee.customerId
                }.substring(9).toInteger());
            }
            order.put("CMSID", exportee.id);
            order.put("PURCHASEDATE", exportee.created);
            order.put("TOTALAMOUNT", exportee.grandTotal)
            def items = new ArrayList();
            order.put("ITEMS", items);
            order.put("TOTALPAIDAMOUNT", exportee.paid);
            def shippingAddress = exportee.shipping;
            if (!shippingAddress) {
                return;
            }

            String shipCode
            Double shipRate
            if(exportee.shippingCost) {
                shipRate = 100 * exportee.shippingTax / exportee.shippingCost
                shipCode = TaxCode.createCriteria().get {
                    projections {
                        property "name"
                    }
                    maxResults 1
                    gt "rate", shipRate - 0.01
                    lt "rate", shipRate + 0.01
                };

                order.put("SHIPPINGCOST", exportee.shippingCost + exportee.shippingTax);
            } else {
                order.put("SHIPPINGCOST", 0);
            }

            order.put("SHIPPINGTAXCODE", shipCode ?: "FRE");

            exportee.items.each { detail ->
                def item = new HashMap();
                String psku = MyobLink.createCriteria().get {
                    projections {
                        property "uid"
                    }
                    eq "linkComponent", LinkComponent.TYPES.PRODUCT
                    eq "componentId", detail.productId
                }?.substring(8) ?: ""
                item.put("ITEMNUMBER", psku);
                item.put("QUANTITY", detail.quantity);
                Double taxRate = 0.0;
                if(detail.price && detail.quantity) {
                    taxRate = 100 * detail.tax / (detail.price * detail.quantity)
                }
                String taxCode = TaxCode.createCriteria().get {
                    projections {
                        property "name"
                    }
                    maxResults 1
                    gt "rate", taxRate - 0.01
                    lt "rate", taxRate + 0.01
                };
                Double detailtTotal =  detail.price * detail.quantity + detail.tax
                def discountPercent = 0.0;
                if(detailtTotal) {
                    discountPercent = (100 * detail.discount / detailtTotal).toFixed(2);
                }
                item.put("DISCOUNT", discountPercent.toDouble());
                item.put("PRICE", detail.price + detail.tax / detail.quantity);
                item.put("TOTAL", detailtTotal);
                item.put("TAXCODE", taxCode ?: "FRE");
                item.put("ExTaxPrice", detail.price);
                item.put("ExTaxTotal", detail.price * detail.quantity);
                if ("GST".equalsIgnoreCase(taxCode)) {
                    item.put("GSTAmount", detail.tax);
                } else if ("LCT".equalsIgnoreCase(taxCode)) {
                    item.put("LCTAmount", detail.tax);
                } else {
                    item.put("NonGSTLCTAmount", detail.tax);
                }

                Double freightAmount = 0
                Double freightTaxAmount = 0
                if(exportee.shippingCost && exportee.total) {
                    freightAmount = exportee.shippingCost
                    freightTaxAmount = exportee.shippingTax
                }
                String freightTaxCode = shipCode ?: "FRE";
                item.put("FreightTaxCode", freightTaxCode);
                item.put("FreightIncTaxAmount", freightAmount + freightTaxAmount);
                if ("GST".equalsIgnoreCase(freightTaxCode)) {
                    item.put("FreightGSTAmount", freightTaxAmount);
                } else if ("LCT".equalsIgnoreCase(freightTaxCode)) {
                    item.put("FreightLCTAmount", freightTaxAmount);
                } else {
                    item.put("FreightNonGSTLCTAmount", freightTaxAmount);
                }
                item.put("FreightExTaxAmount", freightAmount);

                items.add(item);
            }
            if (order != null) {
                orders.push(order);
            }
        }
        return data;
    }

    def updateCustomerLink(Map ids) {
        ids.each {
            new MyobLink(componentId: it.key, linkComponent: LinkComponent.TYPES.CUSTOMER, myobVersion: 'N/A', uid: "CUSTOMER-" + it.value).save()
        }
    }

    def updateOrderLink(Map ids) {
        ids.each {
            new MyobLink(componentId: it.key, linkComponent: LinkComponent.TYPES.ORDER, myobVersion: 'N/A', uid: "ORDER-" + it.value).save()
        }
    }
}
