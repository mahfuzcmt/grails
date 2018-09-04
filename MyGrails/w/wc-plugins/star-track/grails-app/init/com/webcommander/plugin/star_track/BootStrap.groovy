package com.webcommander.plugin.star_track

import com.webcommander.admin.ConfigService
import com.webcommander.calculator.ApiShippingCalculatorService
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.events.AppEventManager
import com.webcommander.manager.HookManager
import com.webcommander.plugin.star_track.calculator.ApiShippingCalculator as ASC
import com.webcommander.tenant.TenantContext
import com.webcommander.util.AppUtil
import com.webcommander.util.PluginDestroyUtil
import com.webcommander.webcommerce.ShippingCondition
import com.webcommander.webcommerce.ShippingPolicy
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap

class BootStrap {

    private final String STAR_TRACK = "star_track"

    List domain_constants = [
            [constant:"SITE_CONFIG_TYPES", key: "STAR_TRACK", value: STAR_TRACK],
            [constant:"SHIPPING_API", key: "STAR_TRACK", value: "starTrack"],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: "star_track", value: true],
    ]

    List named_constants = [
            [constant:"SHIPPING_API", key: "starTrack", value:"star.track"],
    ]

    Map config = [
            mode: "test",
            source: "TEAM",
            account_no: "11112222",
            user_access_key: "30405060708090",
            user_name: "TAY00002",
            password: "Tay12345",
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        NamedConstants.addConstant(named_constants)
        ConfigService.addTab(STAR_TRACK, [
                url: "starTrack/loadConfig",
                message_key: "star.track"
        ])
        if(!SiteConfig.findByType(STAR_TRACK)) {
            config.each {
                new SiteConfig(type: STAR_TRACK, configKey: it.key, value: it.value).save()
            }
        }
    }

    def tenantDestroy = { tenant ->
        ConfigService.removeTab(STAR_TRACK)
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeSiteConfig(STAR_TRACK)
            NamedConstants.removeConstant(named_constants)
            DomainConstants.removeConstant(domain_constants)
        } catch(Exception e) {
            log.error "Could Not Deactivate Plugin star-track From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        Holders.grailsApplication.mainContext.getBean(ApiShippingCalculatorService).metaClass.mixin(ASC);
        TenantContext.eachParallelWithWait(tenantInit)

        HookManager.register("before-shipping-policy-save", {ShippingPolicy policy ->
            GrailsParameterMap params = AppUtil.params
            if(policy.policyType == DomainConstants.SHIPPING_POLICY_TYPE.API && params.api == DomainConstants.SHIPPING_API.STAR_TRACK) {
                ShippingCondition condition = policy.conditions[0];
                condition.apiServiceType = params.starTrackServiceCode
                policy.save(flush: true);
                ShippingPolicyExtension extension = ShippingPolicyExtension.findOrCreateByShippingCondition(condition)
                extension.includeFuelSurcharge = params.includeFuelSurcharge.toBoolean()
                extension.includeSecuritySurcharge = params.includeSecuritySurcharge.toBoolean()
                extension.includeTransitWarranty = params.includeTransitWarranty.toBoolean()
                extension.transitWarrantyValue = extension.includeTransitWarranty ? params.double("transitWarrantyValue") : null;
                extension.save();
            }
        });
        Closure deleteExtensionRelation = { ShippingPolicy policy ->
            policy.conditions.each { ShippingCondition condition ->
                ShippingPolicyExtension extension = ShippingPolicyExtension.findByShippingCondition(condition)
                if (extension) {
                    extension.delete()
                }
            }
        }
        AppEventManager.on("before-shippingPolicy-delete", {Long id, at2_replay ->
            ShippingPolicy policy = ShippingPolicy.get(id)
            deleteExtensionRelation(policy)
        });
        AppEventManager.on("before-shippingPolicy-update", {ShippingPolicy policy ->
            deleteExtensionRelation(policy)
        });

    }
}
