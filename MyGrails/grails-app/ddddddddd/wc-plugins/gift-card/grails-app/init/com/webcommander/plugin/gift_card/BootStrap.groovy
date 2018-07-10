package com.webcommander.plugin.gift_card

import com.webcommander.admin.ConfigService
import com.webcommander.config.EmailTemplate
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.LicenseConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.design.ProductWidgetService
import com.webcommander.manager.HookManager
import com.webcommander.models.ProductData
import com.webcommander.payment.DefaultPaymentService
import com.webcommander.plugin.gift_card.constants.DomainConstants as GCDC
import com.webcommander.plugin.gift_card.mixin_service.ProductWidgetService as CPPS
import com.webcommander.plugin.gift_card.services.DefaultPaymentService as DPS
import com.webcommander.plugin.gift_card.webcommerce.GiftCard
import com.webcommander.tenant.TenantContext
import com.webcommander.util.AppUtil
import com.webcommander.util.PluginDestroyUtil
import com.webcommander.webcommerce.OrderItem
import com.webcommander.webcommerce.PaymentGateway
import com.webcommander.webcommerce.Product
import grails.util.Holders

class BootStrap {

    private final String GIFT_CARD = "gift_card"
    private final String gift_card = "giftCard"
    private final String GIFT_CARD_SELL_INFO = "giftCardSellInfo"
    private final String PAYMENT_GATEWAY_CODE = "GCRD"
    private final String CART_OBJECT_TYPES = "gift.card"
    private final String GIFT_CARD_LABEL = "gift_card_label"
    private final String GIFT_CARD_DEFAULT_LABEL = "gift_card_default_label"
    private final String GIFT_CARD_ACTIVE = "gift_card_active"
    private final String GIFT_CARD_ORDER = "gift_card_order"

    ConfigService configService

