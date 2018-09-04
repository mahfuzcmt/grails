package com.webcommander.plugin.securepay_payment_gateway

class SecurePayConstants {
    static GATEWAY_RESPONSE = [
        '1': "approved",
        '2': "declined.by.bank",
        '3': "decline.other.reason",
        '4': "cancel.by.user"
    ]
}
