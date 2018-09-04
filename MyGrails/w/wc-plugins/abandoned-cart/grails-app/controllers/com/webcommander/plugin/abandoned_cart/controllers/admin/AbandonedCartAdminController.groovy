package com.webcommander.plugin.abandoned_cart.controllers.admin

import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.abandoned_cart.AbandonedCart
import com.webcommander.plugin.abandoned_cart.AbandonedCartService
import com.webcommander.util.AppUtil
import grails.converters.JSON

class AbandonedCartAdminController {
    AbandonedCartService abandonedCartService
    CommonService commonService

    def loadAppView() {
        params.max = params.max ?: "10";
        Integer count = abandonedCartService.getAbandonedCartCount(params)
        List<AbandonedCart> abandonedCartList = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.max = max;
            params.offset = offset;
            abandonedCartService.getAbandonedCart(params);
        }
        render(view: "/plugins/abandoned_cart/admin/appView", model: [carts: abandonedCartList, count: count]);
    }

    def advanceFilter() {
        render(view: "/plugins/abandoned_cart/admin/filter", model: [:]);
    }

    def sendNotification() {
        abandonedCartService.sendNotification(params)
        render([status: "success", message: g.message(code: "notification.successfully.sent")] as JSON)
    }

    def sendBatchNotification() {
        abandonedCartService.sendBatchNotification(params)
        render([status: "success", message: g.message(code: "notification.successfully.sent")] as JSON)
    }

    def viewCart() {
        AbandonedCart cart = AbandonedCart.get(params.id)
        render(view: "/plugins/abandoned_cart/admin/view", model: [cart: cart])
    }

    def disableNotification() {
        Boolean success = abandonedCartService.changeNotification(params)
        if(success) {
            render([status: "success", message: g.message(code: "notification.successfully." + (params.disable ? "disabled" : "enabled"))] as JSON)
        } else {
            render([status: "error", message: g.message(code: "notification.status.change.fail")] as JSON)
        }
    }

    def config() {
        Map config =  AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ABANDONED_CART)
        def globalTick = AppUtil.getAppConfig("webcommander.scheduler.tick", 600000)
        render(view: "/plugins/abandoned_cart/admin/abandonedCartSetting", model: [config: config, tick: globalTick])
    }
}



