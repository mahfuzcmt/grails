package com.webcommander.webcommerce

import com.webcommander.admin.Customer
import com.webcommander.admin.Operator
import com.webcommander.constants.DomainConstants
import com.webcommander.manager.HookManager
import com.webcommander.util.AppUtil

class Order {

    Long id
    Long customerId

    String deliveryType = DomainConstants.ORDER_DELIVERY_TYPE.SHIPPING

    Double shippingCost = 0
    Double shippingTax = 0
    Double handlingCost = 0
    Double totalSurcharge = 0
    Double discountOnShipping = 0
    Double discountOnOrder = 0
    Double actualTax = 0

    String shippingStatus //awaiting, partial, full
    String paymentStatus //unpaid, partial, paid
    String orderStatus //payment awaiting, shipment awaiting, cancelled, completed
    String ipAddress
    String customerName
    Integer reminderCount= 0 ;

    Date lastReminderTime
    Date created
    Date updated

    Address billing
    Address shipping
    Operator createdBy

    Collection<OrderItem> items = []
    Collection<Payment> payments = []
    Collection<OrderComment> orderComment = []
    Collection<Shipment> shipments = []

    static hasMany = [items: OrderItem, payments: Payment, shipments: Shipment, orderComment: OrderComment]

    static constraints = {
        totalSurcharge(nullable: true)
        shipping(nullable: true)
        customerId(nullable: true)
        ipAddress(nullable: true)
        createdBy(nullable: true)
    }

    static mapping = {
        table "orders"
    }

    static marshallerExclude = ["customer"]

    def beforeValidate() {
        if (!this.created) {
            this.created = new Date().gmt()
        }
        if(!this.lastReminderTime) {
            this.lastReminderTime = new Date().gmt();
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
            return ("Order: " + id).hashCode()
        }
        return super.hashCode();
    }

    @Override
    boolean equals(Object o) {
        if (!o instanceof Order) {
            return false
        }
        if (id && o.id) {
            return id == o.id
        }
        return super.equals(o);
    }

    Double getSubTotal() {
        def subTotalConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ORDER_PRINT_AND_EMAIL, "subtotal_price")
        def subtotal = 0
        if(subTotalConfig == DomainConstants.SHOPPING_CART_TOTAL_PRICE.WITH_TAX_WITH_DISCOUNT) {
            subtotal = items.sum { it.totalAmount }
        } else if(subTotalConfig == DomainConstants.SHOPPING_CART_TOTAL_PRICE.WITH_TAX_WITHOUT_DISCOUNT) {
            subtotal = items.sum { it.totalAmount };
        } else if(subTotalConfig == DomainConstants.SHOPPING_CART_TOTAL_PRICE.WITHOUT_TAX_WITH_DISCOUNT) {
            subtotal = items.sum { it.totalAmount } - items.sum { it.tax }
        } else if(subTotalConfig == DomainConstants.SHOPPING_CART_TOTAL_PRICE.WITHOUT_TAX_WITHOUT_DISCOUNT) {
            subtotal = items.sum { it.totalPrice }
        }
        return subtotal
    }

    Double getItemsDiscount() {
        items.sum { it.discount}
    }

    Double getTotalDiscount() {
        return itemsDiscount + discountOnShipping + discountOnOrder
    }

    Double getTotalTax() {
        if (actualTax) {
            return actualTax
        }
        return items.sum { OrderItem item ->
            item.tax
        }
    }

    Double getGrandTotal() {
        Double grandTotal = shippingCost + shippingTax + handlingCost + totalSurcharge + getTotalAmount() - discountOnShipping
        Map data = [order: this, grandTotal: grandTotal]
        data = (Map)HookManager.hook("calculate-grand-total", data)
        return data.grandTotal
    }

    Double getTotalAmount() {
        if (actualTax) {
            return getTotal() - discountOnOrder + actualTax
        } else {
            def itemTotalAmount = items.sum { it.totalAmount }
            return (itemTotalAmount ? itemTotalAmount - discountOnOrder : 0)
        }
    }

    Double getPaid() {
        return payments.sum {
            it.status == DomainConstants.PAYMENT_STATUS.SUCCESS ? it.amount : 0
        } ?: 0
    }

    Double getDue() {
        Double due = grandTotal - paid
        if(due < 0.01) {
            due = 0;
        }
        return due;
    }

    Long getTotalItem() {
        return items.sum{ it.quantity }
    }

    Double getTotal() {
        return items.sum{ it.totalPrice }
    }

    Customer getCustomer() {
        Customer.findById(customerId)
    }
}
