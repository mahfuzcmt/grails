package com.webcommander.plugin.ebay_listing.admin.webmarketing

import java.beans.Transient

class EbayListingProfile {

    String name
    String note

    Integer primaryCategory = 0
    Integer secondaryCategory = 0
    Integer itemCondition = 1000 //0:Use Product Condition/1000:Brand New/2750:Like New/4000:Very Good/5000:Good/6000:Acceptable

    Boolean useProductImage = true
    Boolean useDefaultSetting = true

    EbayPricing pricing
    EbayPaymentMethod safePaymentMethod
    EbayPostage postage
    EbayReturnPolicy returnPolicy
    EbayListingProfileSetting setting

    Collection<EbayPaymentMethod> availablePaymentMethods = []

    static hasMany = [availablePaymentMethods: EbayPaymentMethod]

    static constraints = {
        note(nullable: true, maxSize: 500)
        secondaryCategory(nullable: true)
        setting(nullable: true)
    }

    @Transient
    public Boolean isUpdatableToEbay() {
        //TODO: return true if all requirements for ebay listing fulfil
        return false
    }
}
