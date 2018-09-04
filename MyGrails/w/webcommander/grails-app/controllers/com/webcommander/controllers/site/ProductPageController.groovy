package com.webcommander.controllers.site

import com.webcommander.calculator.TaxCalculator
import com.webcommander.constants.DomainConstants
import com.webcommander.manager.HookManager
import com.webcommander.models.ProductData
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService
import grails.converters.JSON

class ProductPageController {
    ProductService productService

    def priceAndUnavailableMsg() {
        Long productId = params.long("productId");
        Product product = Product.proxy(productId)
        try {
            Map config = params.config ?: [:]
            ProductData data = productService.getProductData(product, config)
            Double displayPrice = data.priceToDisplay
            if(params.included) {
                double price = productService.getCombinationPrice(product, JSON.parse(params.included))
                Map taxConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.TAX)
                Boolean isInclusive = taxConfig.is_price_with_tax.toBoolean()
                Double tax = TaxCalculator.getTax(data.resolveTaxProfile(), price, isInclusive);
                if(isInclusive) {
                    price -= tax
                }
                displayPrice = taxConfig.show_price_with_tax.toBoolean() ? price + tax : price;
            }
            Map model = [status: "success", price: displayPrice, sale: data.previousPriceToDisplay, expect: data.previousPriceToDisplay,
                        stockWidget: wi.productwidget(type: "stockMark", product: product, productData: data), params: params,
                        priceWidget: wi.productwidget(type: "price", product: product, productData: data)]
            model = HookManager.hook("before-price-available-change", model, params)
            render(model as JSON)
        } catch(ApplicationRuntimeException t) {
            render([status: "error", message: g.message(code: "not.available")] as JSON)
        }
    }
}
