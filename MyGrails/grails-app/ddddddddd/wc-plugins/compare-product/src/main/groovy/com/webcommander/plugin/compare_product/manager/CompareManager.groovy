package com.webcommander.plugin.compare_product.manager

import com.webcommander.constants.DomainConstants
import com.webcommander.models.ProductData
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService
import grails.util.Holders

import java.util.concurrent.ConcurrentHashMap
import com.webcommander.util.AppUtil

/**
 * Created by sajed on 5/22/2014.
 */
class CompareManager {
    private static ProductService _productService;
    private static getProductService() {
        if(_productService) {
            return _productService
        } else {
            return _productService = Holders.applicationContext.getBean("productService")
        }
    }

    public static ProductData addToCompare(productId) {
        def session = AppUtil.session
        if (!session.compare) {
            session.compare = []
        }
        Integer max = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.COMPARE_PRODUCT, "maximum_product").toInteger()
        if(session.compare.size() >= max) {
            throw new Exception("maximum.products.added.to.comparison")
        }
        def compareItem = session.compare.find {
            it == productId
        }
        if(compareItem) {
            throw new Exception("you.have.already.added.to.comparison")
        }
        Product product = Product.get(productId)
        ProductData data = productService.getProductData(product)
        session.compare.add(productId)
        return data;
    }

    public static void removeFromCompare(productId) {
        def session = AppUtil.session;
        session.compare.remove(productId)
        if (session.compare.size() == 0) {
            removeCompare();
        }
    }

    public static void removeCompare() {
        def session = AppUtil.session;
        session.removeAttribute("compare")
    }
}
