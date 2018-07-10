package com.webcommander.plugin.gift_registry

import com.webcommander.admin.Customer
import com.webcommander.common.Email
import com.webcommander.webcommerce.Address

class GiftRegistry {
    String name
    String eventName
    String eventDetails
    Address address

    Date eventDate
    Date created
    Date updated

    Collection<GiftRegistryItem> giftItems = []
    Collection<Email> emails = []

    static hasMany = [giftItems: GiftRegistryItem, emails: Email]
    static belongsTo = [customer: Customer]

    static constraints = {
        eventName(maxSize: 100);
        eventDetails(nullable: true, maxSize: 100)
    }

    static mapping = {
        eventDetails type: "text"
    }

    def beforeValidate() {
        if(!this.created) {
            this.created = new Date().gmt()
        }
        if(!this.updated) {
            this.updated = new Date().gmt()
        }
    }

    def beforeUpdate() {
        this.updated = new Date().gmt()
    }

}
