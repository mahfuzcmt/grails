package com.webcommander.plugin.gift_registry

import com.webcommander.webcommerce.Product

class GiftRegistryItem {
    Product product
    Integer quantity
    Integer purchased = 0
    String variation
    String included

    Date created
    Date updated

    Collection<String> variations = []
    Collection<String> combination = []

    static belongsTo = [geiftRegistry: GiftRegistry]
    static hasMany = [combination: String, variations: String]

    static constraints = {
       variation(nullable: true)
       included(nullable: true)
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

    public Integer getRemain() {
        return quantity - purchased;
    }
}
