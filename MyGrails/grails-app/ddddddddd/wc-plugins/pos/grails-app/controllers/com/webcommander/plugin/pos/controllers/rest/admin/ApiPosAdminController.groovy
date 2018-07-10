package com.webcommander.plugin.pos.controllers.rest.admin

import com.webcommander.admin.Customer
import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil
import com.webcommander.util.RestProcessor

class ApiPosAdminController extends RestProcessor{

    def defaultCustomer() {
        String defaultCustomerId = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.POS, "default_customer");
        Customer customer = Customer.get(defaultCustomerId)
        Map config = [
           activeShippingAddress: [details: true],
           activeBillingAddress: [details: true],
           address: [details: true],
        ]
        rest(customer: customer, config)
    }
}
