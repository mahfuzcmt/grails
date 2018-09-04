package com.webcommander.admin

import com.webcommander.annotations.Initializable
import com.webcommander.constants.DomainConstants
import com.webcommander.events.AppEventManager
import com.webcommander.models.DashletFlow
import com.webcommander.models.DashletFlowLayout
import com.webcommander.report.FavouriteReport
import com.webcommander.report.ReportingService
import com.webcommander.webcommerce.Order
import com.webcommander.webcommerce.ProductService
import grails.converters.JSON
import grails.gorm.transactions.Transactional

@Initializable
class DashboardService {
    ReportingService reportingService
    ConfigService configService
    ProductService productService


    static void initialize() {
        AppEventManager.on("before-favourite-report-delete") { id, at1_reply ->
            DashletContent.createCriteria().list {
                holder {
                    inList "title", ['favourite.report']
                }
                eq "contentId", "" + id
            }*.delete()
        }
    }

    List<DashletFlow> getDashletsSorted() {
        List<Dashlet> dashlets = Dashlet.createCriteria().list {
            eq("isVisible", true)
            order("idx", "asc")
        }
        List<DashletFlow> flows = []
        DashletFlowLayout lastRow = new DashletFlowLayout()
        int topAdjust = 0
        boolean first = true
        dashlets.each { dashlet ->
            DashletFlow flow
            if (first) {
                flow = lastRow.addAt(0, dashlet)
                first = false
            } else if (lastRow.points.size() == 4) {
                topAdjust += lastRow.highestPoint
                lastRow = new DashletFlowLayout()
                flow = lastRow.addAt(0, dashlet)
            } else {
                int requiredGap = dashlet.width
                Float foundGap = lastRow.findGap(requiredGap)
                if (foundGap != null) {
                    flow = lastRow.addAt(foundGap, dashlet)
                } else {
                    topAdjust += lastRow.highestPoint
                    lastRow = new DashletFlowLayout()
                    flow = lastRow.addAt(0, dashlet)
                }
            }
            flow.top += topAdjust
            flows.push(flow)
        }
        return flows
    }

    List getStatistics() {
        List quickReportList = DashletContent.createCriteria().list {
            eq("holder", Dashlet.findByUniqueName("quickReport"))
            eq("isVisible", true)
            order("idx", "asc")
        }
        return reportingService.getQuicks(quickReportList)
    }

    List getLatestOrder(Map params) {
        return (Order.createCriteria().list {
            ne("orderStatus", DomainConstants.ORDER_STATUS.CANCELLED)
            order("created", "desc")
            maxResults(10)
        })
    }

    List getLatestCustomer(Map params) {
        return (Customer.createCriteria().list() {
            order("created", "desc")
            maxResults(10)
        })
    }

    List getLatestProduct() {
        List latestSolds = reportingService.latestSoldProducts(72)
        reportingService.populateSummaryNImages(latestSolds)
        return latestSolds
    }

    List getLatestActivity() {
        return ((reportingService.latestActivities(72)))
    }

    List getDashletItem(Map params) {
        List dashletItems = DashletContent.createCriteria().list {
            and {
                if (params.holder) {
                    eq("holder", params.holder)
                }
                if (params.isVisible) {
                    eq("isVisible", true)
                }
            }
            order("idx", "asc")
        }
        return dashletItems
    }

    @Transactional
    boolean saveConfiguration(Map params) {
        params.remove("controller")
        params.remove("action")
        if (params.reportGroup) {
            Dashlet dashlet = Dashlet.findByUniqueName(params.configType)
            Long selectedContentId = params.reportGroup.toLong(0)
            DashletContent.where {
                id != selectedContentId
                holder == dashlet
            }.updateAll(isVisible: false)

            DashletContent content = DashletContent.findByHolderAndId(dashlet, selectedContentId)
            content.isVisible = true
            content.save()
            dashlet.title = content.title
            dashlet.save()
            return (!dashlet.hasErrors())
        } else if (params.configType == "favouriteReports") {
            Dashlet dashlet = Dashlet.proxy(params.dashletId)
            DashletContent.findByHolder(dashlet)?.delete()

            if (new DashletContent(idx: 1, contentId: params.fvrtReportGroup, holder: dashlet).save()) {
                return true
            } else {
                return false
            }
        } else {
            Dashlet dashlet = Dashlet.findByUniqueName(params.configType)
            DashletContent.where {
                holder == dashlet
            }.updateAll(isVisible: false)

            Boolean isSaved = DashletContent.where {
                holder == dashlet
                'in'("uiClass", params.name.collect())
            }.updateAll(isVisible: true)
            return (isSaved)
        }
    }

