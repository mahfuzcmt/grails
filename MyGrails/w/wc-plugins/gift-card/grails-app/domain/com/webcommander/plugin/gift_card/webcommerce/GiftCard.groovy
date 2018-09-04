package com.webcommander.plugin.gift_card.webcommerce

import com.webcommander.admin.Country
import com.webcommander.admin.State
import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil

import java.beans.Transient

class GiftCard {

    Long id
    Long productId
    Long orderItemId
    Long orderRef

    Boolean isActive = false
    Boolean isPaid = false

    Double amount
    String code
    String productName
    String firstName
    String lastName
    String phone
    String mobile
    String city
    String postCode
    String address
    String email
    String senderName
    String message
    String sendingType
    String senderEmail
    String createdBy

    State state
    Country country

    Date created
    Date activated
    Date availableTo

    Collection<GiftCardAmountAdjustment> adjustments = []
    Collection<GiftCardUsage> usages = []

    static hasMany = [adjustments: GiftCardAmountAdjustment, usages: GiftCardUsage]

    static constraints = {
        code(blank: false, maxSize: 50, unique: true)
        senderName(nullable: true)
        city(nullable: true)
        postCode(nullable: true)
        state(nullable: true)
        country(nullable: true)
        address(nullable: true)
        phone(nullable: true)
        mobile(nullable: true)
        lastName(nullable: true)
        message(nullable: true)
        activated(nullable: true)
        availableTo(nullable: true)
        orderRef(nullable: true)
    }

    static mapping = {
        message type: "text"
    }

    public void beforeValidate() {
        if(!created) {
            created = new Date().gmt()
        }
    }

    @Transient
    public Double getAvailableBalance() {
        Double redeemAmount = GiftCardUsage.createCriteria().get {
            projections {
                sum("amount")
            }
            eq("giftCard", this)
        } ?: 0.0
        return amount - redeemAmount
    }

    @Override
    int hashCode() {
        if (id) {
            return ("GiftCard: " + id).hashCode()
        }
        if (code) {
            return ("GiftCard: " + code).hashCode()
        }
        return super.hashCode();
    }

    @Override
    boolean equals(Object o) {
        if (!(o instanceof GiftCard)) {
            return false
        }
        if (id && o.id) {
            return id == o.id
        }
        if (code && o.code) {
            return code == o.code
        }
        return super.equals(o);
    }

    static final String getCodePrefix() {
        return AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GIFT_CARD, "gc_code_prefix")
    }
}
