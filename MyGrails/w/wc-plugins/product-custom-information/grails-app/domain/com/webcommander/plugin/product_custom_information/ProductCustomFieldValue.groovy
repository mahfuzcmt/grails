package com.webcommander.plugin.product_custom_information

class ProductCustomFieldValue {
    Long id

    String value
    String entityType
    Long entityId

    ProductCustomField field

    static mapping = {
        value type: "text"
        sort field: "asc"
    }
}
