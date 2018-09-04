package com.webcommander.plugin.myob.constants

/**
 * Created by sanjoy on 3/7/14.
 */
public class LinkComponent {
    static TYPES = [
        TAX: "tax",
        PRODUCT: "product",
        PRODUCT_ADJUSTMENT: "productAdjustment",
        PRODUCT_ADJUSTMENT_LINE: "productAdjustmentLine",
        CUSTOMER: "customer",
        ORDER: "order",
        PAYMENT: "payment",
        ORDER_ITEM: "order_item"
    ]

    static CUSTOME_ATTR_MAPPING = [
        LENGHT: "length",
        WIDTH: "width",
        HEIGHT: "height",
        WEIGHT: "weight",
        BRAND: "brand",
        MANUFACTURER: "manufacturer",
        MODEL: "model",
        CATEGORY_LEVEL_1: "category_level_1",
        CATEGORY_LEVEL_2: "category_level_2",
        CATEGORY_LEVEL_3: "category_level_3",
    ]

    static CUSTOME_ATTR_MAPPING_NAMED_CONSTANT = [
        "": "none",
        (LinkComponent.CUSTOME_ATTR_MAPPING.LENGHT): "length",
        (LinkComponent.CUSTOME_ATTR_MAPPING.WIDTH): "width",
        (LinkComponent.CUSTOME_ATTR_MAPPING.HEIGHT): "height",
        (LinkComponent.CUSTOME_ATTR_MAPPING.WEIGHT): "weight",
        (LinkComponent.CUSTOME_ATTR_MAPPING.BRAND): "brand",
        (LinkComponent.CUSTOME_ATTR_MAPPING.MANUFACTURER): "manufacturer",
        (LinkComponent.CUSTOME_ATTR_MAPPING.MODEL): "model",
        (LinkComponent.CUSTOME_ATTR_MAPPING.CATEGORY_LEVEL_1): "category.level.1",
        (LinkComponent.CUSTOME_ATTR_MAPPING.CATEGORY_LEVEL_2): "category.level.2",
        (LinkComponent.CUSTOME_ATTR_MAPPING.CATEGORY_LEVEL_3): "category.level.3",
    ]

}