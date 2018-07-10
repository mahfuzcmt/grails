package com.webcommander.plugin.xero.controllers.admin

import com.webcommander.admin.ConfigService
import com.webcommander.admin.Customer
import com.webcommander.authentication.annotations.License
import com.webcommander.constants.DomainConstants
import com.webcommander.events.AppEventManager
import com.webcommander.listener.SessionManager
import com.webcommander.plugin.xero.XeroClient
import com.webcommander.plugin.xero.XeroService
import com.webcommander.plugin.xero.XeroTrack
import com.webcommander.plugin.xero.constants.LinkComponent
import com.webcommander.task.MultiLoggerTask
import com.webcommander.task.Task
import com.webcommander.task.TaskService
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.PaymentGateway
import com.webcommander.webcommerce.Product
import grails.converters.JSON
import org.grails.web.json.JSONObject


class XeroController {
    XeroService xeroService
    TaskService taskService
    ConfigService configService

    def config() {
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.XERO);
        render(view:  "/plugins/xero/admin/config", model: [config: config]);
    }

    def customer(){
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.XERO)
        render(view: "/plugins/xero/admin/customer", model: [xeroService: xeroService, config: config])
    }

    def product(){
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.XERO);
        render (view: "/plugins/xero/admin/product", model: [config: config])
    }

    def taxcode(){
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.XERO)
        render(view: "/plugins/xero/admin/taxcode", model: [xeroService: xeroService, config: config])
    }

    def order() {
        Map xeroConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.XERO);
        XeroClient client = new XeroClient(xeroConfig["consumer_key"], xeroConfig["consumer_secret"], xeroConfig["private_key"]);
        if(!xeroConfig."default_payment_account") {
            throw new ApplicationRuntimeException("default.payment.account.not.set")
        }
        try {
            String accountsDataAsJson = client.getAccounts();
            Map accountsData = JSON.parse(accountsDataAsJson)
            List accounts =[];
            List shippingAccounts =[];
            def generalList = ["REVENUE", "OTHERINCOME", "SALES"]
            def shippingTypeList = ["REVENUE", "OTHERINCOME", "SALES", "EXPENSE"]
            accounts += accountsData.Accounts.findAll{
                generalList.contains(it.Type)
            }?.collect {
                return ["code": it.Code, name: it.Code + "-" + it.Name];
            }
            shippingAccounts += accountsData.Accounts.findAll{
                shippingTypeList.contains(it.Type)
            }?.collect {
                return ["code": it.Code, name: it.Code + "-" + it.Name];
            }
            def syncedProductIdList = XeroTrack.findAll {
                eq "linkComponent", LinkComponent.TYPES.PRODUCT
                eq "xeroOrganisationId", XeroClient.currentOrganisation
                projections {
                    property("componentId")
                }
            }
            def syncedCustomerIdList = XeroTrack.findAll {
                eq "linkComponent", LinkComponent.TYPES.CUSTOMER
                eq "xeroOrganisationId", XeroClient.currentOrganisation
                projections {
                    property("componentId")
                }
            }
            def linkedProductList = syncedProductIdList.size() > 0? Product.findAll {inList "id", syncedProductIdList } : []
            def linkedCustomerList = syncedCustomerIdList.size() > 0? Customer.findAll {inList "id", syncedCustomerIdList }: []
            render(view: "/plugins/xero/admin/order", model: [config: xeroConfig, linkedProductList: linkedProductList,
                                                              linkedCustomerList: linkedCustomerList, accounts: accounts, shippingAccounts: shippingAccounts])
        } catch(Exception e){
            render(view: "/plugins/xero/admin/invalidConfigView", model: [:])
        }
    }

    def paymentAccount() {
        Map xeroConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.XERO);
        XeroClient client = new XeroClient(xeroConfig["consumer_key"], xeroConfig["consumer_secret"], xeroConfig["private_key"]);
        try {
            String accountsDataAsJson = client.getAccounts();
            Map accountsData = JSON.parse(accountsDataAsJson)
            List accounts =[];
            def sortedAccounts = accountsData.Accounts.findAll {
                (it."Type" == "BANK" || it."Type" == "CURRENT") && it."EnablePaymentsToAccount" == true
            }
            accounts += sortedAccounts.collect {
                return ["code": it.Code, name: it.Code + "-" + it.Name];
            }
            def paymentGatewayList = PaymentGateway.findAll() {eq "isEnabled", true}
            def paymentMapping = xeroConfig.payment_account_mapping? JSON.parse(xeroConfig.payment_account_mapping) : null
            render(view: "/plugins/xero/admin/paymentAccount", model: [config: xeroConfig, accounts: accounts, paymentGatewayList: paymentGatewayList, paymentMapping: paymentMapping])
        } catch(Exception e){
            render(view: "/plugins/xero/admin/invalidConfigView", model: [:])
        }
    }

    def connectionConfig() {
        Map paramClone = params.clone()
        def type = DomainConstants.SITE_CONFIG_TYPES.XERO
        def consumerKey =  params."${type}.consumer_key", consumerSecret = params."${type}.consumer_secret", privateKey =  params."${type}.private_key"
        XeroClient xeroClient = new XeroClient(consumerKey, consumerSecret, privateKey)
        def object = xeroClient.getOrganisation()
        JSONObject jsonObject = new JSONObject(object)
        def organisationId = (jsonObject.Organisations).first().APIKey
        if (organisationId == null)
            organisationId = "DEMO_COMPANY"

        if (jsonObject.Status == "OK" && organisationId) {
            params."${type}.organisation_id" = organisationId
            def configs = [];
            params.list("type").each { typex ->
                params."${typex}".each {
                    configs.add([type: type, configKey: it.key, value: it.value]);
                }
            }
            if (configService.update(configs)) {
                AppEventManager.fire("zero-after-settings-updated", [configs])
                render([status: "success", message: g.message(code: "setting.updated.successfully")] as JSON)
            } else {
                render([status: "error", message: g.message(code: "setting.not.updated")] as JSON)
            }
        }
        else {
            throw new ApplicationRuntimeException("unable.to.connect.with.xero")
        }
    }

    @License(required = "allow_xero_feature")
    def loadAppView() {
        render(view: "/plugins/xero/admin/appView");
    }

    def beforeImport() {
        render(view: "/plugins/xero/admin/initImport")
    }

    def beforeExport() {
        render(view: "/plugins/xero/admin/initExport")
    }

    @License(required = "allow_xero_feature")
    def initImport() {
        Task importTask = xeroService.initImport(params)
        String token = importTask.token
        if(params.append_token) {
            Task t = Task.getInstance(new File(SessionManager.protectedTempFolder.absolutePath + "/" + params.append_token + "/xero_item_import_export.job"))
            importTask.meta.task_cache_location = t.meta.task_cache_location
            importTask.meta.logger_dump_file = t.meta.logger_dump_file
        } else {
            importTask.meta.task_cache_location = SessionManager.protectedTempFolder.absolutePath + "/" + token + "/xero_item_import_export.job"
            Date date = new Date().gmt().toZone(session.timezone)
            importTask.meta.logger_dump_file = "XERO-import-${date.toFormattedString("yyyy-MM-dd", true, "HH_mm_ss_SSS", false, null)}.xlsx";
        }
        render([token: importTask.token, name: importTask.name] as JSON);
    }

    @License(required = "allow_xero_feature")
    def initExport() {
        Task exportTask = xeroService.initExport(params)
        exportTask.meta.task_cache_location = SessionManager.protectedTempFolder.absolutePath + "/" + exportTask.token + "/xero_item_import_export.job"
        Date date = new Date().gmt().toZone(session.timezone)
        exportTask.meta.logger_dump_file = "XERO-import-${date.toFormattedString("yyyy-MM-dd", true, "HH_mm_ss_SSS", false, null)}.xlsx";
        render ([token: exportTask.token, name: exportTask.name] as JSON);
    }

    def progressView(){
        String token = params.token;
        Task task = taskService.getByToken(token);
        def type = params.type;
        render(
                view: "/plugins/xero/admin/progressView",
                model: [
                        task: task,
                        totalSuccessCount: xeroService.countTotalSuccess(task),
                        totalWarningCount: xeroService.countTotalWarning(task),
                        totalErrorCount: xeroService.countTotalError(task)
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
                totalSuccessCount: xeroService.countTotalSuccess(task),
                totalWarningCount: xeroService.countTotalWarning(task),
                totalErrorCount: xeroService.countTotalError(task)
            ]
            data.putAll(task.meta)
            render(data as JSON)
        } else {
            render([status: "error", message: g.message(code: "no.task.found")] as JSON)
        }
    }

    def successLogSummary() {
        String token = params.token
        String tempLocation = SessionManager.protectedTempFolder.absolutePath + "/" + token + "/xero_item_import_export.job"
        Task task = MultiLoggerTask.getInstance(new File(tempLocation))
        if (task) {
            Map Logs = xeroService.getLogs(task, "success")
            render(view: "/plugins/xero/admin/summaryView", model: [logs: Logs, operation: task.meta.operation])
        } else {
            render(view: "/plugins/xero/admin/summaryView", model: [emptyTask: true])
        }
    }

    def warningLogSummary() {
        String token = params.token
        String tempLocation = SessionManager.protectedTempFolder.absolutePath + "/" + token + "/xero_item_import_export.job"
        Task task = MultiLoggerTask.getInstance(new File(tempLocation))
        if (task) {
            Map Logs = xeroService.getLogs(task, "warning")
            render(view: "/plugins/xero/admin/summaryView", model: [logs: Logs, operation: task.meta.operation])
        } else {
            render(view: "/plugins/xero/admin/summaryView", model: [emptyTask: true])
        }
    }

    def errorLogSummary() {
        String token = params.token
        String tempLocation = SessionManager.protectedTempFolder.absolutePath + "/" + token + "/xero_item_import_export.job"
        Task task = MultiLoggerTask.getInstance(new File(tempLocation))
        if (task) {
            Map Logs = xeroService.getLogs(task, "error")
            render(view: "/plugins/xero/admin/summaryView", model: [logs: Logs, operation: task.meta.operation])
        } else {
            render(view: "/plugins/xero/admin/summaryView", model: [emptyTask: true])
        }
    }

    def download() {
        String token = params.token
        String tempLocation = SessionManager.protectedTempFolder.absolutePath + "/" + token + "/xero_item_import_export.job"
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
            String path = Holders.servletContext.getRealPath("pub/xero-import-export")
            File file = new File(path + "/" + task.meta.logger_dump_file)
            response.setHeader("Content-disposition", "attachment; filename=\"${file.name}\"")
            response.setHeader("Content-Type", "application/vnd.ms-excel")
            response.outputStream << file.bytes
            response.outputStream.flush()
        } else {
            render text: g.message(code: "task.not.found")
        }
    }

    def saveConfigurations(){
        def configs = [];
        def previousConfigs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.XERO);
        if(!params.xero) {
            params.xero = [:]
        }
        if(params.isSetting && (params.xero.client_id != previousConfigs.client_id || params.xero.client_secret != previousConfigs.client_secret)){
            configs.add([type: "xero", configKey: "refresh_token", value: null]);
            configs.add([type: "xero", configKey: "company_file_uri", value: null]);
        }
        if (params."payment_account") {
            def paymentMap = [:]
            params."payment_account".each {
                paymentMap.put(it.key, it.value)
            }
            params.xero.payment_account_mapping = paymentMap as JSON
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
        if (configService.update(configs)) {
            render([status: "success", message: g.message(code: "setting.updated.successfully")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "setting.not.updated")] as JSON)
        }
    }

}
