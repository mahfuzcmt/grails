package com.webcommander.plugin.order_custom_fields

class OrderCheckoutFieldsTitle {
    String title
    static constraints = {
        title(maxSize: 100)
    }
}
