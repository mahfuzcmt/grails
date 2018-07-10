package com.webcommander.plugin.enterprise_variation

import com.webcommander.AppResourceTagLib
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

class EnterpriseVariationResourceTagLib {
    static namespace = "appResource"

    @Autowired
    @Qualifier("com.webcommander.AppResourceTagLib")
    AppResourceTagLib parent

    def getVariationProductRelativeUrl(def evariationDetailsId) {
        return "variation/product/product-$evariationDetailsId/"
    }

    def findVariationProductUrlInfix(def tenantId, def evariationDetailsId) {
        return "resources/${tenantId}${getVariationProductRelativeUrl(evariationDetailsId)}"
    }

    def getProductVideoInfix(def tenantId, def evariationDetailsId) {
        return "resources/${tenantId}${getVariationProductRelativeUrl(evariationDetailsId)}"
    }

    String getProductSpecInfix(def tenantId, def evariationDetailsId) {
        return "resources/${tenantId}${getVariationProductRelativeUrl(evariationDetailsId)}/spec/"
    }

}