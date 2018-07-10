package com.webcommander.plugin.filter

import com.webcommander.AppResourceTagLib
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

class FilterResourceTagLib {
    static namespace = "appResource"

    @Autowired
    @Qualifier("com.webcommander.AppResourceTagLib")
    AppResourceTagLib parent

    static final String FILTER_GROUP_ITEM = "filter-group-item"

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

    def getFilterGroupItemImageURL = { attrs, body ->
        FilterGroupItem filterGroupItem = attrs.filterGroupItem
        String imageSize = attrs.imageSize
        String url = filterGroupItem.image ? "${filterGroupItem.baseUrl}${AppResourceTagLib.RESOURCES}/${filterGroupItem.getTenantId()}${filterGroupItem.getRelativeUrl()}${parent.getImagePrefix(imageSize)}${filterGroupItem.getResourceName()}" : parent.getDefaultImageWithPrefix("150", AppResourceTagLib.CATEGORY)
        out << url
    }

    def getFilterGroupItemRelativeUrl(def filterGroupItemId) {
        return "${FILTER_GROUP_ITEM}/${FILTER_GROUP_ITEM}-${filterGroupItemId}/"
    }
}