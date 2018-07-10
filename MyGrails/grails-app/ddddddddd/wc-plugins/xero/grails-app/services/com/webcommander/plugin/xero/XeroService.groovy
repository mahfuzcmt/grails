package com.webcommander.plugin.xero

import com.webcommander.admin.Customer
import com.webcommander.annotations.Initializable
import com.webcommander.constants.DomainConstants
import com.webcommander.events.AppEventManager
import com.webcommander.plugin.xero.constants.LinkComponent
import com.webcommander.task.MultiLoggerTask
import com.webcommander.task.Task
import com.webcommander.task.TaskLogger
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Order
import com.webcommander.webcommerce.PaymentGateway
import com.webcommander.webcommerce.Product
import grails.util.Holders
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.grails.web.json.JSONArray
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.MessageSource
/**
 * Created by sajed on 6/12/2014.
 */

@Initializable
class XeroService {
    @Autowired
    @Qualifier("org.grails.plugins.web.taglib.ApplicationTagLib")
    ApplicationTagLib g;
    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    com.webcommander.ApplicationTagLib app;
    XeroSyncService xeroSyncService
    XeroUtilService xeroUtilService
    MessageSource messageSource

    static void initialize() {
        AppEventManager.on("before-product-delete", { id ->
            XeroTrack.where {
                linkComponent == LinkComponent.TYPES.PRODUCT
                componentId == id
            }.deleteAll()
        })
        AppEventManager.on("before-productInventoryAdjustment-delete", { id ->
            XeroTrack.where {
                linkComponent == LinkComponent.TYPES.PRODUCT_ADJUSTMENT
                componentId == id
            }.deleteAll()
        })
        AppEventManager.on("before-taxProfile-delete", { id ->
            XeroTrack.where {
                linkComponent == LinkComponent.TYPES.TAX
                componentId == id
            }.deleteAll()
        })
        AppEventManager.on("before-customer-delete", { id ->
            XeroTrack.where {
                linkComponent == LinkComponent.TYPES.CUSTOMER
                componentId == id
            }.deleteAll()
        })
    }

    public static Map<String, Map<String, String>> tabs = [
            tax: [
                    url: "xero/taxcode",
                    message_key: "tax",
                    data_method: "getTaxRates"
            ],
            product: [
                    url: "xero/product",
                    message_key: "product",
                    domain: Product,
                    data_method: "getItems"
            ],
            customer: [
                    url: "xero/customer",
                    message_key: "customer",
                    domain: Customer,
                    data_method: "getContacts"
            ],
            paymentAccount: [
                    url: "xero/paymentAccount",
                    message_key: "payment.account",
                    domain: PaymentGateway,
                    data_method: ""
            ],
            order: [
                    url: "xero/order",
                    message_key: "order",
                    domain: Order,
                    data_method: ""
            ]

    ];

    private static List<String> supportedItems = ["tax", "product", "customer", "paymentAccount", "order"];

    public Task initExport(Map params) {
        Map<String, List> resultMap = [:];
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.XERO)
        MultiLoggerTask xeroExportTask = new MultiLoggerTask(g.message(code: "xero.export"))
        xeroExportTask.detail_url = app.relativeBaseUrl() + "xero/progressView";
        xeroExportTask.detail_status_url = app.relativeBaseUrl() + "xero/progressStatus";
        xeroExportTask.detail_viewer = "app.tabs.xero.status_viewer"
        xeroExportTask.meta.operation = "export"
        xeroExportTask.meta.taskItems = []

        List<String> taskItems = [];
        supportedItems.each { String item ->
            if (item != "tax" && params["use_${item}"].toBoolean()) {
                taskItems.add(item);
                xeroExportTask.meta.taskItems.add(item)
            }
            ["total${item.capitalize()}Count", "${item}Complete", "${item}Progress", "${item}SuccessCount", "${item}WarningCount", "${item}ErrorCount"].each { String key ->
                xeroExportTask.meta.put(key, 0);
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
            xeroExportTask.totalRecord += results.size();
            xeroExportTask.meta."total${item.capitalize()}Count" = results.size();
            resultMap[item] = results
        }

        if(xeroExportTask.totalRecord <= 0) {
            throw new ApplicationRuntimeException("no.item.found.to.export")
        }

        xeroExportTask.onError { Throwable t ->
            xeroExportTask.serialize(new File(xeroExportTask.meta.task_cache_location))
            dumpImportStatus(xeroExportTask);
            log.error("XERO Export Exception Occurred", t);
            Thread.sleep(5000);
        }

        xeroExportTask.onComplete {
            xeroExportTask.serialize(new File(xeroExportTask.meta.task_cache_location))
            dumpImportStatus(xeroExportTask);
            Thread.sleep(5000);
        }

        xeroExportTask.async {
            XeroTrack.withNewSession {
                XeroSyncService xeroSyncService = Holders.grailsApplication.mainContext.getBean(XeroSyncService)
                XeroClient client = new XeroClient(config["consumer_key"], config["consumer_secret"], config["private_key"]);
                resultMap.each { dataEntry ->
                    String keyName = dataEntry.key.capitalize();
                    if (xeroSyncService.respondsTo("export${keyName}", [config, dataEntry.value, client, xeroExportTask] as Object[])) {
                        "$dataEntry.key" {
                            xeroSyncService."export${keyName}"(config, dataEntry.value, client, xeroExportTask);
                        }
                    }
                }
            }
        }
        return xeroExportTask
    }

