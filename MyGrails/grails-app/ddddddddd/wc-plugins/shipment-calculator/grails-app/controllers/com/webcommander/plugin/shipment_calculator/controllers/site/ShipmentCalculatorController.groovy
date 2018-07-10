package com.webcommander.plugin.shipment_calculator.controllers.site

import com.webcommander.admin.AdministrationService
import com.webcommander.admin.Country
import com.webcommander.admin.State
import com.webcommander.calculator.ShippingCalculator
import com.webcommander.constants.DomainConstants
import com.webcommander.manager.CartManager
import com.webcommander.models.AddressData
import com.webcommander.models.Cart
import com.webcommander.models.CartItem
import com.webcommander.models.ProductData
import com.webcommander.plugin.shipment_calculator.manager.ShipmentCalculatorCartManager
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService
import com.webcommander.webcommerce.ShippingClass
import grails.converters.JSON

class ShipmentCalculatorController {
    AdministrationService administrationService
    ProductService productService

    def renderPopup() {
        Map model = [:]
        model.config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SHIPPING)
        model.defaultCountryId = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "default_country").toLong()
        model.defaultStateId = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "default_state").toLong()
        model.states = administrationService.getStatesForCountry(model.defaultCountryId)
        render(view: "/plugins/shipment_calculator/site/popup", model: model)
    }

    def calculate() {
        String page = params.page
        Cart cart
        AddressData addressData = new AddressData()
        addressData.countryId = params.long("country.id")
        addressData.countryCode = Country.get(addressData.countryId)?.code
        addressData.stateId = params["state.id"] ? params.long("state.id") : null
        addressData.stateCode = addressData.stateId ? State.get(addressData.stateId).code : null
        addressData.postCode = params.postCode
        addressData.city = params.city
        if (page == 'product') {
            params.combination = params.combination ?: params["combination[]"]
            Long productId = params.long("productId")
            Integer quantity = params.int("quantity")
            Product product = Product.get(productId)
            ProductData productData = productService.getProductData(product, params.config ?: [:])
            cart = new Cart()
            try {
                CartItem item = ShipmentCalculatorCartManager.populateCartItemForProduct(productData, quantity, params)
                cart.cartItemList.add(item)
            } catch (Exception e) {
                render([status: "error", message: e.message] as JSON)
                return
            }
        } else {
            cart = CartManager.getCart(session.id, true)
        }
        String error = null
        if(!cart || cart.cartItemList.size() == 0) {
            error = "your.shopping.cart.empty"
        }
        def shippingCostMaps = [:]
        if(error == null) {
            shippingCostMaps = ShippingCalculator.getShippingCost(cart, addressData)
        }
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SHIPPING)
        List shippingClasses = ShippingClass.list()
        if(config['enable_shipping_class'] == "true" && shippingClasses) {
            shippingCostMaps = [:]
            shippingClasses.each { clazz->
                Map shippingMap = [shipping: 0.0, handling: 0.0]
                cart.cartItemList.each {
                    Map costMap = it.shippingCostMaps[clazz.id]
                    if(!it.shippingCostMaps || it.shippingCostMaps.isEmpty() || !costMap) {
                        shippingMap.shipping = null
                        shippingMap.handling = 0
                    } else if(shippingMap.shipping != null) {
                        shippingMap.shipping += costMap.shipping
                        shippingMap.handling += costMap.handling
                    }
                }
                shippingCostMaps[clazz.id] = shippingMap
            }
        }

        def html = g.include(view:  "/plugins/shipment_calculator/site/shipmentCalculator.gsp", model: [shippingCostMaps: shippingCostMaps, cart: cart, config: config, error: error, shippingClasses: shippingClasses])
        render([status: "success", html: html.toString()] as JSON)
    }
}
