package com.webcommander.plugin.product_custom_information

class ProductCustomField {
    Long id

    String title
    String type

    static constraints = {
        title(blank: false)
        type(blank: false)
    }

    static mapping = {
        sort id: "asc"
    }
}
