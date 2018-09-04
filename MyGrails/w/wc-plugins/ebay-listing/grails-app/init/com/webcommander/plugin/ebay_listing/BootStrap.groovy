package com.webcommander.plugin.ebay_listing

import com.webcommander.admin.ConfigService
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil
import grails.util.Holders
import groovy.util.logging.Log

import java.util.logging.Level

@Log
class BootStrap {
    public final String EBAY_LISTING = "ebay_listing"
    public final String EBAY = "ebay"

    List domain_constants = [
            [constant:"SITE_CONFIG_TYPES", key: "EBAY", value: EBAY_LISTING],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: "ebay_listing", value: true],
    ]

    Map initConfig = SiteConfig.INITIAL_DATA[EBAY_LISTING] = [
        mode: "sandbox",
        ebay_site: 15,
        devid: "",
        appid: "",
        certid: "",
        user_token: ""
    ]
    
    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        ConfigService.addTab(EBAY, [
            url: "ebayListingAdmin/loadSettings",
            message_key: "ebay.listing",
            license: "allow_ebay_feature",
            ecommerce  : true
        ])
        if(SiteConfig.countByType(EBAY_LISTING) == 0) {
            initConfig.each {
                new SiteConfig(type: EBAY_LISTING, configKey: it.key, value: it.value).save()
            }
        }
    }

    def tenantDestroy = { tenant->
        PluginDestroyUtil destroyUtil = new PluginDestroyUtil()
        try {
            ConfigService.removeTab(EBAY)
            destroyUtil.removeSiteConfig(EBAY_LISTING)
            DomainConstants.removeConstant(domain_constants)
        } catch (Exception e) {
            log.log Level.SEVERE, "Could Not Deactivate Plugin ebay-listing From Tenant $tenant", e
            throw e
        } finally {
            destroyUtil.closeConnection()
        }
    }

    def init = { servletContext ->
        TenantContext.eachParallelWithWait(tenantInit)
        EbayListingService ebayListingService = Holders.grailsApplication.mainContext.getBean(EbayListingService)
        ebayListingService.startScheduler()
    }
}
