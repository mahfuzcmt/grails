package com.webcommander.webcommerce

class ShippingProfile {

    Long id
    String name
    String description
    String rulePrecedence
    Date created
    Date updated

    List<ShippingRule> shippingRules = []

    static hasMany = [shippingRules: ShippingRule]

    static constraints = {
        name(blank: false, unique: true, maxSize: 100)
        description(nullable: true, maxSize: 500)
    }

    static transients = ['shippingRuleList']

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

    Integer getProductCount() {
        return Product.where {
            shippingProfile == this
        }.count()
    }

    @Override
    int hashCode() {
        if (id) {
            return ("ShippingProfile: " + id).hashCode()
        }
        return super.hashCode();
    }

    @Override
    boolean equals(Object o) {
        if (!o instanceof ShippingProfile) {
            return false
        }
        if (id && o.id) {
            return id == o.id
        }
        return super.equals(o);
    }

    List<ShippingRule> shippingRuleList(int max) {
        Integer count = shippingRules.size()
        if (count) {
            if(max && max <= count) {
                count = max
            }
            return shippingRules[0..count-1]
        }
        return [];
    }
}
