package com.webcommander.models

import com.webcommander.constants.DomainConstants
import com.webcommander.throwables.CartManagerException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.CombinedProduct
import com.webcommander.webcommerce.ProductService
import com.webcommander.webcommerce.ShippingProfile
import grails.converters.JSON
import grails.util.TypeConvertingMap

/**
 * Created by zobair on 24/04/2014.*/
class ProductInCart implements ShippableCartObject, TaxableCartObject, ProductInCartBase {
    public static final long serialVersionUID = 222222222222;

    protected static ProductService _productService;
    protected static ProductService getProductService() {
        if(_productService) {
            return _productService
        } else {
            return _productService = ProductService.getInstance()
        }
    }

    public ProductInCart(ProductData data, TypeConvertingMap params) {
        product = data
        isShippable = !data.isVirtual
        requestedParams = new TypeConvertingMap(new LinkedHashMap(params))
        if(product.isCombined && !product.isCombinationPriceFixed && product.isCombinationQuantityFlexible) {
            includedProductQuantityMap = JSON.parse(params.included)
        }
        updateProps()
    }

    @Override
    ShippingProfile resolveShippingProfile() {
        return product.resolveShippingProfile()
    }
}
