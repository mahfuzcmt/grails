package com.webcommander.controllers.admin

import com.webcommander.admin.*
import com.webcommander.config.StoreDetail
import com.webcommander.constants.DomainConstants
import com.webcommander.models.DashletFlow
import com.webcommander.sso.ProvisionAPIService
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Currency
import com.webcommander.webcommerce.CurrencyService
import grails.converters.JSON

class DashboardController {
    DashboardService dashboardService
    ConfigService configService
    CurrencyService currencyService
    AdministrationService administrationService
    ProvisionAPIService provisionAPIService

    def loadDashlet() {
        Dashlet dashlet = Dashlet.get(params.long("id"));
        render(view: "/admin/dashboard/dashlet", model: [dashlet: dashlet]);
    }

    def quickReport() {
        List components = dashboardService.getStatistics()
        render(view: "/admin/dashboard/quickReport", model:[components: components]);
    }

    def configPopUp() {
        params.holder = Dashlet.findByUniqueName(params.configType)
        if( !params.configType.equals("quickReport")) {
            dashboardService.updateRibbons(params)
        }
        List dashletItemList = dashboardService.getDashletItem(params)
        dashletItemList.removeAll {DomainConstants.ECOMMERCE_DASHLET_CHECKLIST[it.uiClass.replace("-","_")] && (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'false')}
        render(view: "/admin/dashboard/ribonConfigPopUp", model:[dashletItemList: dashletItemList, params: params]);
    }

    def saveRibbonConfiguration() {
        if(dashboardService.saveConfiguration(params)) {
            render([status: "success", message: g.message(code: "configuration.saved.successful")] as JSON)
        }
        else {
            render([status: "error", message: g.message(code: "configuration.saved.failed")] as JSON)
        }
    }

    def webCommerce() {
        params.holder = Dashlet.findByUniqueName("webCommerce")
        params.isVisible = true;
        List dashletItems = dashboardService.getDashletItem(params)
        render(view: "/admin/dashboard/webCommerce", model: [dashletItems: dashletItems]);
    }

    def webContentAndDesign() {
        params.holder = Dashlet.findByUniqueName("webContentAndDesign")
        params.isVisible = true;
        List dashletItems = dashboardService.getDashletItem(params)
        render(view: "/admin/dashboard/webContentAndDesign", model: [dashletItems: dashletItems]);
    }

    def administrationAndMarketing() {
        params.holder = Dashlet.findByUniqueName("administrationAndMarketing")
        params.isVisible = true;
        List dashletItems = dashboardService.getDashletItem(params)
        render(view: "/admin/dashboard/administrationAndWebMarketing", model: [dashletItems: dashletItems]);
    }

    def latestStat() {
        def activeStats = dashboardService.getActiveLatestState();
        switch (activeStats) {
            case "latest.order" :
                latestOrder();
                break;
            case "latest.product" :
                latestProduct();
                break;
            case "latest.customer" :
                latestCustomer();
                break;
            case "latest.activity":
                latestActivity();
                break;
        }

    }

    def latestOrder() {
        List components = dashboardService.getLatestOrder(params)
        render(view: "/admin/dashboard/latestOrder", model:[components: components]);
    }

    def latestCustomer() {
        List components = dashboardService.getLatestCustomer(params)
        render(view: "/admin/dashboard/latestCustomer", model:[components: components]);
    }

    def latestProduct() {
        List latestSolds = dashboardService.getLatestProduct();
        render(view: "/admin/dashboard/latestProduct", model:[latestSolds: latestSolds]);
    }

    def latestActivity() {
        List latestActivities = dashboardService.getLatestActivity();
        render(view: "/admin/dashboard/latestActivity", model:[latestActivities: latestActivities]);
    }

    def configLatestTablePopUp() {
        params.holder = Dashlet.findByUniqueName("latestStat")
        DashletContent activeStat = DashletContent.findByTitle(dashboardService.getActiveLatestState())
        List dashletItemList = dashboardService.getDashletItem(params);
        dashletItemList.removeAll {DomainConstants.ECOMMERCE_DASHLET_CHECKLIST[it.uiClass.replace("-","_")] && (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'false')}
        render(view: "/admin/dashboard/latestConfigPopUp", model:[dashletItemList: dashletItemList, activeStat: activeStat]);
    }

