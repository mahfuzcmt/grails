package com.webcommander.plugin.variation.models

import com.webcommander.item.ProductRawData


class ProductVariationRawData {
    String model
    ProductRawData rawData

    List<Map> combination

    public ProductVariationRawData(ProductRawData rawData) {
        this.rawData = rawData
    }
}
