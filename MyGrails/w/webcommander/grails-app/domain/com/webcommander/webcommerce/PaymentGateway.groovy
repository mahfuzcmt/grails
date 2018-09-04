package com.webcommander.webcommerce

import com.webcommander.admin.Zone
import com.webcommander.constants.DomainConstants
import com.webcommander.events.AppEventManager
import grails.util.Holders
import org.grails.plugins.web.taglib.ApplicationTagLib

class PaymentGateway {
    private static ApplicationTagLib _g
    private static ApplicationTagLib getG() {
        return _g ?: (_g = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib));
    }

    Long id

    String code
    String name
    String information
    String pendingMessage
    String successMessage
    String surchargeType = DomainConstants.SURCHARGE_TYPE.NO_SURCHARGE

    Double flatSurcharge = 0

    Boolean isEnabled = false
    Boolean isDefault = false
    Boolean isPromotional = false
    Boolean isSurChargeApplicable = false

    Date updated

    Zone zone

    Collection<SurchargeRange> surchargeRange = []

    static mapping = {
        surchargeRange cache: true
    }

    static hasMany = [
        surchargeRange: SurchargeRange
    ]

    static constraints = {
        information(nullable: true, maxSize: 2000)
        pendingMessage(nullable: true, maxSize: 2000)
        successMessage(nullable: true, maxSize: 2000)
        zone(nullable: true)
    }

    static fieldMarshaller = [
        name: { PaymentGateway gateway ->
            return g.message(code: gateway.name)
        }
    ]

    def beforeValidate() {
        if (!this.updated) {
            this.updated = new Date().gmt()
        }
    }

    def beforeUpdate() {
        this.updated = new Date().gmt()
    }

    public static void initialize() {
        def _init = {
            if(PaymentGateway.count() == 0) {
                new PaymentGateway(
                    code: DomainConstants.PAYMENT_GATEWAY_CODE.PAY_IN_STORE,
                    name: "pay.store"
                ).save()
                new PaymentGateway(
                    code: DomainConstants.PAYMENT_GATEWAY_CODE.CHEQUE,
                    name: "cheque"
                ).save()
                new PaymentGateway(
                    code: DomainConstants.PAYMENT_GATEWAY_CODE.MONEY_ORDER,
                    name: "money.order"
                ).save()
                new PaymentGateway(
                    code: DomainConstants.PAYMENT_GATEWAY_CODE.BANK_DEPOSIT,
                    name: "bank.deposit"
                ).save()
                new PaymentGateway(
                    code: DomainConstants.PAYMENT_GATEWAY_CODE.ACCOUNTS_PAYABLE,
                    name: "accounts.payable"
                ).save()
                new PaymentGateway(
                    code: DomainConstants.PAYMENT_GATEWAY_CODE.STORE_CREDIT,
                    name: "store.credit",
                    isPromotional: true
                ).save()
                new PaymentGateway(
                    code: DomainConstants.PAYMENT_GATEWAY_CODE.PAYPAL,
                    name: "paypal",
                    isEnabled: true,
                    isDefault: true,
                    isSurChargeApplicable: true,
                    pendingMessage: "s:your.payment.pending"
                ).save()
                new PaymentGateway(
                    code: DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD,
                    name: "credit.card",
                    isSurChargeApplicable: true
                ).save()
                new PaymentGateway(
                    code: DomainConstants.PAYMENT_GATEWAY_CODE.API,
                    name: "api"
                ).save()
            }
        }
        if(Zone.count()) {
            _init()
        } else {
            AppEventManager.one("zone-bootstrap-init", "bootstrap-init", _init)
        }
    }
}
