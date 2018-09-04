package com.webcommander.plugin.variation

import com.webcommander.AppResourceTagLib
import com.webcommander.common.ImageService
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.design.ProductWidgetService
import com.webcommander.events.AppEventManager
import com.webcommander.installation.template.TemplateContent
import com.webcommander.manager.HookManager
import com.webcommander.models.ProductData
import com.webcommander.models.ProductInCart
import com.webcommander.plugin.variation.factory.VariationObjectsProducer
import com.webcommander.plugin.variation.mixin_service.ProductWidgetService as VWS
import com.webcommander.tenant.TenantContext
import com.webcommander.util.AppUtil
import com.webcommander.util.PluginDestroyUtil
import com.webcommander.webcommerce.OrderItem
import com.webcommander.webcommerce.Product
import grails.util.Holders

class BootStrap {
    VariationService variationService
    private static final PLUGIN_NAME = "variation"

    List site_config_constants = [
            [constant: DomainConstants.SITE_CONFIG_TYPES.PRODUCT, key: "enable_matrix_view", value:"false"],
            [constant: DomainConstants.SITE_CONFIG_TYPES.PRODUCT, key: "enable_flate_chooser", value:"false"],
            [constant: DomainConstants.SITE_CONFIG_TYPES.CATEGORY_PAGE, key: "show_variation_combination", value:"true"]
    ]

    List domain_constants = [
            [constant: "PRODUCT_WIDGET_TYPE", key: "VARIATION", value: PLUGIN_NAME],
            [constant: "PRODUCT_EXPORT_MANDATORY_FIELDS", key: "baseProduct", value: "baseProduct"],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: PLUGIN_NAME, value: true],
    ]

    List named_constants = [
            [constant: "PRODUCT_WIDGET_MESSAGE_KEYS", key: PLUGIN_NAME + ".title", value: "variation.widget"],
            [constant: "PRODUCT_WIDGET_MESSAGE_KEYS", key: PLUGIN_NAME + ".label", value: PLUGIN_NAME],
            [constant: "PRODUCT_IMPORT_EXTRA_FIELDS", key: "baseProduct", value: "base.product"],
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        NamedConstants.addConstant(named_constants)

        site_config_constants.each { it ->
            if (!SiteConfig.findAllByTypeAndConfigKey(it.constant, it.key)) {
                new SiteConfig(type: it.constant, configKey: it.key, value: it.value).save()
                AppUtil.clearConfig it.constant
            }
        }
    }

    def tenantDestroy = { tenant ->
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeAutoGeneratePageWidget(NamedConstants.AUTO_GENERATED_PAGE_WIDGET.PRODUCT_WIDGET, PLUGIN_NAME)
            util.removeSiteConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT, variation_site_config.keySet())
            util.removeWidget(PLUGIN_NAME)
            VariationResourceTagLib.RESOURCES_PATH.each { resource ->
                util.deleteResourceFolders(resource.value)
            }
            DomainConstants.removeConstant(domain_constants)
            NamedConstants.removeConstant(named_constants)
        } catch (Exception e) {
            log.error "Could Not Deactivate Plugin variation From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        Holders.grailsApplication.mainContext.getBean(AppResourceTagLib).metaClass.mixin VariationResourceTagLib
        Holders.grailsApplication.mainContext.getBean(ProductWidgetService).metaClass.mixin VWS

        ImageService.RESIZABLE_IMAGE_SIZES["variation-image"] = [16: [16, 16]]
        TenantContext.eachParallelWithWait(tenantInit)
        Product.metaClass.with {
            hasVariation = {
                Long id = delegate.id
                return ProductVariation.createCriteria().count {
                    eq "product.id", id
                    eq("active", true)
                } > 0
            }
        }
        ProductData.metaClass.with {
            hasVariation = {
                Long id = delegate.id
                return ProductVariation.createCriteria().count {
                    eq "product.id", id
                    eq("active", true)
                } > 0
            }
        }

        HookManager.register("cached-product-data", { ProductData data, Product product, Map config ->
            return VariationObjectsProducer.getVariationProductData(product, config)
        });

        HookManager.register("get-product-in-cart", { ProductInCart productCart, ProductData productData, Map params ->
            return VariationObjectsProducer.getVariationProductInCart(productData, params)
        })

        HookManager.register("get-order-item-variation-id", { OrderItem orderItem ->
            OrderVariationItem orderVariationItem = OrderVariationItem.createCriteria().get {
                eq("orderItem", orderItem)
            }
            return orderVariationItem.variationId
        })

        AppEventManager.on("variation-type-update", { id ->
            TemplateContent.where {
                contentType == "variation_type"
                contentId == id
            }.deleteAll()
        });

        HookManager.register("api-product-details-marshaller-config", { Map config, Product product ->
            config.marshallerInclude.add("productVariation")
            return config;
        })

        Product.fieldMarshaller["productVariation"] = { Product product ->
            return variationService.getDataForProductInfoAPI(product)
        }

        HookManager.register("modify-cart-item-data-for-api", { Map data, Product product ->
            if (variationService.hasVariation(product.id) && variationService.allowed(product.id)) {
                if (data.hasProperty('productVariationId') && data.productVariationId) {
                    data.config = [variation: data.productVariationId]
                } else {
                    data.config = [:]
                }
                data.remove("productVariationId")
            }
            return data
        });

        HookManager.register("productConfigForFrontEndAPI", { Map config, Map productConfig ->
            config.variation_option_view = productConfig.enable_flate_chooser == "true" ? "flat" : "dropdown"
            return config
        })

        HookManager.register("getCartParamsFromOrderItem", { Map params, OrderItem orderItem ->
            if (orderItem.productType == NamedConstants.CART_OBJECT_TYPES.PRODUCT) {
                params.config = [orderItemId: orderItem.id]
            }
            return params
        })

    }
}
