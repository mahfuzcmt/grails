package com.webcommander.common

import com.webcommander.AppResourceTagLib
import com.webcommander.ApplicationTagLib
import com.webcommander.admin.Customer
import com.webcommander.admin.Operator
import com.webcommander.config.EmailTemplate
import com.webcommander.config.StoreDetail
import com.webcommander.constants.DomainConstants
import com.webcommander.manager.LicenseManager
import com.webcommander.manager.PathManager
import com.webcommander.parser.EmailTemplateParser
import com.webcommander.tenant.Thread
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import grails.converters.JSON
import grails.util.Holders
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

import javax.activation.MimetypesFileTypeMap

class CommanderMailService {
    def mailService;
    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    ApplicationTagLib app

    @Autowired
    @Qualifier("com.webcommander.AppResourceTagLib")
    AppResourceTagLib appResource

    static transactional = false

    private String[] parseAndFormatMail(String mail) {
        return mail.split(/(,|;)\s*/)
    }

    Map getCommonMacros() {
        def storeDetail = StoreDetail.first()
        Map commonMacros = [
            store_name          : '',
            store_url           : '',
            store_logo          : '',
            store_address       : '',
            store_phone         : '',
            store_mobile        : '',
            store_fax           : '',
            store_email         : '',
            store_city          : '',
            store_country       : '',
            store_state         : '',
            store_post_code     : '',
            link_to_admin_panel : '',
            customer_login_url  : '',
            store_address_line1 : '',
            store_address_line2 : '',
            currency_symbol: ''
        ]

        if (storeDetail == null) {
            throw new ApplicationRuntimeException("store.details.not.found")
        }

        commonMacros.each {
            switch (it.key.toString()) {
                case "store_logo" :
                    it.value = "<img src='${appResource.getStoreLogoURL(storeDetails: storeDetail, isDefault: true, isFullUrl: true)}'>"
                    break;
                case "store_url" :
                    it.value = app.siteBaseUrl();
                    break;
                case "store_name" :
                    it.value = storeDetail.name?: ""
                    break;
                case "store_address" :
                    it.value = storeDetail.address.addressLine1 + ",<br>" + (storeDetail.address.addressLine2 ? storeDetail.address.addressLine2
                            + ",<br>" : "") + (storeDetail.address.city ? storeDetail.address.city + ", " : "") + (storeDetail.address.state ? storeDetail.address.state.name
                            + ", " : "") + " " + (storeDetail.address.postCode ?: "") + "<br>" + (storeDetail.address.country.name ?: "")
                    break;
                case "store_phone" :
                    it.value = storeDetail.address.phone ?: ""
                    break;
                case "store_mobile" :
                    it.value = storeDetail.address.mobile ?: ""
                    break;
                case "store_fax" :
                    it.value = storeDetail.address.fax ?: ""
                    break;
                case "store_email" :
                    it.value = storeDetail.address.email ?: ""
                    break;
                case "store_state" :
                    it.value = storeDetail.address.state.name ?: ""
                    break;
                case "store_city" :
                    it.value = storeDetail.address.city ?: ""
                    break;
                case "store_country" :
                    it.value = storeDetail.address.country.name ?: ""
                    break;
                case "store_post_code" :
                    it.value = storeDetail.address.postCode ?: ""
                    break;
                case "link_to_admin_panel" :
                    it.value = app.siteBaseUrl() + 'admin'
                    break;
                case "customer_login_url" :
                    it.value = app.siteBaseUrl() + 'customer/login'
                    break;
                case "store_address_line1" :
                    it.value = storeDetail.address.addressLine1 ?: "";
                    break;
                case "store_address_line2" :
                    it.value = storeDetail.address.addressLine2 ?: "";
                    break;
                case "currency_symbol" :
                    it.value = AppUtil.baseCurrency.symbol
                    break
            }
        }
        return commonMacros;
    }

