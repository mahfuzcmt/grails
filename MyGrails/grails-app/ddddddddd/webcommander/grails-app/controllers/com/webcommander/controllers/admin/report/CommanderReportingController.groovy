package com.webcommander.controllers.admin.report

import com.webcommander.admin.Dashlet
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.report.FavouriteReport
import com.webcommander.report.ReportingService
import grails.converters.JSON

class CommanderReportingController {

    ReportingService reportingService
    CommonService commonService

    def loadAppView() {
        render view: "/admin/reporting/navigationsView"
    }

    def loadRealTimeView() {
        List quicks = reportingService.getQuicks([[title: "total.sales", uiClass: "total-sales"], [title: "total.item.sold", uiClass: "total-item-sold"], [title: "total.orders", uiClass: "total-orders"]], params.hour.toInteger(1))
        List latestSolds = reportingService.latestSoldProducts(params.hour.toInteger(1))
        List latestActivities = reportingService.latestActivities(params.hour.toInteger(1))
        reportingService.populateSummaryNImages(latestSolds)
        render view: "/admin/reporting/realTimeView", model: [quicks: quicks, latestSolds: latestSolds, latestActivities: latestActivities]
    }

    def loadProductReport() {
        if(!params.reportCode) {
            params.reportCode = "product.by.properties"
        }
        render (view: "/admin/reporting/nonRealTime", model: [title: params.reportCode, chartRenderer: {
            reporting.renderProductChart(type: params.chartType ?: "line")
        }, tabularRendererAction: "loadProductTabularReport", reportTypes: ['product.by.properties', 'product.by.customers', 'product.by.billing.address', 'product.by.shipping.address', 'product.by.period']])
    }

    def loadOrderReport() {
        if(!params.reportCode) {
            params.reportCode = "order.by.status"
        }
        if(!params.duration) {
            params.duration = "today"
        }
        render (view: "/admin/reporting/nonRealTime", model: [title: params.reportCode, chartRenderer: {
            reporting.renderOrderChart(type: params.chartType ?: "line")
        }, tabularRendererAction: "loadOrderTabularReport", reportTypes: ['order.by.status', 'order.by.customers', 'order.by.billing.address', 'order.by.shipping.address', 'order.by.period']])
    }

    def loadPaymentReport() {
        if(!params.reportCode) {
            params.reportCode = "payment.by.properties"
        }
        if(!params.duration) {
            params.duration = "today"
        }
        render (view: "/admin/reporting/nonRealTime", model: [title: params.reportCode, chartRenderer: {
            reporting.renderPaymentChart(type: params.chartType ?: "line")
        }, tabularRendererAction: "loadPaymentTabularReport", reportTypes: ['payment.by.properties', 'payment.by.status', 'payment.by.customers', 'payment.by.billing.address', 'payment.by.shipping.address', 'payment.by.month']])
    }

    def loadTaxReport() {
        render (view: "/admin/reporting/nonRealTime", model: [title: "taxes.by.month", chartRenderer: {
            reporting.renderTaxChart(type: params.chartType ?: "line")
        }, tabularRendererAction: "loadTaxTabularReport"])
    }

    def addToFavourite() {
        FavouriteReport report;
        if(params.id) {
            report = FavouriteReport.get(params.id)
        }
        render view: "/admin/reporting/addToFavouritePopUp", model: [report: report]
    }

    def loadFavouriteAppView() {
        Long totalCount = FavouriteReport.count()
        List<FavouriteReport> reports = commonService.withOffset params.max ?: 10, params.offset ?: 0, totalCount, { max, offset, count ->
            FavouriteReport.list(offset: offset, max: max)
        }
        render view: "/admin/reporting/favouriteAppView", model: [reports: reports, count: totalCount]
    }

    def print() {
        render ( view: "/admin/reporting/print" )
    }

