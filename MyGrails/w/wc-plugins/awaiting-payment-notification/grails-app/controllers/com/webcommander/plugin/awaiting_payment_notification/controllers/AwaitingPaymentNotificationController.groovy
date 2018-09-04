package com.webcommander.plugin.awaiting_payment_notification.controllers

import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil

class AwaitingPaymentNotificationController {

    def config() {
        Map config =  AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.AWAITING_PAYMENT)
        render(view: "/plugins/awaiting_payment_notification/awaitingPaymentSetting", model: [config: config])
    }
}
