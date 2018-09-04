package com.webcommander.admin

import com.webcommander.beans.SiteMessageSource
import grails.util.Holders

class MessageSource {

    Long id
    String messageKey
    String message
    String locale

    Date created
    Date updated

    static constraints = {
        locale(unique: "messageKey")
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

    private static SiteMessageSource _siteMessageSource;
    private static SiteMessageSource getSiteMessageSource() {
        return _siteMessageSource ?: (_siteMessageSource = (Holders.grailsApplication.mainContext.getBean(SiteMessageSource)))
    }

    public static void initialize() {
        Map messages = [
            "abn": "ABN",
            "abn.branch": "ABN Branch",
            "abn.format": "41 824 753 556",
            "address.line": "Address Line",
            "address.line.1": "Address Line 1",
            "address.line.2": "Address Line 2",
            "amount": "Amount",
            "discount": "Discount",
            "tax": "Tax",
            "total.discount": "Total Discount",
            "sub.total.tax": "Sub Total Tax",
            "sub.total": "Sub Total",
            "shipping.cost": "Shipping Cost",
            "shipping.tax": "Shipping Tax",
            "handling.cost": "Handling Cost",
            "available": "Available",
            "call.for.price": "Call for Price",
            "cart.count.items": "%item_count% Items",
            "checkout": "Checkout",
            "confirm.email": "Confirm Email",
            "confirm.order": "Confirm Order",
            "continue.shopping": "Continue Shopping",
            "country": "Country",
            "company.name": "Company Name",
            "customer.type": "Customer Type",
            "email": "Email",
            "email.address.braces": "(email address)",
            "expect.to.pay": "Expect to Pay",
            "error.minimum.purchase.amount": "The minimum purchase amount for this shop is %currencySymbol%%min_purchase_amount%, please add more products to your cart",
            "fax": "Fax",
            "first.name": "First Name",
            "from": "From",
            "gender": "Gender",
            "i.agree.term.condition": "I agree to the terms and conditions",
            "including.code.rate": "Including %code% (%rate%)",
            "last.name": "Last Name",
            "login": "Login",
            "low.stock": "Low Stock",
            "minimum.required.quantity": "Minimum %minimum_quantity% quantity must be added",
            "mobile": "Mobile",
            "name": "Name",
            "newsletter.signup": "Newsletter Sign Up",
            "note": "Note",
            "optional.braces": "(optional)",
            "order.quantity.should.multiple": "Order quantity should be multiple of %multiple_of_quantity%",
            "out.of.stock": "Out of Stock",
            "pagination.status.text": "Showing {0} to {1} of {2} ({3} Pages)",
            "password": "Password",
            "phone": "Phone",
            "post.code": "Post Code",
            "price": "Price",
            "proceed.checkout": "Proceed To Checkout",
            "product.already.in.cart": "This product is already added in your cart",
            "quantity.not.available.availables.added.in.cart": "%requested_quantity% is not currently available. %available_quantity% has been added in your cart.",
            "read.more": "Read More",
            "registration.terms": "Registration Terms",
            "requested.quantity.not.available": "%requested_quantity% quantity of this product is not available",
            "related.product": "Related Product",
            "related.products": "Related Products",
            "retype.password": "Retype Password",
            "search": "Search",
            "search.for.product.etc": "Search for product, category or article",
            "sex": "Sex",
            "shopping.cart": "Shopping Cart",
            "shopping.cart.page": "Shopping Cart Page",
            "subscribe": "Subscribe",
            "suburb/city": "Suburb/City",
            "to": "To",
            "todays.price": "Today's Price",
            "today": "Today",
            "you.can.buy.maximum.quantity.for.product" : "You can buy maximum %maximum_quantity% quantity of this product",
            "you.can.not.buy.this.product": "You can not buy this product",
            "your.shopping.cart.empty": "Your shopping cart is empty",
            "checkout.new.customer.registration.message": "Register with us for a faster checkout. Get updated & track all of your orders. Guest checkout lets you place the order without registering.",
            "handling.tax": "Handling Tax",
            "invalid.quantity": "Invalid quantity",
            "max.quantity.allowed": "Maximum %requested_quantity% quantity allowed",
            "min.required.quantity": "Minimum %requested_quantity% quantity must be added",
            "item.quantity.should.multiple": "Item quantity should be multiple of %requested_quantity%",
            "item.not.available": "This item is not available now",
            "shipping.discount": "Shipping Discount"
        ]

        if (MessageSource.count() == 0) {
            messages.each {
                new MessageSource(messageKey: it.key, message: it.value, locale: "all").save()
            }
        }
    }
}
