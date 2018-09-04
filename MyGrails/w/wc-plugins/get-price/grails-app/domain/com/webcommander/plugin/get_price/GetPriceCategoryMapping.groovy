package com.webcommander.plugin.get_price

class GetPriceCategoryMapping {
    Long id
    Long categoryId
    String getPriceCategory

    static constraints = {
        categoryId(unique: true, blank: false)
    }
}
