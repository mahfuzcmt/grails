package com.webcommander.config

import com.webcommander.AppResourceTagLib
import com.webcommander.common.FileService
import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil

class EmailTemplate {

    Long id

    Date updated

    String label
    String identifier
    String contentType = DomainConstants.EMAIL_CONTENT_TYPE.HTML
    String subject = ""
    String type

    Boolean active = true
    Boolean isActiveReadonly = false
    Boolean ccToAdmin = false
    Boolean isCcToAdminReadonly = true

    def beforeValidate() {
        if (!this.updated) {
            this.updated = new Date().gmt()
        }
    }

    def beforeUpdate() {
        this.updated = new Date().gmt()
    }

    static constraints = {
        identifier unique: true
    }

    static Map getMailBodies(String identifier) {
        FileService fileService = AppUtil.getBean(FileService)
        String htmlRelativePath = "${AppResourceTagLib.EMAIL_TEMPLATES}/${identifier}/${AppResourceTagLib.DEFAULT_HTML}"
        String textRelativePath = "${AppResourceTagLib.EMAIL_TEMPLATES}/${identifier}/${AppResourceTagLib.DEFAULT_TXT}"
        InputStream htmlStream = fileService.getModifiableResourceStream(htmlRelativePath)
        InputStream textStream
        if(htmlStream) {
            textStream = fileService.getModifiableResourceStream(textRelativePath)
        } else {
            htmlStream = fileService.getRestrictedResourceStream(htmlRelativePath)
            textStream = fileService.getRestrictedResourceStream(textRelativePath)
        }
        String html = htmlStream.getText("UTF-8")
        String text = textStream.getText("UTF-8")
        htmlStream.close()
        textStream.close()
        return [html: html, text: text]
    }

    static void initialize() {
        def templates = [
            [
                label: "payment.success",
                identifier: "payment-success",
                subject: "Thank you for your complete payment for the order %order_id%",
                isCcToAdminReadonly: false,
                type: DomainConstants.EMAIL_TYPE.PAYMENT
            ],
            [
                label: "payment.pending",
                identifier: "payment-pending",
                subject: "Your payment is pending for the order %order_id%",
                isCcToAdminReadonly: false,
                type: DomainConstants.EMAIL_TYPE.PAYMENT
            ],
            [
                label: "recommend.friend",
                identifier: "tell-friend",
                subject: "Your friend has recommended this URL",
                isActiveReadonly: true,
                isCcToAdminReadonly: false,
                type: DomainConstants.EMAIL_TYPE.CUSTOMER
            ],
            [
                label: "invoice",
                identifier: "send-invoice",
                subject: "Your order details are here",
                isActiveReadonly: true,
                type: DomainConstants.EMAIL_TYPE.ORDER
            ],
            [
                label: "order.creation",
                identifier: "create-order",
                subject: "Your order has been created",
                isCcToAdminReadonly: false,
                type: DomainConstants.EMAIL_TYPE.ORDER
            ],
            [
                label: "partial.shipment",
                identifier: "partial-shipment",
                subject: "Your order (%order_id%) is partially shipped",
                isCcToAdminReadonly: false,
                type: DomainConstants.EMAIL_TYPE.ORDER
            ],
            [
                label: "shipment.complete",
                identifier: "shipment-complete",
                subject: "Your order (%order_id%) has been shipped",
                isCcToAdminReadonly: false,
                type: DomainConstants.EMAIL_TYPE.ORDER
            ],
            [
                label: "operator.password.reset",
                identifier: "operator-reset-password",
                subject: "Password reset link for your request",
                isActiveReadonly: true,
                type: DomainConstants.EMAIL_TYPE.ADMIN
            ],
            [
                label: "customer.reset.password",
                identifier: "customer-reset-password",
                subject: "Password reset link for your request",
                isActiveReadonly: true,
                type: DomainConstants.EMAIL_TYPE.CUSTOMER
            ],
            [
                label: "customer.creation",
                identifier: "create-customer",
                subject: "Your account has been created",
                type: DomainConstants.EMAIL_TYPE.CUSTOMER
            ],
            [
                label: "customer.registration",
                identifier: "customer-registration",
                subject: "%store_name%: Customer Registration.",
                type: DomainConstants.EMAIL_TYPE.CUSTOMER
            ],
            [
                label: "operator.creation",
                identifier: "create-operator",
                subject: "Your account has been created",
                type: DomainConstants.EMAIL_TYPE.ADMIN
            ],
            [
                label: "confirmation.customer.creation.in.restricted.mode",
                identifier: "customer-restricted-registration-notification",
                subject: "Awaiting approval for Customer Registration (Admin Approval)",
                isCcToAdminReadonly: false,
                type: DomainConstants.EMAIL_TYPE.ADMIN
            ],
            [
                label: "admin.approval.during.customer.creation.restricted.mode",
                identifier: "customer-restricted-registration-approval",
                subject: "Your account has been approved",
                isActiveReadonly: true,
                type: DomainConstants.EMAIL_TYPE.CUSTOMER
            ],
            [
                label: "request.for.store.credit",
                identifier: "store-credit-request",
                subject: "%customer_name% has requested for Store Credit.",
                isActiveReadonly: true,
                type: DomainConstants.EMAIL_TYPE.ADMIN
            ],
            [
                label: "newsletter.subscription.notification",
                identifier: "newsletter-subscription-notification",
                subject: "%store_name%: Newsletter Subscription",
                isCcToAdminReadonly: false,
                type: DomainConstants.EMAIL_TYPE.CUSTOMER
            ],
            [
                label: "customer.order.comment.notification",
                identifier: "customer-order-comment-notification",
                subject: "%customer_name% has sent a message against Order NO. %order_id%",
                isActiveReadonly: true,
                type: DomainConstants.EMAIL_TYPE.ADMIN
            ],
            [
                label: "admin.order.comment.notification",
                identifier: "admin-order-comment-notification",
                subject: "%customer_name%, you have a new message for your order",
                isActiveReadonly: true,
                type: DomainConstants.EMAIL_TYPE.CUSTOMER
            ],
            [
                label: "license.validation.failure",
                identifier: "license-validation-failed",
                subject: "couldn't verify your license",
                isActiveReadonly: false,
                type: DomainConstants.EMAIL_TYPE.LICENSE
            ]
        ]
        if(EmailTemplate.count() == 0) {
            templates.each {
                new EmailTemplate(it).save()
            }
        }
    }
}
