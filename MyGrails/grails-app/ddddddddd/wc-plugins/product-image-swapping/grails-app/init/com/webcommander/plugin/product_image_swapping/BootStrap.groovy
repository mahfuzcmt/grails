package com.webcommander.plugin.product_image_swapping

import com.webcommander.admin.ConfigService
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.manager.HookManager
import com.webcommander.tenant.TenantContext
import com.webcommander.util.AppUtil
import com.webcommander.util.PluginDestroyUtil

class BootStrap {

    private final String PRODUCT_IMAGE_SWAPPING = "product_image_swapping"
    private final String product_image_swapping = "productImageSwapping"

    List domain_constants = [
            [constant:"SITE_CONFIG_TYPES", key: "PRODUCT_IMAGE_SWAPPING", value: PRODUCT_IMAGE_SWAPPING],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: PRODUCT_IMAGE_SWAPPING, value: true],
    ]

    def product_image_setting_data = [
            'enable_swapping' : 'false'
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        ConfigService.addTab(product_image_swapping, [
                url: "productImage/loadProductImageSettingView",
                message_key: "product.image.swapping",
                license: "allow_product_image_swapping_feature",
                ecommerce  : true
        ])
        if(SiteConfig.findAllByType(PRODUCT_IMAGE_SWAPPING).size() == 0) {
            product_image_setting_data.each { entry ->
                new SiteConfig(type: PRODUCT_IMAGE_SWAPPING, configKey: entry.key, value: entry.value).save()
            }
        }
    }

    def tenantDestroy = { tenant ->
        ConfigService.removeTab(product_image_swapping)
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeSiteConfig(PRODUCT_IMAGE_SWAPPING)
            DomainConstants.removeConstant(domain_constants)
        } catch (Exception e) {
            log.error "Could Not Deactivate Plugin product-image-swapping From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        TenantContext.eachParallelWithWait(tenantInit)

        HookManager.register("productViewClazz", { productViewClazz, config ->
            String swappingEnable = AppUtil.getConfig(PRODUCT_IMAGE_SWAPPING, "enable_swapping")
            if(swappingEnable == "true") {
                productViewClazz.add("image-swap")
            }
            return productViewClazz
        })
    }
}
