package com.webcommander.admin

import com.webcommander.events.AppEventManager

class Dashlet {

    Long id
    String uniqueName
    String title
    String controller
    String action
    Long height
    Double width
    Long idx
    Boolean isVisible = true

    Collection<DashletContent> contents = []

    static  hasMany = [contents: DashletContent]

    static constraints = {
        uniqueName(blank: false, maxSize: 50)
        title(blank: false, maxSize: 100)
    }

    @Override
    int hashCode() {
        if (id) {
            return ("Dashlet: " + id).hashCode()
        }
        return super.hashCode()
    }

    static void initialize() {
        def insertSql = [
            ['quick.report', "dashboard", 'quickReport', 100, 17, 1, true, 'quickReport', [
                [1, "", "total-orders", "total.orders", true],
                [2, "", "total-customers", "total.customers", true],
                [3, "", "total-sales", "total.sales", true],
                [4, "", "gross-sales", "gross.sales", true],
                [5, "", "total-item-sold", "total.item.sold", true],
                [6, "", "average-order-value", "average.order.value", false],
                [7, "", "canceled-orders", "canceled.orders", false]
            ]],
            ['latest.order', "dashboard", 'latestStat', 40, 43, 2, true, 'latestStat', [
                    [1, "", "latest-order", "latest.order", true],
                    [2, "", "latest-product", "latest.product", false],
                    [3, "", "latest-customer", "latest.customer", false],
                    [4, "", "latest-activity", "latest.activity", false],
            ]],
            ['web.commerce', "dashboard", 'webCommerce', 29, 26, 3, true, 'webCommerce', [
                [1, "web_commerce", "item", "product", true],
                [2, "web_commerce", "payment-gateway", "payment.gateway", true],
                [3, "web_commerce", "tax", "tax", true],
                [4, "web_commerce", "shipping", "shipping", true],
                [5, "web_commerce", "brand", "brand", true],
                [6, "web_commerce", "currency", "currency", true]
            ]],
            ['web.content.and.web.design', "dashboard", 'webContentAndDesign', 29, 26, 4, true, 'webContentAndDesign', [
                [1, "web_design", "layout", "layout", true],
                [2, "web_content", "page", "page", true],
                [3, "web_content", "content", "content", true],
                [4, "web_content", "navigation", "navigation", true],
                [5, "web_content", "album", "album", true],
                [6, "web_content", "asset-library", "asset.library", true]
            ]],
            ['administration.and.web.marketing', "dashboard", 'administrationAndMarketing', 59, 16, 5, true, 'administrationAndMarketing', [
                [1, "administration", "plugin", "plugin", true],
                [2, "administration", "customer", "customer", true],
                [3, "administration", "trash", "trash", true],
                [4, "administration", "operator", "operator", true],
                [5, "web_marketing", "sitemap", "sitemap", true],
                [6, "web_marketing", "newsletterView", "newsletterView", true]
            ]],
            ['favourite.report', 'commanderReporting', 'renderFavouriteReportChart', 49.5, 35, 6, true, 'favouriteReportChartOne'],
            ['favourite.report', 'commanderReporting', 'renderFavouriteReportChart', 49.5, 35, 7, true, 'favouriteReportChartTwo']
        ]
        if (!Dashlet.count()) {
            insertSql.each {
                Dashlet dashlet = new Dashlet(title: it[0], uniqueName: it[7], controller: it[1], action: it[2], width: it[3], height: it[4], idx: it[5], isVisible: it[6]).save()
                it[8]?.each { content ->
                    new DashletContent(idx: content[0], title: content[3], contentId: content[1], uiClass: content[2], holder: dashlet, isVisible: content[4]).save()
                }
            }
        }
        AppEventManager.fire("dashlet-bootstrap-init")
    }

    @Override
    boolean equals(Object obj) {
        return obj instanceof String ? uniqueName == obj : super.equals(obj)
    }
}