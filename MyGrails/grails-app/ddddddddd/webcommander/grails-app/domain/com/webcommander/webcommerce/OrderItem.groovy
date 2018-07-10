package com.webcommander.webcommerce

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.manager.HookManager
import com.webcommander.models.ProductData
import com.webcommander.models.blueprints.CartItemable
import com.webcommander.util.AppUtil
import grails.util.TypeConvertingMap

class OrderItem implements Serializable, CartItemable {

    Long id
    Integer quantity
    Long productId
    Long shippingClassId

    String productName
    String productType
    String shippingClassName

    Boolean isTaxable
    Boolean isShippable

    Double price = 0 //unit price
    Double tax = 0
    Double discount = 0
    Double actualPrice = 0
    Double actualDiscount = 0
    Double taxDiscount = 0

    Collection<String> variations = [] //stored as key: value map

    static hasMany = [variations: String]

    static belongsTo = [order: Order]

    static constraints = {
        shippingClassId nullable: true
        shippingClassName nullable: true
    }
    static mapping = {
        variations joinTable: [name: "order_product_variations", key: "item_id", column: "variation", type: "varchar(2000)"]
    }

    static transients = ['productData', 'shippingMethodName']

    @Override
    int hashCode() {
        if (id) {
            return ("OrderItem: " + id).hashCode()
        }
        return super.hashCode();
    }

    @Override
    boolean equals(Object o) {
        if (!o instanceof OrderItem) {
            return false
        }
        if (id && o.id) {
            return id == o.id
        }
        return super.equals(o);
    }

    Double getTotalPriceConsideringConfiguration() {
        def totalConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ORDER_PRINT_AND_EMAIL, "product_total_price")
        def total = 0
        if(totalConfig == DomainConstants.SHOPPING_CART_TOTAL_PRICE.WITH_TAX_WITH_DISCOUNT) {
            total = totalAmount
        } else if(totalConfig  == DomainConstants.SHOPPING_CART_TOTAL_PRICE.WITH_TAX_WITHOUT_DISCOUNT) {
            total = totalAmount
        } else if(totalConfig  == DomainConstants.SHOPPING_CART_TOTAL_PRICE.WITHOUT_TAX_WITH_DISCOUNT) {
            total = totalAmount - tax
        } else if(totalConfig  == DomainConstants.SHOPPING_CART_TOTAL_PRICE.WITHOUT_TAX_WITHOUT_DISCOUNT) {
            total = totalPrice
        }
        return total
    }

    Double getTotalPrice() {
        return quantity * price
    }

    Double getTotalAmount() {
        return (quantity * getDisplayPrice()) - actualDiscount
    }

    Double getDisplayPrice() {
        return new Double( (price + (tax/quantity)).toConfigPrice() );
    }

    @Override
    String getItemName() {
        return this.productName
    }

    String getItemNumber() {
        String itemNumber = null
        Product product
        if(this.productType == NamedConstants.CART_OBJECT_TYPES.PRODUCT && (product = Product.get(this.productId))) {
            ProductService productService = ProductService.getInstance()
            TypeConvertingMap paramsObj = this.paramsObj()
            ProductData productData = productService.getProductData(product, paramsObj?.config)
            itemNumber = productData?.sku
        }
        return itemNumber
    }

    @Override
    String getItemType() {
        return this.productType
    }

    @Override
    Long getItemId() {
        return this.productId
    }

    @Override
    TypeConvertingMap paramsObj() {
        TypeConvertingMap params = new TypeConvertingMap()
        params = HookManager.hook("getCartParamsFromOrderItem", params, this)
        return params
    }

    def getProductData() {
        Product product = Product.get(this.itemId)
        if(!product) {
            return [:]
        }
        TypeConvertingMap paramsObj = this.paramsObj()
        ProductData productData = ProductService.instance.getProductData(product, paramsObj?.config)
        return (productData ?: [:])
    }

    String getShippingMethodName() {
        ShippingClass shippingClass = shippingClassId ? ShippingClass.get(shippingClassId) : null
        return shippingClass ? shippingClass.name : shippingClassName
    }

    Boolean isShowActualPrice () {
        if (discount > 0.001 && actualPrice != discount) {
            return true
        }
        return false
    }

    Double getTax() {
        return tax - taxDiscount
    }

}
