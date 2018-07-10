package com.webcommander.plugin.ebay_listing.admin.webmarketing

import com.ebay.soap.eBLBaseComponents.SiteCodeType

class EbayListingProfileSetting {
    String ebaySite = SiteCodeType.AUSTRALIA.toString()
    String mode //sandbox/production
    String devId
    String appId
    String certId
    String userToken

    static mapping = {
        userToken type: "text"
    }

}
