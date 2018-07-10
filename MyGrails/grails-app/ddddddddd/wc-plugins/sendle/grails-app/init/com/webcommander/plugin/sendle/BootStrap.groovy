package com.webcommander.plugin.sendle

import com.webcommander.admin.ConfigService
import com.webcommander.calculator.ApiShippingCalculatorService
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.events.AppEventManager
import com.webcommander.manager.HookManager
import com.webcommander.models.AddressData
import com.webcommander.plugin.sendle.communicator.SendleCommunicator
import com.webcommander.tenant.TenantContext
import com.webcommander.util.AppUtil
import com.webcommander.util.PluginDestroyUtil
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ShippingCondition
import com.webcommander.webcommerce.ShippingPolicy
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap
import com.webcommander.plugin.sendle.calculator.ApiShippingCalculator as ASC

class BootStrap {

    private final String SENDLE = "sendle"

    private static final Double DOMESTIC_SHIPPING_WEIGHT_LIMIT = 25
    private static final Double DOMESTIC_SHIPPING_VOLUME_LIMIT = 100000
    private static final Double DOMESTIC_SHIPPING_SIDE_LIMIT = 120

    private static final Double INTERNATIONAL_SHIPPING_WEIGHT_LIMIT = 20
    private static final Double INTERNATIONAL_SHIPPING_VOLUME_LIMIT = 125000
    private static final Double INTERNATIONAL_SHIPPING_SIDE_LIMIT_LOWER = 60
    private static final Double INTERNATIONAL_SHIPPING_SIDE_LIMIT_UPPER = 105
    private static final Double INTERNATIONAL_SHIPPING_VALUE_LIMIT = 2000
    private static final Double INTERNATIONAL_SHIPPING_WEIGHT_LIMIT_FOR_LOWER_SIDE_LIMIT = 1.5

    List domain_constants = [
            [constant:"WIDGET_TYPE", key: "SENDLE", value: SENDLE],
            [constant:"SITE_CONFIG_TYPES", key: "SENDLE", value: SENDLE],
    ]

    List named_constants = [
            [constant:"SHIPPING_API", key: SENDLE, value:"sendle"],
            [constant:"SHIPPING_METHOD", key: SENDLE, value:"sendle"],
    ]

    Map config = [
            sendleId: "",
            apiKey: "",
            mode: "test",
    ]

    List site_config_constants = [
            [constant:SENDLE, key: "DOMESTIC_SHIPPING_WEIGHT_LIMIT", value: DOMESTIC_SHIPPING_WEIGHT_LIMIT],
            [constant:SENDLE, key: "DOMESTIC_SHIPPING_VOLUME_LIMIT", value: DOMESTIC_SHIPPING_VOLUME_LIMIT],
            [constant:SENDLE, key: "DOMESTIC_SHIPPING_SIDE_LIMIT", value: DOMESTIC_SHIPPING_SIDE_LIMIT],
            [constant:SENDLE, key: "INTERNATIONAL_SHIPPING_WEIGHT_LIMIT", value: INTERNATIONAL_SHIPPING_WEIGHT_LIMIT],
            [constant:SENDLE, key: "INTERNATIONAL_SHIPPING_VOLUME_LIMIT", value: INTERNATIONAL_SHIPPING_VOLUME_LIMIT],
            [constant:SENDLE, key: "INTERNATIONAL_SHIPPING_SIDE_LIMIT_LOWER", value: INTERNATIONAL_SHIPPING_SIDE_LIMIT_LOWER],
            [constant:SENDLE, key: "INTERNATIONAL_SHIPPING_SIDE_LIMIT_UPPER", value: INTERNATIONAL_SHIPPING_SIDE_LIMIT_UPPER],
            [constant:SENDLE, key: "INTERNATIONAL_SHIPPING_VALUE_LIMIT", value: INTERNATIONAL_SHIPPING_VALUE_LIMIT],
            [constant:SENDLE, key: "INTERNATIONAL_SHIPPING_WEIGHT_LIMIT_FOR_LOWER_SIDE_LIMIT", value: INTERNATIONAL_SHIPPING_WEIGHT_LIMIT_FOR_LOWER_SIDE_LIMIT],
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        NamedConstants.addConstant(named_constants)
        ConfigService.addTab(SENDLE, [
                url: "sendle/loadConfig",
                message_key: "sendle"
        ])
        if(!SiteConfig.findByType(SENDLE)) {
            config.each {
                new SiteConfig(type: SENDLE, configKey: it.key, value: it.value).save()
            }
        }
        site_config_constants.each { it ->
            if (!SiteConfig.findAllByTypeAndConfigKey(it.constant, it.key)) {
                new SiteConfig(type: it.constant, configKey: it.key, value: it.value).save()
                AppUtil.clearConfig it.constant
            }
        }
    }

    def tenantDestroy = { tenant ->
        ConfigService.removeTab(SENDLE)
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeSiteConfig(SENDLE)
            NamedConstants.removeConstant(named_constants)
            DomainConstants.removeConstant(domain_constants)
            site_config_constants.each { it ->
                util.removeSiteConfig(it.constant, it.key)
            }
        } catch(Exception e) {
            log.error "Could Not Deactivate Plugin sendle From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        Holders.grailsApplication.mainContext.getBean(ApiShippingCalculatorService).metaClass.mixin(ASC);
        TenantContext.eachParallelWithWait(tenantInit)

        HookManager.register("${SENDLE}-save-shipment", { def orderResponse, AddressData receiver, Product product, String pickupDate, String description, String receiverInstruction ->
            orderResponse =  SendleCommunicator.placeOrder(receiver, product, pickupDate, description, receiverInstruction)
            return orderResponse
        })

        HookManager.register("before-shipping-policy-save", { ShippingPolicy policy ->
            GrailsParameterMap params = AppUtil.params
            if(policy.policyType == DomainConstants.SHIPPING_POLICY_TYPE.API && params.api == DomainConstants.SHIPPING_API.SENDLE) {
                ShippingCondition condition = policy.conditions[0];
                condition.apiServiceType = params.sendleServiceCode
                policy.save(flush: true);
            }
        })
        Closure deleteExtensionRelation = { ShippingPolicy policy ->
            policy.conditions.each { ShippingCondition condition ->

            }
        }
        AppEventManager.on("before-shippingPolicy-delete", { Long id, at2_replay ->
            ShippingPolicy policy = ShippingPolicy.get(id)
            deleteExtensionRelation(policy)
        });
        AppEventManager.on("before-shippingPolicy-update", {ShippingPolicy policy ->
            deleteExtensionRelation(policy)
        });

    }
}
