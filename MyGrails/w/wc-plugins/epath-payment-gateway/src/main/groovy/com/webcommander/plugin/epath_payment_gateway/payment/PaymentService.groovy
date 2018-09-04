package com.webcommander.plugin.epath_payment_gateway.payment

import com.webcommander.ApplicationTagLib
import com.webcommander.constants.DomainConstants
import com.webcommander.log.WcLogManager
import com.webcommander.models.AddressData
import com.webcommander.models.Cart
import com.webcommander.plugin.epath_payment_gateway.EpathService
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.PaymentGatewayMeta
import grails.util.Holders

import javax.servlet.http.HttpSession

class PaymentService {
    static EpathService _epathService

    static ApplicationTagLib _app
    static getApp() {
        if(!_app) {
            return _app = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib)
        }
        return _app
    }
    static getEpathService() {
        if(_epathService) {
            return _epathService
        }
        return _epathService = Holders.grailsApplication.mainContext.getBean(EpathService)
    }
    void renderEPATHCRDPaymentPage(Cart cart, Closure renderer) {
        def configs = PaymentGatewayMeta.findAllByFieldFor(DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.EPATH)
        Map configMap = [:]
        configs.each {
            configMap[it.name] = it.value
        }
        configMap["type"] = "NET"
        "render${configMap["type"]}EPATHCRDPaymentPage"(cart, configMap, renderer)
    }

    private void renderNETEPATHCRDPaymentPage(Cart cart, Map configs, Closure renderer) {
        HttpSession session = AppUtil.session
        AddressData address = session.effective_billing_address;
        Map formModel = epathService.processEpathPayment(configs, address, cart)
        String log = "Action: Redirecting to E-path\nRequestData:\n${AppUtil.getQueryStringFromMap(formModel)}\n"
        WcLogManager.log(log, "E-pathLogger")
        renderer(view: "/plugins/e_path/epathHostedForm", model: formModel)
    }

}
