package com.webcommander.controllers.rest.admin.webcommerce

import com.webcommander.authentication.annotations.Restriction
import com.webcommander.util.RestProcessor
import com.webcommander.webcommerce.ShippingClass
import com.webcommander.webcommerce.ShippingPolicy
import com.webcommander.webcommerce.ShippingProfile
import com.webcommander.webcommerce.ShippingRule
import com.webcommander.webcommerce.ShippingService

class ApiShippingAdminController extends RestProcessor {
    ShippingService shippingService

    @Restriction(permission = "shipping.view.list")
    def profileList() {
        params.max = params.max ?: "-1"
        params.offset = params.offset ?: "0"
        List<ShippingProfile> profiles = shippingService.getShippingProfile(params)
        rest profiles: profiles
    }

    @Restriction(permission = "shipping.view.list")
    def ruleList() {
        params.max = params.max ?: "-1"
        params.offset = params.offset ?: "0"
        List<ShippingRule> rules = shippingService.getShippingRule(params)
        rest rules: rules
    }

    @Restriction(permission = "shipping.view.list")
    def policyList() {
        params.max = params.max ?: "-1"
        params.offset = params.offset ?: "0"
        List<ShippingPolicy> policies = shippingService.getShippingPolicy(params)
        Map config = [
           conditions: [details: true]
        ]
        rest policies: policies, config
    }

    @Restriction(permission = "shipping.view.list")
    def classList() {
        params.max = params.max ?: "-1"
        params.offset = params.offset ?: "0"
        List<ShippingClass> classes = shippingService.getShippingClass(params)
        rest classes: classes
    }
}
