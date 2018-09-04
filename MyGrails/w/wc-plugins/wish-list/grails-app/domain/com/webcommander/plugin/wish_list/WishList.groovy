package com.webcommander.plugin.wish_list

import com.webcommander.admin.Customer
import com.webcommander.common.Email

class WishList {
    Long id
    String name
    Customer customer

    Date created
    Date updated

    Collection<WishListItem> wishListItems = []
    Collection<Email> emails = []

    static hasMany = [wishListItems: WishListItem, emails: Email]

    def beforeValidate() {
        if (!this.created) {
            this.created = new Date().gmt()
        }
        if (!this.updated) {
            this.updated = new Date().gmt()
        }
    }

    def beforeUpdate() {
        this.updated = new Date().gmt()
    }
}