    def saveLatestConfig() {
        params.configType = "latestStat"
        if(dashboardService.saveConfiguration(params)) {
            render([status: "success", message: g.message(code: "configuration.saved.successful")] as JSON)
        }
        else {
            render([status: "error", message: g.message(code: "configuration.saved.failed")] as JSON)
        }
    }

    def latestStatUpdate() {
        DashletFlow flow = dashboardService.getDashletsSorted().find{
            it.dashlet.uniqueName == "latestStat"
        }
        render(view: "/admin/dashboard/dashlet", model: [flow: flow, dashlet: flow.dashlet]);
    }

    def configFavouriteReportPopUp() {
        Map favouriteReport = dashboardService.getFavouriteReport(params);
        render(view: "/admin/dashboard/favouriteReportConfigPopUp", model:[favouriteReport: favouriteReport.reportList, activeStat: favouriteReport.active, dashletId: params.dashletId]);
    }

    def saveReportConfig() {
        params.configType = "favouriteReports"
        if(dashboardService.saveConfiguration(params)) {
            render([status: "success", message: g.message(code: "configuration.saved.successful")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "configuration.saved.failed")] as JSON)
        }
    }

    def loadReviewTax() {
        def taxSettings = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.TAX);
        Currency currency = Currency.findByBase(true);
        render(view: "/admin/dashboard/gettingStarted/taxForm", model: [currency: currency, taxSettings: taxSettings]);
    }

    def loadStartedProperties() {
        switch (params.property) {
            case "storeDetails":
                render(view: "/admin/dashboard/gettingStarted/storeDetails", model: [:])
                break;
            case "email":
                render(view: "/admin/dashboard/gettingStarted/email", model: [:])
                break;
            case "product":
                render(view: "/admin/dashboard/gettingStarted/product", model: [:])
                break;
            case "page":
                render(view: "/admin/dashboard/gettingStarted/page", model: [:])
                break;
            case "design":
                render(view: "/admin/dashboard/gettingStarted/design", model: [:])
                break;
            case "tax":
                render(view: "/admin/dashboard/gettingStarted/tax", model: [:])
                break;
            case "shipping":
                render(view: "/admin/dashboard/gettingStarted/shipping", model: [:])
                break;
            case "paymentGateway":
                render(view: "/admin/dashboard/gettingStarted/paymentGateway", model: [:])
                break;
            case "launchStore":
                render(view: "/admin/dashboard/gettingStarted/launchStore", model: [:])
                break;
        }
    }

    def updateTax() {
        currencyService.setBaseCurrency(params.long("default_currency"))
        forward(controller: "setting", action: "saveConfigurations");
    }

    def loadPaymentForm() {
        StoreDetail storeDetail = StoreDetail.first();
        def states = storeDetail?.address ? administrationService.getStatesForCountry(storeDetail.address.country.id) : [];
        render(view: "/admin/dashboard/gettingStarted/paymentForm", model: [storeDetail: storeDetail, states: states]);
    }

    def softDone() {
        boolean update = dashboardService.updateSoftDone(params.key, "true");
        if(update) {
            render([status: "success"] as JSON)
        } else {
            render([status: "error"] as JSON)
        }
    }

    def loadOnScreenHelp() {
        List title = ["configure.as.you.go", "settings", "own.your.dashboard", "bring.your.content", "being.ecommerce.adventure", "select.and.customize.design", "subscription.management", "more.help"]
        List ScreenMessage = ["on.screen.first", "on.screen.second", "on.screen.third", "on.screen.fourth", "on.screen.fifth", "on.screen.sixth", "on.screen.seventh", "on.screen.eight"]
        Integer count = params.count.toInteger();
        render (view: "/admin/dashboard/onScreenHelp", model: [title: title[count-1], description: ScreenMessage[count-1], count: params.wizard ? count : count - 1])
    }

    def allInstances() {
        render(provisionAPIService.getAllInstances() as JSON)
    }

    def instanceInfo() {
        render(provisionAPIService.getInstanceInfo(params.instanceIdentity) as JSON)
    }
}
