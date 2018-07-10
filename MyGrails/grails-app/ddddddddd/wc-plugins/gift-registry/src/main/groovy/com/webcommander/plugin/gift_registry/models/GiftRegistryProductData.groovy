package com.webcommander.plugin.gift_registry.models

import com.webcommander.beans.SiteMessageSource
import com.webcommander.models.ProductData
import com.webcommander.plugin.gift_registry.GiftRegistryItem
import com.webcommander.webcommerce.Product
import grails.util.Holders

/**
 * Created by sourav on 9/25/2016.
 */

class GiftRegistryProductData extends  ProductData {

    GiftRegistryItem giftItem
    private static SiteMessageSource _siteMessageSource

    private static getSiteMessageSource() {
        return _siteMessageSource?: (_siteMessageSource = Holders.applicationContext.getBean(SiteMessageSource))
    }
    GiftRegistryProductData(Product product, GiftRegistryItem giftItem){
        super(product)
        this.giftItem = giftItem
    }


}
