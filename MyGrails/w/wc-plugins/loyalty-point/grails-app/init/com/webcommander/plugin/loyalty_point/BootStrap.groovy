package com.webcommander.plugin.loyalty_point

import com.webcommander.admin.ConfigService
import com.webcommander.admin.Customer
import com.webcommander.config.EmailTemplate
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.LicenseConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.design.ProductWidgetService
import com.webcommander.events.AppEventManager
import com.webcommander.manager.HookManager
import com.webcommander.models.Cart
import com.webcommander.payment.DefaultPaymentService
import com.webcommander.plugin.loyalty_point.constants.DomainConstants as LDC
import com.webcommander.plugin.loyalty_point.mixin_service.DefaultPaymentService as DPS
import com.webcommander.plugin.loyalty_point.mixin_service.ProductWidgetService as LPWS
import com.webcommander.tenant.TenantContext
import com.webcommander.util.AppUtil
import com.webcommander.util.PluginDestroyUtil
import com.webcommander.webcommerce.PaymentGateway
import com.webcommander.webcommerce.PaymentGatewayMeta
import grails.util.Holders

class BootStrap {

    private static final String LOYALTY_POINT = "loyaltyPoint"
    private static final String LOYALTY_UNDERSCORE_POINT = "loyalty_point"
    private static final String CONFIG_TYPE = "loyalty-point"
    private static final String LPP = "LPP"
    private static final String LOYALTY_POINT_LABEL = "loyalty_point_label"
    private static final String LOYALTY_POINT_ACTIVE = "loyalty_point_active"
    private static final String LOYALTY_POINT_DEFAULT_LABEL = "loyalty_point_default_label"
    private static final String LOYALTY_POINT_ORDER = "loyalty_point_order"

    ConfigService configService

