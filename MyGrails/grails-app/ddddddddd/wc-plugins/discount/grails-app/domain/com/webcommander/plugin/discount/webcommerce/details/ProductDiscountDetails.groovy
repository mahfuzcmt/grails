package com.webcommander.plugin.discount.webcommerce.details

import com.webcommander.plugin.discount.Constants
import com.webcommander.util.AppUtil
import com.webcommander.util.SortAndSearchUtil
import com.webcommander.webcommerce.Category
import com.webcommander.webcommerce.Product

class ProductDiscountDetails {
    Long id
    String type
    String amountType
    String singleAmountType = Constants.AMOUNT_TYPE.FLAT
    String minimumQtyOn = Constants.MINIMUM_QTY_ON.EACH_ITEM

    Integer freeProductMaxQty
    Integer capPriceMaxQty

    Double capPrice
    Double singleAmount

    Collection<Long> productIds = []
    Collection<Long> categoryIds = []
    Collection<DiscountQtyTier> tiers = []

    static copy_reference = ["productIds", "categoryIds", "tiers"]

    static hasMany = [productIds: Long, categoryIds: Long, tiers: DiscountQtyTier]

    static constraints = {
        freeProductMaxQty(nullable: true)
        capPriceMaxQty(nullable: true)
        capPrice(nullable: true)
        singleAmount(nullable: true)
        amountType(nullable: true)
    }

    def beforeInsert() {
        productIds.sort()
        categoryIds.sort()
    }

    def afterLoad() {
        SortAndSearchUtil.sortIfUnsorted(productIds)
        SortAndSearchUtil.sortIfUnsorted(categoryIds)
    }

    List<Product> getProducts() {
        return (this.productIds ? Product.findAllByIdInList(this.productIds) : [])
    }

    List<Category> getCategories() {
        return (this.categoryIds ? Category.findAllByIdInList(this.categoryIds) : [])
    }

    String getDisplaySingleAmount() {
        return singleAmountType == Constants.AMOUNT_TYPE.FLAT ? AppUtil.baseCurrency.symbol + (singleAmount?.toConfigPrice() as String) : "$singleAmount%"
    }
}
