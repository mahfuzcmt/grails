package com.webcommander.plugin.variation

import com.webcommander.AppResourceTagLib
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

class VariationResourceTagLib {
    static namespace = "appResource"

    public static final RESOURCES_PATH = [
            "VARIATION" : "variation"
    ]

    @Autowired
    @Qualifier("com.webcommander.AppResourceTagLib")
    AppResourceTagLib parent

    def getVariationRelativeUrl(def id) {
        return "${RESOURCES_PATH.VARIATION}/option/option-$id/"
    }

    def getVariationImageUrl = { attrs, body->
        def variation = attrs.image
        String sizeOrPrefix = attrs.sizeOrPrefix ?: ""
        String url = parent.getAbstractStaticResourceImageURL(variation, sizeOrPrefix)
        out << url
    }
}