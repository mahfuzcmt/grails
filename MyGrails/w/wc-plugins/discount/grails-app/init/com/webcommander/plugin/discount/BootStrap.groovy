package com.webcommander.plugin.discount

import com.webcommander.acl.Permission
import com.webcommander.acl.RolePermission
import com.webcommander.admin.ConfigService
import com.webcommander.admin.Role
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.design.ProductWidgetService
import com.webcommander.events.AppEventManager
import com.webcommander.models.Cart
import com.webcommander.plugin.discount.mixin_service.ProductWidgetService as DWS
import com.webcommander.plugin.discount.processor.DiscountProcessor
import com.webcommander.plugin.discount.processor.ProcessorFactory
import com.webcommander.plugin.discount.util.DiscountDataUtil
import com.webcommander.tenant.TenantContext
import com.webcommander.util.AppUtil
import com.webcommander.util.PluginDestroyUtil
import com.webcommander.webcommerce.Order
import grails.util.Holders

import java.util.logging.Level

class BootStrap {

    private static final DISCOUNT = "discount"

    DiscountService discountService

    List domain_constants = [
            [constant: "PRODUCT_WIDGET_TYPE", key: "DISCOUNT", value: DISCOUNT],
            [constant: "SITE_CONFIG_TYPES", key: "DISCOUNT", value: DISCOUNT],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: "discount", value: true],
    ]

    List named_constants = [
            [constant: "PRODUCT_WIDGET_MESSAGE_KEYS", key: DISCOUNT + ".title", value: "discount.widget"],
            [constant: "PRODUCT_WIDGET_MESSAGE_KEYS", key: DISCOUNT + ".label", value: "discount"],
    ]

    Map initData = [
            show_coupon_in_cart_page    : "true",
            show_coupon_in_checkout_page: "true",
            coupon_code_prefix          : "DSCP-"
    ]

    List permissions = [
            ["view.list", false],
    ]

    def tenantInit = { tenant ->

        DomainConstants.addConstant(domain_constants)
        NamedConstants.addConstant(named_constants)

        ConfigService.addTab(DISCOUNT, [
                url        : "discount/loadConfig",
                message_key: "discount",
                ecommerce  : true
        ])
        if (SiteConfig.countByType(DISCOUNT) == 0) {
            initData.each { entry ->
                new SiteConfig(type: DISCOUNT, configKey: entry.key, value: entry.value).save()
            }
        }

        if (!Permission.findByType(DISCOUNT)) {
            Role role = Role.findByName("Admin")
            permissions.each { entry ->
                Permission permission = new Permission(name: entry[0], label: entry[0], applicableOnEntity: entry[1], type: "discount").save()
                new RolePermission(role: role, permission: permission, isAllowed: true).save()
            }
        }
    }

    def tenantDestroy = { tenant ->
        ConfigService.removeTab(DISCOUNT)

        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removePermission(DISCOUNT)
            util.removeAutoGeneratePageWidget(NamedConstants.AUTO_GENERATED_PAGE_WIDGET.PRODUCT_WIDGET, DISCOUNT)
            DomainConstants.removeConstant(domain_constants)
            NamedConstants.removeConstant(named_constants)
        } catch (Exception e) {
            log.log Level.SEVERE, "Could Not Deactivate Plugin discount From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        Holders.grailsApplication.mainContext.getBean(ProductWidgetService).metaClass.mixin DWS

        TenantContext.eachParallelWithWait(tenantInit)

        AppEventManager.on("before-cart-details-load cart-total-updated", { Object param1, Object param2 ->

            Cart cart = param1 instanceof Cart ? param1 : param2

            if (!cart) {
                return
            }

            DiscountProcessor processor = ProcessorFactory.getDiscountProcessor()

            Map context = [:]
            context.cart = cart

            processor.process(context)

        });

        AppEventManager.on("order-confirm", { Cart cart ->
            def session = AppUtil.session;
            Order order = Order.get(cart.orderId);
            if (order && cart.selectedDiscountData) {

                Map context = [:]

                context.data = cart.selectedDiscountData
                context.orderId = cart.orderId
                context.customerId = order.customerId
                context.appliedCouponCode = DiscountDataUtil.getEffectiveCouponCode()

                discountService.saveDiscountUsage(context)

                AppUtil.session.effectiveCouponCode = null

            }
        });

        AppEventManager.on("customer-logout") { customerId ->
            AppUtil.session.effectiveCouponCode = null
        }

    }
}
