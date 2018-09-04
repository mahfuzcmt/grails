package com.webcommander.plugin.product_custom_fields

import com.webcommander.plugin.product_custom_fields.domain.CheckoutField
import com.webcommander.webcommerce.Category as CAT

class CategoryCheckoutField extends CheckoutField {

    CAT category

    static belongsTo = [category: CAT]

    static mapping = {
        options joinTable:[name: "category_checkout_field_options", key: "field_id", column: "option_text", type: "varchar(255)"]
    } >> CheckoutField.mapping

}
