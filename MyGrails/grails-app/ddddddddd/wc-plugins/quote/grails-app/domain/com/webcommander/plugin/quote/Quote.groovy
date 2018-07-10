package com.webcommander.plugin.quote

import com.webcommander.admin.Customer
import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Address

class Quote {
    Long id
    Customer customer

    Double shippingCost = 0
    Double shippingTax = 0
    Double handlingCost = 0

    Address billing
    Address shipping

    Date created
    Date updated

    Collection<QuoteItem> quoteItems = []
    static hasMany = [quoteItems: QuoteItem]

    static constraints = {
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

    public Double getGrandTotal() {
        return shippingCost + shippingTax + handlingCost + (quoteItems.sum { it.totalAmount } ?: 0)
    }

    public Double getSubTotal() {
        def subTotalConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ORDER_PRINT_AND_EMAIL, "subtotal_price")
        def subtotal = 0
        if(subTotalConfig == DomainConstants.SHOPPING_CART_TOTAL_PRICE.WITH_TAX_WITH_DISCOUNT) {
            subtotal = quoteItems.sum { it.totalAmount }
        } else if(subTotalConfig == DomainConstants.SHOPPING_CART_TOTAL_PRICE.WITH_TAX_WITHOUT_DISCOUNT) {
            subtotal = quoteItems.sum { it.totalAmount } + quoteItems.sum { it.discount };
        } else if(subTotalConfig == DomainConstants.SHOPPING_CART_TOTAL_PRICE.WITHOUT_TAX_WITH_DISCOUNT) {
            subtotal = quoteItems.sum { it.totalAmount } - quoteItems.sum { it.tax }
        } else if(subTotalConfig == DomainConstants.SHOPPING_CART_TOTAL_PRICE.WITHOUT_TAX_WITHOUT_DISCOUNT) {
            subtotal = quoteItems.sum { it.totalPrice }
        }
        return subtotal
    }

    public Double getTotalDiscount() {
        return quoteItems.sum { it.discount} ?: 0.0
    }

    public Double getTotalTax() {
        return quoteItems.sum { it.tax} ?: 0.0
    }
}
