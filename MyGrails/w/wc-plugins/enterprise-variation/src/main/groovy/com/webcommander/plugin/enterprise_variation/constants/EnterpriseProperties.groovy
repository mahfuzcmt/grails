package com.webcommander.plugin.enterprise_variation.constants

class EnterpriseProperties {
    public static CORE_PROP = ["isInventoryEnabled", "availableStock", "lowStockLevel", "spec", "metaTags"]
    public static STRING_PROP = ["title", "heading", "summary", "description", "model", "productCondition"]
    public static BOOLEAN_PROP = ["isMultipleOrderQuantity", "isOnSale", "isExpectToPay", "isCallForPriceEnabled", "isFeatured", "isNew"]
    public static DOUBLE_PROP = ["basePrice", "costPrice", "salePrice", "expectToPayPrice", "weight", "height", "length", "width"]

    public static BASIC_PROP = ["title", "heading", "summary", "description", "basePrice", "costPrice"]
    public static PRICE_STOCK_PROP = ["isInventoryEnabled", "availableStock", "lowStockLevel","isMultipleOrderQuantity", "isOnSale", "isExpectToPay", "isCallForPriceEnabled", "isFeatured", "isNew", "salePrice", "expectToPayPrice", "weight", "height", "length", "width", "model", "minOrderQuantity", "maxOrderQuantity"]
    public static ADVANCE_PROP = ["productCondition"]
}
