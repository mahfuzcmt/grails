package com.webcommander.plugin.product_quick_view

import com.webcommander.admin.ConfigService
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.manager.HookManager
import com.webcommander.tenant.TenantContext
import com.webcommander.util.AppUtil

class BootStrap {

    private static final PRODUCT_QUICK_VIEW = "productQuickView"

    List domain_constants = [
            [constant:"SITE_CONFIG_TYPES", key: "PRODUCT_QUICK_VIEW", value: "product_quick_view"],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: "product_quick_view", value: true],
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        ConfigService.addTab(PRODUCT_QUICK_VIEW, [
                url: "quickView/quickSettingView",
                message_key: "product.quick.view",
                license: "allow_product_quick_view_feature",
                ecommerce  : true
        ])
        if(!SiteConfig.countByType(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_QUICK_VIEW)) {
            new SiteConfig(type: DomainConstants.SITE_CONFIG_TYPES.PRODUCT_QUICK_VIEW, configKey: 'enable_quick_view', value: 'false').save()
        }
    }

    def tenantDestroy = { tenant ->
        ConfigService.removeTab(PRODUCT_QUICK_VIEW)
        DomainConstants.removeConstant(domain_constants)
    }

    def init = { servletContext ->
        TenantContext.eachParallelWithWait(tenantInit)
        HookManager.register("productViewClazz", { productViewClazz, config ->
            if(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_QUICK_VIEW, "enable_quick_view") == "true") {
                productViewClazz.add("product-quick-view")
            }
            return productViewClazz
        })
    }
}
