package com.webcommander.plugin.auto_product_load_on_scroll

import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.manager.HookManager
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil

import java.util.logging.Level

class BootStrap {

    private final String AUTO_PRODUCT_LOAD_ON_SCROLL = "auto-product-load-on-scroll"

    List domain_constants = [
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: "auto_product_load_on_scroll", value: true],
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        if(!SiteConfig.countByTypeAndConfigKey(DomainConstants.SITE_CONFIG_TYPES.CATEGORY_PAGE, "enable_load_more")) {
            new SiteConfig(type: DomainConstants.SITE_CONFIG_TYPES.CATEGORY_PAGE, configKey: "enable_load_more", value: "false").save()
            new SiteConfig(type: DomainConstants.SITE_CONFIG_TYPES.CATEGORY_PAGE, configKey: "initial_item", value: "10").save()
            new SiteConfig(type: DomainConstants.SITE_CONFIG_TYPES.CATEGORY_PAGE, configKey: "item_on_scroll", value: "5").save()
        }
    }

    def tenantDestroy = { tenant ->
        DomainConstants.removeConstant(domain_constants)
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeSiteConfig("category_page", "enable_load_more")
                    .removeSiteConfig("category_page", "initial_item")
                    .removeSiteConfig("category_page", "item_on_scroll")
        } catch (Exception e) {
            log.log Level.SEVERE, "Could Not Deactivate Plugin auto-product-load-on-scroll From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        TenantContext.eachParallelWithWait(tenantInit)

        HookManager.register("productWidgetConfigWithRequestContribution", { config ->
            if(config["enable_load_more"] == "true" && config["display-type"] != "scrollable") {
                config["max"] = config["initial_item"].toInteger()
            }
            return config
        });
        HookManager.register("productViewFilterWithRequestContributionInCategoryPage", { filterMap ->
            Map config = SiteConfig.findAllByType(DomainConstants.SITE_CONFIG_TYPES.CATEGORY_PAGE).collectEntries{[(it.configKey):it.value]}
            if(config["enable_load_more"] == "true" && config["display_type"] != "scrollable" && config["show_pagination"] == "none") {
                filterMap["max"] = config["initial_item"].toInteger()
            }
            return filterMap
        })
        HookManager.register("productViewClazz", { productViewClazz, config ->
            if(config.enable_load_more == "true" && config["display-type"] != "scrollable") {
                productViewClazz.add("auto_scroll_product")
            }
            return productViewClazz
        })
    }
}
