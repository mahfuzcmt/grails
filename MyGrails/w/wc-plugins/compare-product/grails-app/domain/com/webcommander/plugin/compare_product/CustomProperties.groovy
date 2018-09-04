package com.webcommander.plugin.compare_product

import com.webcommander.webcommerce.Product

class CustomProperties {

    Long id
    Long idx
    Product product
    String label
    String description

    static constraints = {
        label(unique: "product")
    }

    static mapping = {
        description type: "text"
    }

}
