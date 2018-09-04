package com.webcommander.plugin.multi_level_pricing

import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.events.AppEventManager
import com.webcommander.manager.HookManager
import com.webcommander.models.Cart
import com.webcommander.models.CartItem
import com.webcommander.models.ProductData
import com.webcommander.models.ProductInCartBase
import com.webcommander.tenant.TenantContext
import com.webcommander.util.AppUtil
import com.webcommander.util.PluginDestroyUtil
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService
import grails.util.TypeConvertingMap

class BootStrap {

    private final String MULTI_LEVEL_PRICING = "multi_level_pricing"

    List domain_constants = [
            [constant:"SITE_CONFIG_TYPES", key: "MULTI_LEVEL_PRICING", value: MULTI_LEVEL_PRICING],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: "multi_level_pricing", value: true],
    ]

    Map initData = [
            is_enabled             : "false",
            lowest_or_highest_price: "lowest"
    ]
    
    MultiLevelPricingService multiLevelPricingService
    ProductService productService

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        if (SiteConfig.countByType(MULTI_LEVEL_PRICING) == 0) {
            initData.each { entry ->
                new SiteConfig(type: MULTI_LEVEL_PRICING, configKey: entry.key, value: entry.value).save()
            }
        }
    }

    def tenantDestroy = { tenant ->
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeSiteConfig(MULTI_LEVEL_PRICING)
            DomainConstants.removeConstant(domain_constants)
        }catch(Exception e) {
            log.error "Could Not Deactivate Plugin multi-level-pricing From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        TenantContext.eachParallelWithWait(tenantInit)

        HookManager.register("savePriceNQuantity", { Map resp, TypeConvertingMap params ->
            def configs = AppUtil.getConfig(MULTI_LEVEL_PRICING);
            if(configs.is_enabled == "true"){
                resp.success = multiLevelPricingService.save(params)
            }
            return resp
        })

        HookManager.register("populateCartItem",{ CartItem cartItem, Integer quantity, ProductData productData ->
            Long variationId = productData.hasProperty('productVariationId') ? productData.productVariationId : null
            Double price = multiLevelPricingService.getMultiLevelPriceForProduct(productData.id, variationId)
            ProductMultiLevelPrice multiLevelPrice
            if(variationId){
                multiLevelPrice = ProductMultiLevelPrice.findByProductAndVariationId(Product.get(productData.id),variationId)
            } else {
                multiLevelPrice = ProductMultiLevelPrice.findByProduct(Product.get(productData.id))
            }
            Map configs = AppUtil.getConfig(MULTI_LEVEL_PRICING);
            if(configs.is_enabled == "true" && multiLevelPrice && multiLevelPrice.isActive && AppUtil.loggedCustomer && price) {

                // TODO need to deal cache for product data
                productData.basePrice = price
                productData.calculatePrice()

                cartItem.unitPrice = productData.priceToDisplay - productData.tax
                cartItem.baseTotal = cartItem.unitPrice * quantity

                cartItem.unitTax = productData.tax

                cartItem.object.effectivePrice = productData.basePrice
            }
            return cartItem
        });


        AppEventManager.on("before-refresh-cart", { Cart cart ->
            List<CartItem> cartItems = cart.cartItemList
            cartItems.each { cartItem->
                def productData = cartItem.object.product
                Long variationId = productData.hasProperty('productVariationId') ? productData.productVariationId : null
                Double price = multiLevelPricingService.getMultiLevelPriceForProduct(productData.id, variationId)
                ProductMultiLevelPrice multiLevelPrice
                if(variationId){
                    multiLevelPrice = ProductMultiLevelPrice.findByProductAndVariationId(Product.get(productData.id),variationId)
                } else {
                    multiLevelPrice = ProductMultiLevelPrice.findByProduct(Product.get(productData.id))
                }
                Map configs = AppUtil.getConfig(MULTI_LEVEL_PRICING)
                def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.TAX)
                if(configs.is_enabled == "true" &&  multiLevelPrice && multiLevelPrice.isActive && AppUtil.loggedCustomer && price) {
                    productData.basePrice = price
                    productData.calculatePrice()
                    cartItem.unitPrice = config.show_price_with_tax == "true" ? productData.priceToDisplay - productData.tax : productData.priceToDisplay
                    cartItem.baseTotal = cartItem.unitPrice * cartItem.quantity
                    cartItem.unitTax = productData.tax
                    cartItem.object.effectivePrice = productData.basePrice
                }
            }
        })
    }
}
