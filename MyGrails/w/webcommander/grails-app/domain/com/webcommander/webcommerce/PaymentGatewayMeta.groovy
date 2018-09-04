package com.webcommander.webcommerce

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants

class PaymentGatewayMeta {

    String fieldFor
    String label
    String name
    String value
    String htmlType
    String validation
    String fieldId
    String extraAttrs
    String clazz

    Collection<String> optionLabel = []
    Collection<String> optionValue = []

    static hasMany = [
        optionLabel: String,
        optionValue: String
    ]

    static constraints = {
        value(nullable: true)
        validation(nullable: true)
        fieldId(nullable: true)
        extraAttrs(nullable: true)
        clazz(nullable: true)
        fieldFor unique: "name"
    }

    static mapping = {
    }

    public static void initialize() {
        if (PaymentGatewayMeta.count == 0) {
            PaymentGatewayMeta pplMeta = new PaymentGatewayMeta(
                fieldFor: DomainConstants.PAYMENT_GATEWAY_CODE.PAYPAL,
                label: "mode",
                name: "mode",
                htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.SELECT,
                validation: "required"
            ).save()
            pplMeta.optionLabel = pplMeta.optionValue = ["test", "live"]
            pplMeta.save()
            new PaymentGatewayMeta(
                fieldFor: DomainConstants.PAYMENT_GATEWAY_CODE.PAYPAL,
                label: "email.address",
                name: "emailAddress",
                htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.TEXT,
                validation: "required email"
            ).save()
            new PaymentGatewayMeta(
                fieldFor: DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD,
                label: "credir.card.processor",
                name: "creditCardProcessor",
                htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.SELECT
            ).save()
        }

        if(!PaymentGatewayMeta.findByNameAndFieldFor("cardType", DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD)) {
            PaymentGatewayMeta cardType = new PaymentGatewayMeta(
                    name: "cardType",
                    extraAttrs: 'toggle-target="card-type"',
                    fieldFor: DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD,
                    clazz: "card-type",
                    label: "card.logo.type",
                 htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.SELECT
            ).save()
            cardType.optionLabel = ["credit.card", "custom"]
            cardType.optionValue = ["creditCard", "custom"]
            cardType.save()

            PaymentGatewayMeta creditCards = new PaymentGatewayMeta(
                    fieldFor: DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD,
                    clazz: "card-type-creditCard",
                    label: "credit.cards",
                    name: "creditCards",
                    htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.MULTI_CHECK_BOX
            ).save()

            creditCards.optionValue = ["visa", "master", "amex", "unionpay", "diners"]
            creditCards.optionLabel = ["visa.card", "master.card", "american.express", "china.union.pay", "diners"]
            creditCards.save()

            new PaymentGatewayMeta(
                    fieldFor: DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD,
                    clazz: "card-type-custom",
                    extraAttrs: "file-type='image'",
                    label: "image",
                    name: "customLogo",
                    htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.FILE
            ).save()
        }
    }
}