    def saveFavourite() {
        boolean saved = reportingService.saveFavourite(params.id?.toLong(), params.reportName, params.filters, params.type)
        if(saved) {
            render([status: "success", message: g.message(code: "report.saved.successfully")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "report.not.saved")] as JSON)
        }
    }

    def export() {
        render view: "/admin/reporting/exportPopUp"
    }

    def loadProductTabularReport() {
        if(!params.duration) {
            params.duration = "today"
        }
        if(!params.reportCode) {
            params.reportCode = "product.by.properties"
        }
        String suffix = params.reportCode.substring(11).camelCase()
        List products = reportingService."loadProductBy$suffix"(params)
        render (view: "/admin/reporting/tabularReport/productTable/productBy$suffix", model: [products: products])
    }

    def loadOrderTabularReport() {
        if(!params.duration) {
            params.duration = "today"
        }
        if(!params.reportCode) {
            params.reportCode = "order.by.status"
        }
        String suffix = params.reportCode.substring(9).camelCase()
        List orders = reportingService."loadOrderBy$suffix"(params)
        render (view: "/admin/reporting/tabularReport/orderTable/orderBy$suffix", model: [orders: orders])
    }

    def loadPaymentTabularReport() {
        if(!params.duration) {
            params.duration = "today"
        }
        if(!params.reportCode) {
            params.reportCode = "payment.by.properties"
        }
        String suffix = params.reportCode.substring(11).camelCase()
        List payments = reportingService."loadPaymentBy$suffix"(params)
        render (view: "/admin/reporting/tabularReport/paymentTable/paymentBy$suffix", model: [payments: payments])
    }

    def loadTaxTabularReport() {
        if(!params.duration) {
            params.duration = "today"
        }
        List tax = reportingService.loadTaxByMonth(params)
        render (view: "/admin/reporting/tabularReport/taxTable/taxByMonth", model: [taxList: tax])
    }

    def loadChartOptionForProduct() {
        Map chartOptions = [chartYOptionList: ["units.sold", "gross.sales"], yAxis: params.yaxis ?: "units.sold", chartType: params.chartType ?: "line"]
        if(params.reportCode == "product.by.customers") {
            chartOptions.chartXOptionList = ["customer.name", "customer.email", "customer.type", "customer.sex"]
        } else if(params.reportCode == "product.by.billing.address") {
            chartOptions.chartXOptionList = ["billing.country", "billing.region", "billing.city"]
        } else if(params.reportCode == "product.by.shipping.address") {
            chartOptions.chartXOptionList = ["shipping.country", "shipping.region", "shipping.city"]
        } else if(params.reportCode == "product.by.period") {
            chartOptions.chartXOptionList = ["year", "month"]
        } else {
            chartOptions.chartXOptionList = ["type", "category", "name", "price"]
        }
        chartOptions.xAxis = params.xaxis ?: chartOptions.chartXOptionList[0]
        render view: "/admin/reporting/chartOptionSelect", model: chartOptions
    }

    def loadChartOptionForOrder() {
        Map chartOptions = [chartYOptionList: ["total.sales", "total.discounts", "total.shipping", "total.taxes", "order.count"], yAxis: params.yaxis ?: "total.sales", chartType: params.chartType ?: "line"]
        if(params.reportCode == "order.by.customers") {
            chartOptions.chartXOptionList = ["customer.name", "customer.email", "customer.type", "customer.sex"]
        } else if(params.reportCode == "order.by.billing.address") {
            chartOptions.chartXOptionList = ["billing.country", "billing.region", "billing.city"]
        } else if(params.reportCode == "order.by.shipping.address") {
            chartOptions.chartXOptionList = ["shipping.country", "shipping.region", "shipping.city"]
        } else if(params.reportCode == "order.by.period") {
            chartOptions.chartXOptionList = ["year", "month"]
        } else {
            chartOptions.chartXOptionList = ["status"]
        }
        chartOptions.xAxis = params.xaxis ?: chartOptions.chartXOptionList[0]
        render view: "/admin/reporting/chartOptionSelect", model: chartOptions
    }

    def loadChartOptionForPayment() {
        Map chartOptions = [chartYOptionList: ["transaction.count", "total.refunds", "total.payments"], yAxis: params.yaxis ?: "transaction.count", chartType: params.chartType ?: "line"]
        if(params.reportCode == "payment.by.status") {
            chartOptions.chartXOptionList = ["payment.status"]
        } else if(params.reportCode == "payment.by.customers") {
            chartOptions.chartXOptionList = ["customer.name", "customer.email", "customer.type", "customer.sex"]
        } else if(params.reportCode == "payment.by.billing.address") {
            chartOptions.chartXOptionList = ["billing.country", "billing.region", "billing.city"]
        } else if(params.reportCode == "payment.by.shipping.address") {
            chartOptions.chartXOptionList = ["shipping.country", "shipping.region", "shipping.city"]
        } else if(params.reportCode == "payment.by.month") {
            chartOptions.chartXOptionList = ["payment.year", "payment.month"]
        } else {
            chartOptions.chartXOptionList = ["payment.method"]
        }
        chartOptions.xAxis = params.xaxis ?: chartOptions.chartXOptionList[0]
        render view: "/admin/reporting/chartOptionSelect", model: chartOptions
    }

    def loadChartOptionForTax() {
        render (view: "/admin/reporting/chartOptionSelect", model: [chartXOptionList: ["period"], chartYOptionList: ["total.taxes"], xAxis: "period", yAxis: "total.taxes", chartType: params.chartType ?: "line"])
    }

    def loadFilterForProduct() {
        List tableFilters
        if(params.reportCode == "product.by.customers") {
            tableFilters = ["customer.name", "customer.email", "customer.type", "customer.sex"]
        } else if(params.reportCode == "product.by.billing.address") {
            tableFilters = ["billing.country", "billing.region", "billing.city"]
        } else if(params.reportCode == "product.by.shipping.address") {
            tableFilters = ["shipping.country", "shipping.region", "shipping.city"]
        } else if(params.reportCode == "product.by.period") {
            tableFilters = ["year", "month"]
        } else {
            tableFilters = ["type", "category", "name", "price"]
        }
        render (view: "/admin/reporting/filterOptionSelect", model: [tableFiltersList: tableFilters])
    }

    def loadFilterForOrder() {
        List tableFilters
        if(params.reportCode == "order.by.customers" ) {
            tableFilters = ["customer.name", "customer.email", "customer.type", "customer.sex"]
        } else if(params.reportCode == "order.by.billing.address") {
            tableFilters = ["billing.country", "billing.region", "billing.city"]
        } else if(params.reportCode == "order.by.shipping.address") {
            tableFilters = ["shipping.country", "shipping.region", "shipping.city"]
        } else if(params.reportCode == "order.by.period") {
            tableFilters = ["year", "month"]
        } else {
            tableFilters = ["status"]
        }
        render (view: "/admin/reporting/filterOptionSelect", model: [tableFiltersList: tableFilters])
    }

    def loadFilterForPayment() {
        List tableFilters
        if(params.reportCode == "payment.by.status") {
            tableFilters = ["payment.status"]
        } else if(params.reportCode == "payment.by.customers") {
            tableFilters = ["customer.name", "customer.email", "customer.type", "customer.sex"]
        } else if(params.reportCode == "payment.by.billing.address") {
            tableFilters = ["billing.country", "billing.region", "billing.city"]
        } else if(params.reportCode == "payment.by.shipping.address") {
            tableFilters = ["shipping.country", "shipping.region", "shipping.city"]
        } else if(params.reportCode == "payment.by.month") {
            tableFilters = ["payment.year", "payment.month"]
        } else {
            tableFilters = ["payment.method"]
        }
        render (view: "/admin/reporting/filterOptionSelect", model: [tableFiltersList: tableFilters])
    }

    def loadFilterForTax() {
        List tableFilters = ["year", "month"]
        render (view: "/admin/reporting/filterOptionSelect", model: [tableFiltersList: tableFilters])
    }

    def renderFavouriteReportChart() {
        Dashlet dashlet = Dashlet.load(params.dashlet)
        if(dashlet?.contents) {
            Long reportId = dashlet.contents[0].contentId.toLong()
            FavouriteReport report = FavouriteReport.get(reportId)
            if(report) {
                params.putAll(JSON.parse(report.filters))
                render view: "/admin/reporting/dashletReportChart", model: [type: report.type]
            } else {
                render(view: "/admin/dashboard/emptyFavouriteReport", model: [data: false]);
            }
        } else {
            render(view: "/admin/dashboard/emptyFavouriteReport", model: [data: false]);
        }
    }

    def deleteReport() {
        Long id = params.long("id");
        if(reportingService.deleteReport(id, params.at2_reply, params.at1_reply)) {
            render([status: "success", message: g.message(code: "report.deleted.successfully")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "report.not.deleted")] as JSON)
        }
    }
}