    Map getMacrosAndTemplateByIdentifier(String identifier) {
        EmailTemplate emailTemplate = EmailTemplate.findByIdentifier(identifier)
        def macroPath = PathManager.getRestrictedResourceRoot("email-templates/" + identifier + "/macro.json")
        Map bodies = EmailTemplate.getMailBodies(identifier)
        Properties macros = new Properties()
        File macroFile = new File(macroPath)
        JSON.parse(macroFile.text).each {
            macros.setProperty(it.key, "")
        }
        return [emailTemplate: emailTemplate, macros: macros, commonMacros: commonMacros, activeText: bodies.text, activeHtml: bodies.html]
    }

    def sendMail(EmailTemplate emailTemplate, String activeHtml, String activeText, Map macros, String recipient, String sender = null, htmlMacros = [:], textMacros = [:], Boolean testMode = false) {
        sendMail(emailTemplate, activeHtml, activeText, macros, recipient, null, null, sender, htmlMacros, textMacros, testMode)
    }

    def sendMail(EmailTemplate emailTemplate, String activeHtml, String activeText, Map macros, String recipient, String sender = null, htmlMacros = [:], textMacros = [:], List attachments, Boolean testMode = false) {
        sendMail(emailTemplate, activeHtml, activeText, macros, recipient, null, null, sender, attachments, htmlMacros, textMacros, testMode)
    }

    def sendMail(EmailTemplate emailTemplate, String activeHtml, String activeText, Map macros, String recipient, String ccAddress, String bccAddress, String sender = null, htmlMacros = [:], textMacros = [:], Boolean testMode = false) {
        sendMail(emailTemplate, activeHtml, activeText, macros, recipient, ccAddress, bccAddress, sender, null, htmlMacros, textMacros, testMode)
    }

    def sendFormMail(EmailTemplate emailTemplate, String activeHtml, String activeText, Map macros, String recipient, String ccAddress, String bccAddress, String sender, List attachments, htmlMacros = [:], textMacros = [:], Boolean testMode = false){
        sendMail(emailTemplate, activeHtml, activeText, macros, recipient, ccAddress, bccAddress, sender, attachments, htmlMacros, textMacros, testMode)
    }

    def sendMail(EmailTemplate emailTemplate, String activeHtml, String activeText, Map macros, String recipient, String ccAddress, String bccAddress, String sender, List attachments, htmlMacros = [:], textMacros = [:], Boolean testMode = false) {
        def storeDetail = StoreDetail.first();
        def emailConfigs = AppUtil.getConfig("email");
        def qualifiedName
        if(sender) {
            qualifiedName = "${sender}"
        } else {
            qualifiedName = "${storeDetail.name.textify().replaceAll(";", "")} <${(emailConfigs.sender_email)}>"
        }
        return mailService.sendMail {
            mailSender.host = emailConfigs.smtp_host;
            mailSender.username = emailConfigs.smtp_username;
            mailSender.password = emailConfigs.smtp_password;
            mailSender.port = emailConfigs.smtp_port.toInteger();
            if(emailConfigs.smtp_encryption != 'no') {
                mailSender.javaMailProperties.put("mail.smtp.ssl.enable", "false");
                mailSender.javaMailProperties.put("mail.smtp.starttls.enable", "false");
                def encryptionPropKey =  "mail.smtp."+ emailConfigs.smtp_encryption + ".enable";
                mailSender.javaMailProperties.put(encryptionPropKey, "true");
            }
            multipart(emailTemplate.contentType == DomainConstants.EMAIL_CONTENT_TYPE.TEXT_HTML)
            mailSender.javaMailProperties.put("mail.smtp.auth", emailConfigs.has_smtp_authentication);
            to parseAndFormatMail(recipient)
            if(sender) {
                from sender
            } else {
                from storeDetail.name ? qualifiedName : emailConfigs.sender_email
            }
            subject EmailTemplateParser.parse(emailTemplate.subject, macros, new StringBuilder(), testMode);
            if(emailTemplate.contentType == DomainConstants.EMAIL_CONTENT_TYPE.HTML || emailTemplate.contentType == DomainConstants.EMAIL_CONTENT_TYPE.TEXT_HTML)  {
                Map htmls = macros
                if(htmlMacros) {
                    htmls = new LinkedHashMap(macros)
                    htmls << htmlMacros
                }
                html EmailTemplateParser.parse(activeHtml, htmls, new StringBuilder(), testMode);
            }
            if(emailTemplate.contentType == DomainConstants.EMAIL_CONTENT_TYPE.TEXT || emailTemplate.contentType == DomainConstants.EMAIL_CONTENT_TYPE.TEXT_HTML) {
                Map texts = macros
                if(textMacros) {
                    texts = new LinkedHashMap(macros)
                    texts << textMacros
                }
                text EmailTemplateParser.parse(activeText, texts, new StringBuilder(), testMode);
            }
            if(ccAddress) {
                cc parseAndFormatMail(ccAddress)
            }
            if(!emailTemplate.isCcToAdminReadonly && emailTemplate.ccToAdmin) {
                List _bcc = parseAndFormatMail(storeDetail.address.email) as List
                if(bccAddress) {
                    _bcc.add(bccAddress)
                }
                bcc _bcc.toArray(String[])
            } else {
                if(bccAddress) {
                    bcc parseAndFormatMail(bccAddress)
                }
            }
            attachments.each { file ->
                if(file instanceof File) {
                    attachBytes file.getName(), new MimetypesFileTypeMap().getContentType(file), file.getBytes()
                } else {
                    attachBytes file.name, file.contentType, file.byte
                }
            }
        }
    }