    List domain_constants = [
            [constant: "SITE_CONFIG_TYPES", key: "LOYALTY_POINT", value: CONFIG_TYPE],
            [constant: "PAYMENT_GATEWAY_CODE", key: "LOYALTY_POINT", value: LPP],
            [constant: "PRODUCT_WIDGET_TYPE", key: "LOYALTY_POINT", value: LOYALTY_POINT],
            [constant: "ECOMMERCE_EMAIL_TEMPLATE_CHECKLIST", key: "loyalty_point_reward_notification", value: true],
            [constant: "ECOMMERCE_EMAIL_TEMPLATE_CHECKLIST", key: "invite_friend", value: true],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: LOYALTY_UNDERSCORE_POINT, value: true],
            [constant: "DEFAULT_PAYMENT_GATE_WAYS", key: "LOYALTY_POINT", value: [
                    ORDER               : 3,
                    IDENTIFIER          : "LoyaltyPoint",
                    PAYMENT_GATEWAY_CODE: LPP
            ]],
    ]

    List site_config_constants = [
            [constant: DomainConstants.SITE_CONFIG_TYPES.MY_ENTITLEMENTS, key: LOYALTY_POINT_LABEL, value: "My Loyalty Points"],
            [constant: DomainConstants.SITE_CONFIG_TYPES.MY_ENTITLEMENTS, key: LOYALTY_POINT_DEFAULT_LABEL, value: "My Loyalty Points"],
            [constant: DomainConstants.SITE_CONFIG_TYPES.MY_ENTITLEMENTS, key: LOYALTY_POINT_ACTIVE, value: "true"],
            [constant: DomainConstants.SITE_CONFIG_TYPES.MY_ACCOUNT_PAGE, key: LOYALTY_UNDERSCORE_POINT, value: "false"]
    ]

    List named_constants = [
            [constant: "PRODUCT_WIDGET_MESSAGE_KEYS", key: LOYALTY_POINT + ".title", value: "loyalty.point.widget"],
            [constant: "PRODUCT_WIDGET_MESSAGE_KEYS", key: LOYALTY_POINT + ".label", value: "loyalty.point"]
    ]

    List license_constants = [
            [constant: "PAYMENT_GATEWAY", key: LPP, value: "allow_loyalty_program_feature"]
    ]


    Map initData = SiteConfig.INITIAL_DATA[CONFIG_TYPE] = [
            product_widget_text                             : "You'll earn %point_per_product% point(s) for each quantity of this product",
            is_enabled                                      : "false",
            earning_enabled                                 : "true",
            show_in_cart                                    : "true",
            point_policy                                    : LDC.POINT_POLICY.HIGHEST_IN_PRODUCT_AND_CATEGORIES,
            conversion_rate_earning                         : "0.5",
            enable_store_credit                             : "true",
            conversion_rate_store_credit                    : "0.5",
            conversion_rate_payment                         : "1",
            enable_expire                                   : "never",
            expire_in_value                                 : "1",
            expire_in_offset                                : LDC.EXPIRE_IN_OFFSET.MONTHS,
            apply_by_default                                : "true",
            enable_purchase_point                           : "false",
            on_purchase_amount                              : "0",
            enable_product_review_point                     : "false",
            on_product_review_amount                        : "0",
            enable_signup_registration_point                : "false",
            on_signup_registration_amount                   : "0",
            on_facebook_share_amount                        : "10",
            on_twitter_share_amount                         : "10",
            on_googleplus_share_amount                      : "10",
            on_linkedin_share_amount                        : "10",
            purchase_valid_for                              : "every",
            on_first_purchase_amount                        : "0",
            enable_referral                                 : "false",
            enable_refer_product                            : "false",
            refer_product_on_purchase_referrer_loyalty_point: "0",
            refer_product_on_purchase_referree_loyalty_point: "0",
            enable_refer_customer                           : "false",
            refer_customer_on_signup_referrer_loyalty_point : "0",
            refer_customer_on_signup_referree_loyalty_point : "0",
            enable_send_referral_code_with_mail             : "false"
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        NamedConstants.addConstant(named_constants)
        LicenseConstants.addConstant(license_constants)

        ConfigService.addTab(LOYALTY_POINT, [
                url        : "loyaltyPointAdmin/loadAppView",
                message_key: "loyalty.point",
                license    : "allow_loyalty_program_feature",
                ecommerce  : true
        ])


        if (!EmailTemplate.findByIdentifier("loyalty-point-reward-notification")) {
            Map emailTemplate = [
                    label           : "loyalty.point.reward.notification",
                    identifier      : "loyalty-point-reward-notification",
                    subject         : "Congratulations!! Loyalty Point has been Redeemed",
                    type            : DomainConstants.EMAIL_TYPE.CUSTOMER,
                    isActiveReadonly: true
            ]
            new EmailTemplate(emailTemplate).save()
        }

        if (!EmailTemplate.findByIdentifier("invite-friend")) {
            Map emailTemplate = [
                    label           : "invite.a.friend",
                    identifier      : "invite-friend",
                    subject         : "Invitation",
                    type            : DomainConstants.EMAIL_TYPE.CUSTOMER,
                    isActiveReadonly: true
            ]
            new EmailTemplate(emailTemplate).save()
        }

        if (PaymentGateway.countByCode(LPP) == 0) {
            new PaymentGateway(
                    code: LPP,
                    name: "loyalty.point",
                    isPromotional: true
            ).save()
        }

        if (PaymentGatewayMeta.countByFieldFor(DomainConstants.PAYMENT_GATEWAY_CODE.LOYALTY_POINT) == 0) {
            new PaymentGatewayMeta(fieldFor: DomainConstants.PAYMENT_GATEWAY_CODE.LOYALTY_POINT, label: "for.each.100.loyalty.points", name: "conversionRate", htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.TEXT, value: "1", validation: 'required number gt[0]', extraAttrs: "retrict='decimal'").save()
        }

        site_config_constants.each { it ->
            if (!SiteConfig.findAllByTypeAndConfigKey(it.constant, it.key)) {
                new SiteConfig(type: it.constant, configKey: it.key, value: it.value).save()
                AppUtil.clearConfig it.constant
            }
        }
        if (!SiteConfig.findAllByTypeAndConfigKey(DomainConstants.SITE_CONFIG_TYPES.MY_ENTITLEMENTS, LOYALTY_POINT_ORDER)) {
            configService.setOrder(DomainConstants.SITE_CONFIG_TYPES.MY_ENTITLEMENTS, LOYALTY_POINT_ORDER)
            AppUtil.clearConfig DomainConstants.SITE_CONFIG_TYPES.MY_ENTITLEMENTS
            AppUtil.clearConfig DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_PROFILE_PAGE
        }
    }

    def tenantDestroy = { tenant ->
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            ConfigService.removeTab(LOYALTY_POINT)
            util.removeAutoGeneratePageWidget(NamedConstants.AUTO_GENERATED_PAGE_WIDGET.PRODUCT_WIDGET, LOYALTY_POINT)
            util.removeEmailTemplates("loyalty-point-reward-notification", "invite-friend")
            site_config_constants.each { it ->
                util.removeSiteConfig(it.constant, it.key)
            }
            util.removePaymentMeta(CONFIG_TYPE)
            util.removePaymentGateway(LPP)
            LicenseConstants.removeConstant(license_constants)
            NamedConstants.removeConstant(named_constants)
            DomainConstants.removeConstant(domain_constants)
            util.removeSiteConfig(DomainConstants.SITE_CONFIG_TYPES.MY_ENTITLEMENTS, LOYALTY_POINT_ORDER)
            configService.reorderFields(DomainConstants.SITE_CONFIG_TYPES.MY_ENTITLEMENTS, LOYALTY_POINT_ORDER)
            AppUtil.clearConfig DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_PROFILE_PAGE
        } catch (Exception e) {
            log.error "Could Not Deactivate Plugin loyalty-point From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        Holders.grailsApplication.mainContext.getBean(ProductWidgetService).metaClass.mixin LPWS;
        Holders.grailsApplication.mainContext.getBean(DefaultPaymentService).metaClass.mixin DPS;
        initData.each { entry ->
            site_config_constants.add([constant: CONFIG_TYPE, key: entry.key, value: entry.value])
        }
        TenantContext.eachParallelWithWait(tenantInit)
        AppEventManager.on("cart-cleared cart-removed cart-item-add cart-item-quantity-update cart-modified", { cart ->
            def defaultPaymentService = Holders.grailsApplication.mainContext.getBean("defaultPaymentService");
            defaultPaymentService.removeLoyaltyPointCachedAmount(cart.sessionId)
        })

        HookManager.register("order-mail-macros", { Map macros, Long orderId, String identifier ->
            def loyaltyPointService = Holders.applicationContext.getBean("loyaltyPointService");
            Long loyaltyPoint = 0;
            def messageSource = Holders.applicationContext.getBean('messageSource');
            String code = "";
            if (identifier == "create-order") {
                loyaltyPoint = loyaltyPointService.findLoyaltyPointFromOrder(orderId);
                code = "you.may.have.earned.n.loyalty.point.from.order";
            } else if (identifier == "payment-success") {
                loyaltyPoint = loyaltyPointService.findPointFromHistory(orderId);
                code = "you.have.earned.n.loyalty.point.from.order";
            } else if (identifier == "shipment-complete") {
                loyaltyPoint = loyaltyPointService.findPointFromHistory(orderId);
                code = "you.have.earned.n.loyalty.point.from.order";
            }
            String messageBody = messageSource.getMessage(code, [loyaltyPoint] as Object[], code, Locale.default);
            List messages = macros.custom_message;
            macros.custom_message = messages ? messages.add(messageBody) : [messageBody];
            return macros
        });

        AppEventManager.on("before-customer-create", { Customer customer ->
            String usedReferralCode = AppUtil.params.reference_number, howDoYouKnow = AppUtil.params.how_do_you_know
            def configs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT)
            if (configs.enable_referral && configs.enable_refer_customer) {
                customer.referralCode = String.format("%08x", customer.userName.hashCode())
                if (usedReferralCode) {
                    customer.usedReferralCode = usedReferralCode
                }
                if (howDoYouKnow) {
                    customer.howDoYouKnow = howDoYouKnow
                }
            }
            return customer;
        });

        AppEventManager.on("customer-create", { Customer customer ->
            String usedReferralCode = AppUtil.params.reference_number
            def configs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT)
            Customer referralCustomer = Customer.findByReferralCode(usedReferralCode)
            if (configs.enable_referral && configs.enable_refer_customer && referralCustomer) {
                int used = referralCustomer.countReferralCodeUsed
                referralCustomer.countReferralCodeUsed = used + 1;
                PointHistory referreeHistory = new PointHistory([
                        customer     : customer,
                        pointCredited: configs.refer_customer_on_signup_referree_loyalty_point.toLong(),
                        comment      : "${customer.id}",
                        type         : com.webcommander.plugin.loyalty_point.constants.NamedConstants.RULE_TYPE.ON_SIGNUP_REFERRER
                ])
                referreeHistory.save()

                PointHistory referrerHistory = new PointHistory([
                        customer     : referralCustomer,
                        pointCredited: configs.refer_customer_on_signup_referrer_loyalty_point.toLong(),
                        comment      : "${referralCustomer.id}",
                        type         : com.webcommander.plugin.loyalty_point.constants.NamedConstants.RULE_TYPE.ON_SIGNUP_REFERRAL
                ])
                referrerHistory.save()
                return !referreeHistory.hasErrors() && !referrerHistory.hasErrors()
            }
        });

        AppEventManager.on("order-confirm", { Cart cart ->
            Long orderId = cart.orderId
            def loyaltyPointService = Holders.applicationContext.getBean("loyaltyPointService");
            def configs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT)
            String referralCode = AppUtil.session.referralCode
            if (configs.enable_referral && referralCode) {
                def result = loyaltyPointService.saveOrderReferral(orderId, referralCode);
                if (result) {
                    AppUtil.session.referralCode = null;
                }
            }
        });

        AppEventManager.on("before-cart-details-load before-confirm-step-load", { Map params ->
            def loyaltyPointService = Holders.applicationContext.getBean("loyaltyPointService");
            if (params.referralCode) {
                loyaltyPointService.applyReferralCode(params.referralCode.trim());
            }
        });
    }
}