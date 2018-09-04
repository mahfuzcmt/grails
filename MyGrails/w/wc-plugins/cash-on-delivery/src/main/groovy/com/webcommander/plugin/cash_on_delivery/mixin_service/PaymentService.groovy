package com.webcommander.plugin.cash_on_delivery.mixin_service

import com.webcommander.models.Cart

/**
 * Created by sanjoy on 12/08/2014.
 */
class PaymentService {
    void renderCODPaymentPage(Cart cart, Closure renderer) {
        renderOfflinePaymentPage(cart, renderer)
    }
}
