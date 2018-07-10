package com.webcommander.plugin.gift_card.webcommerce

import com.webcommander.admin.Operator

class GiftCardAmountAdjustment {

    Long id
    Double changeAmount
    String note
    Date created
    Operator createdBy

    GiftCard giftCard

    static belongsTo = [giftCard: GiftCard]

    static constraints = {
        note(nullable: true)
        createdBy(nullable: true)
    }

    def beforeValidate(){
        if(!created){
            created = new Date().gmt()
        }
    }

}
