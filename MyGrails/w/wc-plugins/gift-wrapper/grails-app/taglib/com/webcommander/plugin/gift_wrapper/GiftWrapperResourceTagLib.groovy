package com.webcommander.plugin.gift_wrapper

import com.webcommander.AppResourceTagLib
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

class GiftWrapperResourceTagLib {

    static namespace = "appResource"

    static final String GIFT_WRAPPER = "gift-wrapper"
    static final String PRODUCT = "product"

    @Autowired
    @Qualifier("com.webcommander.AppResourceTagLib")
    AppResourceTagLib parent

    static def getGiftWrapperImageRelativeUrl(def dbId) {
        return "${GIFT_WRAPPER}/${GIFT_WRAPPER}-${dbId}/"
    }

    def getGiftWrapperImageURL = { attrs, body ->
        def giftWrapper = attrs.image
        String sizeOrPrefix = attrs.sizeOrPrefix ?: ""
        String url = giftWrapper.image ? parent.getAbstractStaticResourceImageURL(giftWrapper, sizeOrPrefix) : parent.getDefaultImageWithPrefix("450",PRODUCT)
        out << url
    }

}