    void updateRibbons(Map params) {
        Dashlet ribbonHolder = params.holder
        List currentRibbonItems = []
        params.ribbons.each {
            currentRibbonItems.add(JSON.parse(it))
        }
        List tempList = currentRibbonItems.asList().uiClass
        List currentRibbonList = DashletContent.createCriteria().list {
            and {
                eq("holder", ribbonHolder)
                not { 'in'("uiClass", tempList) }
            }

        }
        if (currentRibbonList.size() > 0)
            currentRibbonList*.delete()

        Long cnt = DashletContent.createCriteria().get {
            projections {
                max "idx"
            }
            eq("holder", ribbonHolder)
        } ?: 0

        currentRibbonItems.each { ribbon ->
            if (!DashletContent.find("from DashletContent as d where d.holder=:holder and d.uiClass=:uiClass", [holder: ribbonHolder, uiClass: ribbon.uiClass])) {
                new DashletContent(idx: ++cnt, title: ribbon.uiClass.replace("-", "."), contentId: ribbon.tabId, uiClass: ribbon.uiClass, holder: ribbonHolder, isVisible: false).save(flush: true)
            }

        }

    }

    String getActiveLatestState() {
        Dashlet holder = Dashlet.findByUniqueName("latestStat")
        String name = DashletContent.createCriteria().get() {
            and {
                eq("holder", holder)
                eq("isVisible", true)
            }
        }.title

        return name
    }

    Map getFavouriteReport(Map params) {
        Map favouriteReportData = [:]
        favouriteReportData.active = DashletContent.find("from DashletContent d where d.holder.id = ?", [params.long("dashletId")])?.contentId.toLong()
        favouriteReportData.reportList = FavouriteReport.list()
        return favouriteReportData
    }

    boolean updateSoftDone(key, value) {
        return configService.update([[type: DomainConstants.SITE_CONFIG_TYPES.GET_STARTED_WIZARD, configKey: key, value: value]])
    }

    Map getQuickReports() {
        Map reports = [:]
        Map orders = Order.executeQuery("select new Map(sum(i.quantity * i.price - i.discount) as sale, sum(i.quantity) as item) from Order o inner join o.items i where o.orderStatus = :status", [status: DomainConstants.ORDER_STATUS.COMPLETED])[0]
        Long orderCount = Order.createCriteria().count {
            eq "orderStatus", DomainConstants.ORDER_STATUS.COMPLETED
        }
        reports.order_count = orderCount
        reports.total_costomers = Customer.createCriteria().count {
            eq "isInTrash", false
            eq "status", DomainConstants.CUSTOMER_STATUS.ACTIVE
        }
        reports.sales = orders.sale ?: 0
        reports.gross_sales = Order.executeQuery("select sum(shippingCost + shippingTax + handlingCost + totalSurcharge + (select sum(i.quantity * i.price + i.tax - i.discount) from o.items i)) from Order o where o.orderStatus = :status", [status: DomainConstants.ORDER_STATUS.COMPLETED])[0] ?: 0
        reports.total_item_sold = orders.item ?: 0
        try {
            reports.avarage_order_value = (orders.sale / orderCount)
        } catch (Exception e) {
            reports.avarage_order_value = 0
        }
        reports.cancelled_order = Order.createCriteria().count {
            eq "orderStatus", DomainConstants.ORDER_STATUS.CANCELLED
        }
        reports.pending_order = Order.createCriteria().count {
            eq "orderStatus", DomainConstants.ORDER_STATUS.PENDING
        }
        reports.new_product = productService.getProductsCount([isNew: true])
        reports.out_of_stock_product = productService.getProductsCount([stock: "out"])
        return reports
    }
}