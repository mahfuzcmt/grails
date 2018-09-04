package com.webcommander.plugin.google_trusted_store

import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Order
import com.webcommander.webcommerce.Payment
import grails.util.Holders

class GoogleTrustedStoreTagLib {
    static namespace = "googleTrustedStore"

    def layoutHead = {attr, body ->
        out << body();
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GOOGLE_TRUSTED_STORE )
        out << include(view: "/plugins/google_trusted_store/site/badge.gsp", model: [config: config]).toString()
    }

    def confirmStep = {attr, body ->
        out << body();
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GOOGLE_TRUSTED_STORE )
        Payment payment = pageScope.payment
        Order order = payment.order;
        String domain = Holders.config.grails.server.hostname
        Boolean hasDigitalGood  = order.items.find { !it.isShippable }
        try {
            out << include(view: "/plugins/google_trusted_store/site/confirmStep.gsp", model: [order: order, config: config, domain: domain, hasDigitalGood: hasDigitalGood])
        } catch (Exception ex) {}
    }
}
