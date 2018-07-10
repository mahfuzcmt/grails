package com.webcommander.plugin.product_custom_fields

import com.webcommander.plugin.product_custom_fields.domain.CheckoutField
import com.webcommander.webcommerce.Product

class ProductCheckoutField extends CheckoutField {

    Product product

    static belongsTo = [product: Product]

    static mapping = {
        options joinTable:[name: "product_checkout_field_options", key: "field_id", column: "option_text", type: "varchar(255)"]
    } >> CheckoutField.mapping
}