    def sendMail(EmailTemplate emailTemplate, String activeHtml, String activeText, Map macros, List<File> attachments, String recipient, String sender = null) {
        sendMail(emailTemplate, activeHtml, activeText, macros, recipient, null, null, sender, attachments)
    }

    def sendResetPasswordMail(Operator recipient, String resetPasswordLink) {
        Map macrosAndTemplate = getMacrosAndTemplateByIdentifier("operator-reset-password")
        if(!macrosAndTemplate.emailTemplate.active) {
            return;
        }
        Map refinedMacros = macrosAndTemplate.commonMacros
        macrosAndTemplate.macros.each {
            switch (it.key.toString()) {
                case "operator_full_name" :
                    refinedMacros[it.key] = recipient.fullName;
                    break;
                case "user_email" :
                    refinedMacros[it.key] = recipient.email
                    break;
                case "password_reset_link" :
                    refinedMacros[it.key] = resetPasswordLink
                    break;
            }
        }
        sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, recipient.email)
    }

    def sendCreateCustomerMail(Customer recipient) {
        def storeDetail = StoreDetail.first();
        Map macrosAndTemplate = getMacrosAndTemplateByIdentifier("create-customer")
        if(!macrosAndTemplate.emailTemplate.active) {
            return;
        }
        Map refinedMacros = macrosAndTemplate.commonMacros
        macrosAndTemplate.macros.each {
            switch (it.key.toString()) {
                case "customer_first_name" :
                    refinedMacros[it.key] = recipient.firstName.encodeAsBMHTML();
                    break;
                case "customer_last_name" :
                    refinedMacros[it.key] = recipient.lastName.encodeAsBMHTML();
                    break;
                case "customer_email" :
                    refinedMacros[it.key] = recipient.userName
                    break;
                case "customer_password" :
                    refinedMacros[it.key] = "change it"
                    break;
                case "store_name" :
                    refinedMacros[it.key] = storeDetail.name
                    break;
            }
        }
        String recipientEmail = recipient.userName;
        Thread.start {
            StoreDetail.withNewSession {
                AppUtil.initialDummyRequest();
                sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, recipientEmail)
            }
        }
    }

    def sendCustomerRegistrationMail(Long customerId) {
        Customer.withNewSession {
            Customer recipient = Customer.proxy(customerId)
            Map macrosAndTemplate = getMacrosAndTemplateByIdentifier("customer-registration")
            if(!macrosAndTemplate.emailTemplate.active) {
                return;
            }
            Map refinedMacros = macrosAndTemplate.commonMacros
            macrosAndTemplate.macros.each {
                switch (it.key.toString()) {
                    case "customer_first_name" :
                        refinedMacros[it.key] = recipient.firstName;
                        break;
                    case "customer_last_name" :
                        refinedMacros[it.key] = recipient.lastName;
                        break;
                    case "customer_email" :
                        refinedMacros[it.key] = recipient.userName
                        break;
                    case "referral_code":
                        refinedMacros[it.key] = recipient.referralCode
                        break
                }
            }
            sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, recipient.userName)
        }
    }

    def sendCreateUserMail(Operator recipient, Map data = [:]) {
        String recipientEmail = recipient.email
        def storeDetail = StoreDetail.first();
        Map macrosAndTemplate = getMacrosAndTemplateByIdentifier("create-operator")
        if(!macrosAndTemplate.emailTemplate.active) {
            return;
        }
        Map refinedMacros = macrosAndTemplate.commonMacros
        macrosAndTemplate.macros.each {
            switch (it.key.toString()) {
                case "operator_full_name" :
                    refinedMacros[it.key] = recipient.fullName;
                    break;
                case "operator_password" :
                    refinedMacros[it.key] =  data.exists ? (LicenseManager.isProvisionActive() ? null : "change it") : data.password
                    break;
                case "store_name" :
                    refinedMacros[it.key] = storeDetail.name
                    break;
                case "user_name":
                    refinedMacros[it.key] = recipient.email
                    break;
                case "confirm_url":
                    refinedMacros[it.key] = LicenseManager.isProvisionActive() ? LicenseManager.ssoURL() + "/user/confirm?token=" + data.token : null
                    break
            }
        }
        Thread.start {
            Operator.withNewSession {
                AppUtil.initialDummyRequest();
                sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, recipientEmail)
            }
        }
    }

    def sendApproveCustomerMailToAdmin(Customer customer) {
        def storeDetail = StoreDetail.first();
        Map macrosAndTemplate = getMacrosAndTemplateByIdentifier("customer-restricted-registration-notification")
        if(!macrosAndTemplate.emailTemplate.active) {
            return;
        }
        Map refinedMacros = macrosAndTemplate.commonMacros
        macrosAndTemplate.macros.each {
            switch (it.key.toString()) {
                case "customer_name" :
                    refinedMacros[it.key] = customer.firstName
                    break;
                case "customer_email" :
                    refinedMacros[it.key] = customer.address.email
                    break;
                case "customer_address" :
                    refinedMacros[it.key] = customer.address.addressLine1
                    break
                case "customer_mobile" :
                    refinedMacros[it.key] = customer.address.mobile
                    break
                case "store_admin_name" :
                    refinedMacros[it.key] = storeDetail.address.firstName
                    break
            }
        }
        sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, storeDetail.address.email)
    }

    def sendApprovalMailToCustomer(Customer customer) {
        def storeDetail = StoreDetail.first();
        Map macrosAndTemplate = getMacrosAndTemplateByIdentifier("customer-restricted-registration-approval")
        if(!macrosAndTemplate.emailTemplate.active) {
            return;
        }
        Map refinedMacros = macrosAndTemplate.commonMacros
        macrosAndTemplate.macros.each {
            switch (it.key.toString()) {
                case "customer_first_name" :
                    refinedMacros[it.key] = customer.firstName;
                    break;
                case "customer_last_name" :
                    refinedMacros[it.key] = customer.lastName;
                    break;
                case "customer_email" :
                    refinedMacros[it.key] = customer.userName
                    break;
                case "customer_address" :
                    refinedMacros[it.key] = customer.address.addressLine1
                    break;
                case "customer_city" :
                    refinedMacros[it.key] = customer.address.city
                    break;
                case "customer_postCode" :
                    refinedMacros[it.key] = customer.address.postCode
                    break;
                case "customer_phone" :
                    refinedMacros[it.key] = customer.address.phone
                    break;

            }
        }
        sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, customer.address.email, storeDetail.address.email)
    }

    def sendTestMail(Map data, String recipient) {
        Map macros = getCommonMacros()
        sendMail(data.template, data.html, data.text, macros, recipient, null, [:], [:] , true)
    }
}
