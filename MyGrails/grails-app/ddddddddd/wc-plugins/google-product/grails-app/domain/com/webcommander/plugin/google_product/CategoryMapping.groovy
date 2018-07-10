package com.webcommander.plugin.google_product

class CategoryMapping {
    Long id
    Long categoryId
    String googleCategory

    static constraints = {
        categoryId(unique: true, blank: false)
    }
}
