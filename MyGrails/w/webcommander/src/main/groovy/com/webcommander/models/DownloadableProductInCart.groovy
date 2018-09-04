package com.webcommander.models

import com.webcommander.throwables.CartManagerException
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService
import grails.converters.JSON
import grails.util.Holders
import grails.util.TypeConvertingMap
import org.springframework.beans.BeanUtils

/**
 * Created by sajedur on 10-12-2014.
 */
class DownloadableProductInCart implements TaxableCartObject, ProductInCartBase {

    private static ProductService _productService;
    private ProductService getProductService() {
        if(_productService) {
            return _productService
        } else {
            return _productService = Holders.applicationContext.getBean("productService")
        }
    }

    public DownloadableProductInCart(ProductData data, TypeConvertingMap params) {
        product = data
        requestedParams = new TypeConvertingMap(new LinkedHashMap(params))
        if(product.isCombined && !product.isCombinationPriceFixed && product.isCombinationQuantityFlexible) {
            includedProductQuantityMap = JSON.parse(params.included)
        }
        updateProps()
    }

    public Boolean equals(ProductData data, List variation1 = null, List variation2 = null) {
        return this.id == data.id
    }
}
