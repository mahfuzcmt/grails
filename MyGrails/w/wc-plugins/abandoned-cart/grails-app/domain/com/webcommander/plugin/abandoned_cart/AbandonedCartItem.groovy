package com.webcommander.plugin.abandoned_cart

import com.webcommander.admin.Customer
import com.webcommander.calculator.TaxCalculator
import com.webcommander.constants.DomainConstants
import com.webcommander.events.AppEventManager
import com.webcommander.manager.HookManager
import com.webcommander.models.CartItem
import com.webcommander.models.CartObject
import com.webcommander.models.blueprints.CartItemable
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.TaxProfile
import groovy.json.JsonSlurper
import grails.util.TypeConvertingMap

class AbandonedCartItem implements CartItemable {

    Long id
    String itemType //product and others cart item
    Long itemId
    Integer quantity = 0
    String params

    Collection<String> variations = []
    Long storeId

    static belongsTo = [abandonedCart: AbandonedCart]
    static hasMany = [variations: String]
    static transients = ['object', 'priceObject', 'findNameAndUrl', 'paramsObj']
    static constraints = {
        params(nullable: true)
        variations(nullable: true)
        storeId(nullable: true)
    }
    static mapping = {
        params length: 1500
        variations joinTable:[name: "abandoned_cart_item_variations", key: "abandoned_cart_item_id", column: "variations_string", type: "varchar(250)"]
    }

    CartObject object() {
        CartObject obj = HookManager.hook("resolveCartObject-${itemType}", null, this)
        return obj;
    }

    Map findNameAndUrl() {
        return HookManager.hook("resolveNameAndUrl-${itemType}", [:], this.itemId)
    }

    // TODO: RFD : rework for discount
    Map priceObject() {
        CartObject object = object()
        if(object) {
            CartItem cartItem = new CartItem(object, quantity)
            AppUtil.session.customer ? Customer.proxy(AppUtil.session.customer) : null
            def discountTaxProfile = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.DISCOUNT, "tax_profile").toLong(null)
            discountTaxProfile ? TaxCalculator.getTaxRate(TaxProfile.proxy(discountTaxProfile)) : 0
            return [
                baseTotal: cartItem.baseTotal,
                price: cartItem.unitPrice,
                tax: cartItem.tax,
                discount: cartItem.discount,
                total: cartItem.baseTotal + cartItem.tax - cartItem.discount
            ]
        } else {
            return [:]
        }
    }

    TypeConvertingMap paramsObj() {
        def jsonSlurper = new JsonSlurper()
        return params ? new TypeConvertingMap(jsonSlurper.parseText(params) as Map) : new TypeConvertingMap()
    }

    @Override
    Long getStoreId() {
        return this.storeId
    }

    Long getItemId() {
        return this.itemId
    }

    @Override
    String getItemName() {
        CartObject object = object()
        return object ? object.name : itemType
    }
}
