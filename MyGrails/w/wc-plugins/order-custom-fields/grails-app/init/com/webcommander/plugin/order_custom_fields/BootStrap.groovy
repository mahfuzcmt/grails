package com.webcommander.plugin.order_custom_fields

import com.webcommander.constants.NamedConstants
import com.webcommander.events.AppEventManager
import com.webcommander.manager.HookManager
import com.webcommander.tenant.TenantContext
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Order
import grails.gsp.PageRenderer
import grails.util.Holders
import com.webcommander.constants.DomainConstants


class BootStrap {

    List domain_constants = [
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: "order_custom_fields", value: true],
    ]

    List named_constants = [
            [constant:"CHECKOUT_PAGE_STEP", key: "ORDER_CUSTOM_FIELDS", value:"custom"],
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        NamedConstants.addConstant(named_constants)
    }

    def tenantDestroy = { tenant ->
        DomainConstants.removeConstant(domain_constants)
        NamedConstants.removeConstant(named_constants)
    }

    def init = { servletContext ->
        TenantContext.eachParallelWithWait(tenantInit)
        PageRenderer renderer = Holders.grailsApplication.mainContext.getBean(PageRenderer);
        AppEventManager.on("order-confirm", { cart ->
            def session = AppUtil.session
            Map params = session.order_custom_fields
            Order order = Order.proxy(cart.orderId)
            params.each { k, v ->
                if(v instanceof Map) {
                    return;
                }
                new OrderCustomData(order: order, fieldName: k, fieldValue: (v instanceof String[] || v instanceof List) ? v.join(", ") : v).save()
            }
        });
        HookManager.register("order-mail-macros", { Map macros, Long orderId, String identifier ->
            if(identifier == "create-order") {
                List<OrderCustomData> customData = OrderCustomData.where {
                    eq("order.id", orderId)
                }.list();
                StringWriter writer = new StringWriter();
                renderer.renderTo([view: "/plugins/order_custom_fields/orderCheckoutFieldReplaceMacro", model: [customData: customData]], writer);
                macros.custom_checkout_fields = writer.toString();
                return macros;
            }
        })
    }
}