    public Task initImport(Map params) {
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.XERO)
        MultiLoggerTask xeroImportTask = new MultiLoggerTask(g.message(code: "xero.import"))
        xeroImportTask.detail_url = app.relativeBaseUrl() + "xero/progressView";
        xeroImportTask.detail_status_url = app.relativeBaseUrl() + "xero/progressStatus";
        xeroImportTask.detail_viewer = "app.tabs.xero.status_viewer"
        xeroImportTask.meta.operation = "import";
        XeroClient client = new XeroClient(config.consumer_key, config.consumer_secret, config.private_key)
        xeroImportTask.meta.taskItems = [];
        List<String> taskItems = [];
        Map<String, JSONArray> retrievedDataMap = [:]
        supportedItems.each { String item ->
            if (params["use_${item}"].toBoolean()) {
                taskItems.add(item);
                xeroImportTask.meta.taskItems.add(item)
            }
            ["total${item.capitalize()}Count", "${item}Complete", "${item}Progress", "${item}SuccessCount", "${item}WarningCount", "${item}ErrorCount"].each { String key ->
                xeroImportTask.meta.put(key, 0);
            }
        }

        taskItems.each { String item ->
            String dataMethod = XeroService.tabs[item].data_method
            List retrievedItems = [];
            def result = client."${dataMethod}"();
            result? retrievedItems.addAll(result): [];
            xeroImportTask.totalRecord += retrievedItems.size();
            xeroImportTask.meta."total${item.capitalize()}Count" = retrievedItems.size();
            retrievedDataMap[item] = retrievedItems
        }

        if (xeroImportTask.totalRecord <= 0) {
            throw new ApplicationRuntimeException("no.item.found.to.import")
        }

        xeroImportTask.onError { Throwable t ->
            if(xeroImportTask.meta.task_cache_location) {
                xeroImportTask.serialize(new File(xeroImportTask.meta.task_cache_location))
            }
            dumpImportStatus(xeroImportTask);
            log.error("XERO Import Exception Occurred", t);
            Thread.sleep(5000);
        }

        xeroImportTask.onComplete {
            if(xeroImportTask.meta.task_cache_location) {
                xeroImportTask.serialize(new File(xeroImportTask.meta.task_cache_location))
            }
            dumpImportStatus(xeroImportTask);
            Thread.sleep(5000);
        }

        xeroImportTask.async {
            XeroSyncService xeroSyncService = Holders.grailsApplication.mainContext.getBean(XeroSyncService)
            retrievedDataMap.each { dataEntry ->
                String keyName = dataEntry.key.capitalize()
                if (xeroSyncService.respondsTo("import${keyName}", [config, dataEntry.value, xeroImportTask] as Object[])) {
                    "$dataEntry.key" {
                        xeroSyncService."import${keyName}"(config, dataEntry.value, xeroImportTask)
                    }
                }
            }
        }

        return xeroImportTask
    }

    public int countTotalSuccess(Task task) {
        int count = 0;
        XeroService.tabs.keySet().each { item ->
            if ((task.meta.operation == "import" && item != "order") || (task.meta.operation == "export" && item != "tax")) {
                count += task.meta."${item}SuccessCount"
            }
        }
        return count
    }

    public int countTotalWarning(Task task) {
        int count = 0;
        XeroService.tabs.keySet().each { item ->
            if ((task.meta.operation == "import" && item != "order") || (task.meta.operation == "export" && item != "tax")) {
                count += task.meta."${item}WarningCount"
            }
        }
        return count
    }

    public int countTotalError(Task task) {
        int count = 0;
        XeroService.tabs.keySet().each { item ->
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
            String path = Holders.servletContext.getRealPath("pub/xero-import-export")
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


    public List<Order> getExportableOrders() {
        return Order.where {
            ne("orderStatus", DomainConstants.ORDER_STATUS.CANCELLED)
            eq("paymentStatus", DomainConstants.ORDER_PAYMENT_STATUS.PAID)
            def o = Order
            notExists XeroTrack.where {
                def l = XeroTrack
                eqProperty("l.componentId", "o.id")
                eq("l.linkComponent", LinkComponent.TYPES.ORDER)
            }.id()
        }.list()
    }

    public List<Customer> getExportableCustomers() {
        return Customer.where {
            eq("isInTrash", false)
            if(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.XERO, "update_customer")) {
                def c = Customer
                notExists XeroTrack.where {
                    def l = XeroTrack
                    eqProperty("l.componentId", "c.id")
                    eq("l.linkComponent", LinkComponent.TYPES.CUSTOMER)
                }.id()
            }
        }.list()
    }

}
