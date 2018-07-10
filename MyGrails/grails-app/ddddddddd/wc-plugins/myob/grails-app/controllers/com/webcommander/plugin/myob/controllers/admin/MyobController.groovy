package com.webcommander.plugin.myob.controllers.admin

import com.webcommander.admin.ConfigService
import com.webcommander.admin.Customer
import com.webcommander.authentication.annotations.License
import com.webcommander.constants.DomainConstants
import com.webcommander.listener.SessionManager
import com.webcommander.plugin.myob.MyOBClient
import com.webcommander.plugin.myob.MyobLink
import com.webcommander.plugin.myob.MyobService
import com.webcommander.plugin.myob.constants.LinkComponent
import com.webcommander.plugin.myob.constants.MYOB
import com.webcommander.task.MultiLoggerTask
import com.webcommander.task.Task
import com.webcommander.task.TaskService
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.PaymentGateway
import com.webcommander.webcommerce.Product
import grails.converters.JSON
import grails.util.Holders
import org.grails.web.json.JSONObject


class MyobController {
    ConfigService configService
    MyobService myobService
    TaskService taskService

    @License(required = "allow_myob_feature")
    def loadAppView() {
        if(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.MYOB, "app_version") == "19.x") {
            render(view: "/plugins/myob/appView19")
        } else {
            render(view: "/plugins/myob/appView", model: [myobService: myobService])
        }
    }

    def customer(){
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.MYOB)
        render(view: "/plugins/myob/customer", model: [myobService: myobService, config: config])
    }

    def product(){
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.MYOB)
        render(view: "/plugins/myob/product", model: [myobService: myobService, config: config])
    }

    def taxcode(){
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.MYOB)
        render(view: "/plugins/myob/taxcode", model: [myobService: myobService, config: config])
    }

    def order() {
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.MYOB)
        if(!config."default_payment_account") {
            throw new ApplicationRuntimeException("default.payment.account.not.set")
        }
        def syncedProductIdList = MyobLink.findAll {
            eq "linkComponent", LinkComponent.TYPES.PRODUCT
            projections {
                property("componentId")
            }
        }
        def syncedCustomerIdList = MyobLink.findAll {
            eq "linkComponent", LinkComponent.TYPES.CUSTOMER
            projections {
                property("componentId")
            }
        }
        def linkedProductList = syncedProductIdList.size() > 0?Product.findAll {inList "id", syncedProductIdList } : []
        def linkedCustomerList = syncedCustomerIdList.size() > 0? Customer.findAll {inList "id", syncedCustomerIdList }: []
        render(view: "/plugins/myob/order", model: [myobService: myobService, config: config, linkedProductList: linkedProductList, linkedCustomerList: linkedCustomerList])
    }

    def paymentAccount() {
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.MYOB);
        try {
            List<JSONObject> accountsData = myobService.getAccounts();
            List accounts =[];
            def sortedAccounts = accountsData.findAll {
                it."Type" == "Bank" || it."Type" == "Account Receivable"
            }
            accounts += sortedAccounts.collect {
                return ["code": it.UID, name: it.DisplayID + "-" + it.Name];
            }
            def paymentGatewayList = PaymentGateway.findAll() {eq "isEnabled", true}
            def paymentMapping = config.payment_account_mapping? JSON.parse(config.payment_account_mapping) : null
            render(view: "/plugins/myob/paymentAccount", model: [config: config, accounts: accounts, paymentGatewayList: paymentGatewayList, paymentMapping: paymentMapping])
        } catch(Exception e){
            render( "Invalid myob config")
        }
    }

    def config(){
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.MYOB)
        render(view: "/plugins/myob/config", model: [config: config])
    }

    def saveConfigurations(){
        def configs = [];
        def previousConfigs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.MYOB);
        if(!params.myob) {
            params.myob = [:]
        }
        if(params.isSetting && (params.myob.client_id != previousConfigs.client_id || params.myob.client_secret != previousConfigs.client_secret)){
            configs.add([type: "myob", configKey: "refresh_token", value: null]);
            configs.add([type: "myob", configKey: "company_file_uri", value: null]);
        }

        if (params."payment_account") {
            def paymentMap = [:]
            params."payment_account".each {
                paymentMap.put(it.key, it.value)
            }
            params.myob.payment_account_mapping = paymentMap as JSON
        }

        if(params.myob && params.myob.enable_surcharge_sync == "true") {
            if(!params.myob.default_surcharge_line_product) {
                throw new ApplicationRuntimeException("surcharge.line.product.required")
            }
        }

        params.list("type").each { type ->
            List<String> booleanFields = params.list("booleans")
            booleanFields.each {
                if(!params."${type}"){
                    params."${type}" = [:]
                }
                if(!params."${type}"."${it}"){
                    params."${type}"."${it}" = "false"
                }
            }
            params."${type}".each {
                configs.add([type: type, configKey: it.key, value: it.value]);
            }
        }
        myobService.clearClientCache();
        if (configService.update(configs)) {
            render([status: "success", message: g.message(code: "setting.updated.successfully")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "setting.not.updated")] as JSON)
        }
    }

    def authorize() {
        render([url: myobService.authorizationUrl] as JSON);
    }

    def callback(){
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.MYOB)
        render(view: "/plugins/myob/callback", model: [success: myobService.processCallback(params), config: config])
    }

    @License(required = "allow_myob_feature")
    def initImport() {
        Task importTask = myobService.initImport(params)
        String token = importTask.token
        if(params.append_token) {
            Task t = Task.getInstance(new File(SessionManager.protectedTempFolder.absolutePath + "/" + params.append_token + "/myob_item_import_export.job"))
            importTask.meta.task_cache_location = t.meta.task_cache_location
            importTask.meta.logger_dump_file = t.meta.logger_dump_file
        } else {
            importTask.meta.task_cache_location = SessionManager.protectedTempFolder.absolutePath + "/" + token + "/myob_item_import_export.job"
            Date date = new Date().gmt().toZone(session.timezone)
            importTask.meta.logger_dump_file = "MYOB-import-${date.toFormattedString("yyyy-MM-dd", true, "HH_mm_ss_SSS", false, null)}.xlsx";
        }
        render([token: importTask.token, name: importTask.name] as JSON);
    }

    @License(required = "allow_myob_feature")
    def initExport() {
        Task exportTask = myobService.initExport(params)
        exportTask.meta.task_cache_location = SessionManager.protectedTempFolder.absolutePath + "/" + exportTask.token + "/myob_item_import_export.job"
        Date date = new Date().gmt().toZone(session.timezone)
        exportTask.meta.logger_dump_file = "MYOB-import-${date.toFormattedString("yyyy-MM-dd", true, "HH_mm_ss_SSS", false, null)}.xlsx";
        render ([token: exportTask.token, name: exportTask.name] as JSON);
    }

    def progressView(){
        String token = params.token;
        Task task = taskService.getByToken(token)
        render(
                view: "/plugins/myob/progressView",
                model: [
                        task: task,
                        totalSuccessCount: myobService.countTotalSuccess(task),
                        totalWarningCount: myobService.countTotalWarning(task),
                        totalErrorCount: myobService.countTotalError(task)
                ]
        )
    }

    def progressStatus(){
        String token = params.token;
        Task task = taskService.getByToken(token);
        if(task){
            Map data = [
                    token: token,
                    status: task.status,
                    totalRecord: task.totalRecord,
                    recordComplete: task.recordComplete,
                    totalProgress: task.progress,
                    totalSuccessCount: myobService.countTotalSuccess(task),
                    totalWarningCount: myobService.countTotalWarning(task),
                    totalErrorCount: myobService.countTotalError(task)
            ]
            data.putAll(task.meta)
            render(data as JSON)

        }else {
            render([status: "error", message: g.message(code: "no.task.found")] as JSON)
        }
    }

    def successLogSummary() {
        String token = params.token
        String tempLocation = SessionManager.protectedTempFolder.absolutePath + "/" + token + "/myob_item_import_export.job"
        Task task = MultiLoggerTask.getInstance(new File(tempLocation))
        if (task) {
            Map Logs = myobService.getLogs(task, "success")
            render(view: "/plugins/myob/summaryView", model: [logs: Logs, operation: task.meta.operation])
        } else {
            render(view: "/plugins/myob/summaryView", model: [emptyTask: true])
        }
    }

    def warningLogSummary() {
        String token = params.token
        String tempLocation = SessionManager.protectedTempFolder.absolutePath + "/" + token + "/myob_item_import_export.job"
        Task task = MultiLoggerTask.getInstance(new File(tempLocation))
        if (task) {
            Map Logs = myobService.getLogs(task, "warning")
            render(view: "/plugins/myob/summaryView", model: [logs: Logs, operation: task.meta.operation])
        } else {
            render(view: "/plugins/myob/summaryView", model: [emptyTask: true])
        }
    }

    def errorLogSummary() {
        String token = params.token
        String tempLocation = SessionManager.protectedTempFolder.absolutePath + "/" + token + "/myob_item_import_export.job"
        Task task = MultiLoggerTask.getInstance(new File(tempLocation))
        if (task) {
            Map Logs = myobService.getLogs(task, "error")
            render(view: "/plugins/myob/summaryView", model: [logs: Logs, operation: task.meta.operation])
        } else {
            render(view: "/plugins/myob/summaryView", model: [emptyTask: true])
        }
    }

    def beforeImport() {
        render(view: "/plugins/myob/initImport")
    }

    def beforeExport() {
        render(view: "/plugins/myob/initExport")
    }

    def test() {
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.MYOB)
        MyOBClient cacheClient = new MyOBClient(config.client_id, config.client_secret, config.redirect_uri, config.refresh_token)
        cacheClient.setCompanyFileProperties(config.company_file_uri, config.company_file_username, config.company_file_password)
        def response = cacheClient.performOperation(MYOB.READ,  "Sale/Invoice/Item")
        render response
    }

    def download() {
        String token = params.token
        String tempLocation = SessionManager.protectedTempFolder.absolutePath + "/" + token + "/myob_item_import_export.job"
        Task task
        try {
            task = MultiLoggerTask.getInstance(new File(tempLocation))
        } catch (FileNotFoundException ex) {
            log.error(ex.message, ex)
            render text: g.message(code: "task.not.found")
            return;
        } catch (Exception ex) {
            log.error(ex.message, ex)
        }
        if (task) {
            String path = Holders.servletContext.getRealPath("pub/myob-import-export")
            File file = new File(path + "/" + task.meta.logger_dump_file)
            response.setHeader("Content-disposition", "attachment; filename=\"${file.name}\"")
            response.setHeader("Content-Type", "application/vnd.ms-excel")
            response.outputStream << file.bytes
            response.outputStream.flush()
        } else {
            render text: g.message(code: "task.not.found")
        }
    }

    def collectCustomers() {
        render myobService.collectCustomers(params.start) as JSON
    }

    def updateCustomerLink() {
        myobService.updateCustomerLink(JSON.parse(params.json_map))
        render ([status: "success"] as JSON)
    }

    def collectOrders() {
        render myobService.collectOrders(params.start) as JSON
    }

    def updateOrderLink() {
        myobService.updateOrderLink(JSON.parse(params.json_map))
        render ([status: "success"] as JSON)
    }
}