    List domain_constants = [
            [constant: "SITE_CONFIG_TYPES", key: "GIFT_CARD", value: GIFT_CARD],
            [constant: "PRODUCT_WIDGET_TYPE", key: "GIFT_CARD_SELL_INFO", value: GIFT_CARD_SELL_INFO],
            [constant: "PAYMENT_GATEWAY_CODE", key: "GIFT_CARD", value: PAYMENT_GATEWAY_CODE],
            [constant: "PRODUCT_TYPE", key: GCDC.GIFT_CARD_EMAIL_TEMPLATES.RECIPIENT_NOTIFICATION, value: GIFT_CARD],
            [constant: "PRODUCT_TYPE", key: "GIFT_CARD", value: GIFT_CARD],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: GIFT_CARD, value: true],
            [constant: "ECOMMERCE_EMAIL_TEMPLATE_CHECKLIST", key: "gift_card_recipient", value: true],
            [constant: "DEFAULT_PAYMENT_GATE_WAYS", key: "GIFT_CARD", value: [
                    ORDER               : 2,
                    IDENTIFIER          : "GiftCard",
                    PAYMENT_GATEWAY_CODE: PAYMENT_GATEWAY_CODE]
            ]
    ]

    List named_constants = [
            [constant: "PRODUCT_WIDGET_MESSAGE_KEYS", key: GIFT_CARD_SELL_INFO + ".title", value: "gift.card.sell.info"],
            [constant: "PRODUCT_WIDGET_MESSAGE_KEYS", key: GIFT_CARD_SELL_INFO + ".label", value: "gift.card.sell.info"],
            [constant: "PRODUCT_TYPE", key: GIFT_CARD, value: CART_OBJECT_TYPES],
            [constant: "CART_OBJECT_TYPES", key: "GIFT_CARD", value: CART_OBJECT_TYPES],
    ]

    List site_config_constants = [
            [constant: DomainConstants.SITE_CONFIG_TYPES.MY_ENTITLEMENTS, key: GIFT_CARD_LABEL, value: "My Gift Cards"],
            [constant: DomainConstants.SITE_CONFIG_TYPES.MY_ENTITLEMENTS, key: GIFT_CARD_DEFAULT_LABEL, value: "My Gift Cards"],
            [constant: DomainConstants.SITE_CONFIG_TYPES.MY_ENTITLEMENTS, key: GIFT_CARD_ACTIVE, value: "true"]
    ]

    List license_constants = [
            [constant: "PAYMENT_GATEWAY", key: PAYMENT_GATEWAY_CODE, value: "allow_gift_card_feature"]
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        NamedConstants.addConstant(named_constants)
        LicenseConstants.addConstant(license_constants)
        ConfigService.addTab(gift_card, [
                url: "giftCardAdmin/loadSettings", message_key: "gift.card", ecommerce: true
        ])

        if (!PaymentGateway.findByCode(PAYMENT_GATEWAY_CODE)) {
            new PaymentGateway(
                    name: "gift.card",
                    code: PAYMENT_GATEWAY_CODE,
                    isPromotional: true
            ).save()
        }
        if (!EmailTemplate.findByIdentifier(GCDC.GIFT_CARD_EMAIL_TEMPLATES.RECIPIENT_NOTIFICATION)) {
            Map emailTemplate = [
                    label     : "gift.card.notification",
                    identifier: GCDC.GIFT_CARD_EMAIL_TEMPLATES.RECIPIENT_NOTIFICATION,
                    subject   : "%gift_sender_name% has sent you a Gift Card of %gift_amount%",
                    type      : DomainConstants.EMAIL_TYPE.CUSTOMER
            ]
            new EmailTemplate(emailTemplate).save()
        }
        site_config_constants.each { it ->
            if (!SiteConfig.findAllByTypeAndConfigKey(it.constant, it.key)) {
                new SiteConfig(type: it.constant, configKey: it.key, value: it.value).save()
                AppUtil.clearConfig it.constant
            }
        }

        GCDC.GIFT_CARD_SITE_CONFIGS.each { configKey, value ->
            if (!SiteConfig.findAllByTypeAndConfigKey(GIFT_CARD, configKey)) {
                new SiteConfig(type: GIFT_CARD, configKey: configKey, value: value).save()
                AppUtil.clearConfig(GIFT_CARD)
            }
        }

        if (!SiteConfig.findAllByTypeAndConfigKey(DomainConstants.SITE_CONFIG_TYPES.MY_ENTITLEMENTS, GIFT_CARD_ORDER)) {
            configService.setOrder(DomainConstants.SITE_CONFIG_TYPES.MY_ENTITLEMENTS, GIFT_CARD_ORDER)
            AppUtil.clearConfig DomainConstants.SITE_CONFIG_TYPES.MY_ENTITLEMENTS
            AppUtil.clearConfig DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_PROFILE_PAGE
        }
    }

    def tenantDestroy = { tenant ->
        ConfigService.removeTab(gift_card)
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeAutoGeneratePageWidget(NamedConstants.AUTO_GENERATED_PAGE_WIDGET.PRODUCT_WIDGET, GIFT_CARD_SELL_INFO)
            util.removeEmailTemplates("gift-card-recipient")
            util.removeSiteConfig(GIFT_CARD)
            util.removePaymentGateway(PAYMENT_GATEWAY_CODE)
            DomainConstants.removeConstant(site_config_constants)
            LicenseConstants.removeConstant(license_constants)
            NamedConstants.removeConstant(named_constants)
            DomainConstants.removeConstant(domain_constants)
            site_config_constants.each { it ->
                util.removeSiteConfig(it.constant, it.key)
            }
            util.removeSiteConfig(DomainConstants.SITE_CONFIG_TYPES.MY_ENTITLEMENTS, GIFT_CARD_ORDER)
            configService.reorderFields(DomainConstants.SITE_CONFIG_TYPES.MY_ENTITLEMENTS, GIFT_CARD_ORDER)
            AppUtil.clearConfig DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_PROFILE_PAGE
        } catch (Exception e) {
            log.error "Could Not Deactivate Plugin gift-card From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }

    }

    def init = { servletContext ->
        Holders.grailsApplication.mainContext.getBean(DefaultPaymentService).metaClass.mixin DPS
        Holders.grailsApplication.mainContext.getBean(ProductWidgetService).metaClass.mixin CPPS
        TenantContext.eachParallelWithWait(tenantInit)

        HookManager.register("getCartParamsFromOrderItem", { Map params, OrderItem orderItem ->
            GiftCard card
            if (orderItem.productType == CART_OBJECT_TYPES && (card = GiftCard.get(orderItem.itemId))) {
                params.cardType = card.type
                params.recipientName = card.recipientName
                params.recipientEmail = card.recipientEmail
                params.senderName = card.senderName
                params.senderEmail = card.senderEmail
                params.amount = card.amount
                params.message = card.message
            }
            return params
        });

        HookManager.register("productCartAdd", { response, ProductData productData, Product product, params ->
            if (productData.productType == GIFT_CARD && !params.gift_card) {
                response.blocks.add([label: "choose.required.options", requiresKey: "giftCardSellInfo"])
            }
            return response
        })
    }
}