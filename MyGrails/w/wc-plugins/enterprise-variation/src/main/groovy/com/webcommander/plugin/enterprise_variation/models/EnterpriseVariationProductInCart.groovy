package com.webcommander.plugin.enterprise_variation.models

import com.webcommander.plugin.variation.models.VariationProductData
import com.webcommander.plugin.variation.models.VariationProductInCart
import grails.util.TypeConvertingMap

class EnterpriseVariationProductInCart extends VariationProductInCart {

    EnterpriseVariationProductInCart(VariationProductData data, TypeConvertingMap params) {
        super(data, params)
    }

    Boolean iEquals(Object ob) {
        if (ob instanceof EnterpriseVariationProductData) {
            return this.product.productVariationId == ob.productVariationId
        } else if (ob instanceof EnterpriseVariationProductInCart){
            return ob.product.productVariationId == this.product.productVariationId
        }
        return false
    }
}
