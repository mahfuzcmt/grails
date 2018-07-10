package com.webcommander.webcommerce

class ShippingPolicy {

    Long id

    String name
    String policyType

    Double additionalAmount
    Double additionalCost

    Boolean isCumulative = false
    Boolean isPriceEnterWithTax = false
    Boolean isAdditional


    Date created
    Date updated

    Collection<ShippingCondition> conditions = []

    static hasMany = [conditions: ShippingCondition];

    static constraints = {
        name(blank: false, unique: true, maxSize: 100)
        isAdditional(nullable: true)
        additionalAmount(nullable: true)
        additionalCost(nullable: true)
    }

    static mapping = {
        conditions sort: "id", order: "asc"
    }

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

    @Override
    int hashCode() {
        if (id) {
            return ("ShippingPolicy: " + id).hashCode()
        }
        return super.hashCode();
    }

    @Override
    boolean equals(Object o) {
        if (!o instanceof ShippingPolicy) {
            return false
        }
        if (id && o.id) {
            return id == o.id
        }
        return super.equals(o);
    }
}
