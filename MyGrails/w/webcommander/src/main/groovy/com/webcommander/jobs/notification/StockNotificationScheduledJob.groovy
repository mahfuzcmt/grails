package com.webcommander.jobs.notification

import com.webcommander.annotations.event.Event
import com.webcommander.annotations.event.EventHandler
import com.webcommander.tenant.TenantContext
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.ProductService
import grails.util.Holders
import org.springframework.stereotype.Component

@Component
@EventHandler
class StockNotificationScheduledJob {

    static ProductService _productService

    static getProductService() {
        return _productService ?: (_productService = Holders.grailsApplication.mainContext.getBean(ProductService))
    }

    @Event("clock-daily-00-10-trigger")
    def execute() {
        if (!Holders.servletContext || !Holders.servletContext.initialized) {
            return
        }
        TenantContext.eachParallel {
            AppUtil.initialDummyRequest()
            productService.checkToSendStockReport()
        }
    }
}
