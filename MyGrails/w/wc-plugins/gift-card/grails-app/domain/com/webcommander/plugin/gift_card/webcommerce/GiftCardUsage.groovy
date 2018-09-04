package com.webcommander.plugin.gift_card.webcommerce

import com.webcommander.webcommerce.Order

class GiftCardUsage {

    Long id
    Double amount
    Order order

    Date created
    Date updated

    static belongsTo = [giftCard: GiftCard]

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
            return ("GiftCardUsage: " + id).hashCode()
        }
        if (order.id) {
            return ("GiftCardUsage: " + order.id).hashCode()
        }
        return super.hashCode();
    }

    @Override
    boolean equals(Object o) {
        if (!(o instanceof GiftCardUsage)) {
            return false
        }
        if (id && o.id) {
            return id == o.id
        }
        if (order.id && o.order.id) {
            return order.id == o.order.id
        }
        return super.equals(o);
    }
}
