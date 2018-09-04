package com.webcommander.plugin.quote

import com.webcommander.acl.Permission
import com.webcommander.acl.RolePermission
import com.webcommander.admin.ConfigService
import com.webcommander.admin.MessageSource
import com.webcommander.admin.Role
import com.webcommander.config.EmailTemplate
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil

class BootStrap {

    private final String QUOTE = "quote"

    List domain_constants = [
            [constant:"SITE_CONFIG_TYPES", key: "QUOTE", value: QUOTE],
            [constant:"ECOMMERCE_EMAIL_TEMPLATE_CHECKLIST", key: "get_quote", value: true],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: QUOTE, value: true],
    ]

    Map config = [
            enabled: "true",
            button_label: "s:get.quote"
    ]
    Map site_message = [
            "get.quote": "Get Quote"
    ]

    Map emailTemplate = [
            label: "get.quote",
            identifier: "get-quote",
            subject: "Thanks for requesting a quotation. Your quote (%quote_id%) details are here",
            isActiveReadonly: true,
            type: DomainConstants.EMAIL_TYPE.CUSTOMER
    ]
    List permissions = [
            ["view.list", false],
            ["send", false],
            ["view", false],
            ["manage", false]
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        ConfigService.addTab(QUOTE, [
                url: "quoteAdmin/loadConfig",
                message_key: "quote",
                ecommerce  : true
        ])
        site_message.each {
            if(!MessageSource.findByLocaleAndMessageKey("all", it.key))
                new MessageSource(locale: "all", messageKey: it.key, message: it.value).save()
        }
        if(!SiteConfig.findByType(QUOTE)) {
            config.each {
                new SiteConfig(type: QUOTE, configKey: it.key, value: it.value).save()
            }
        }
        if(!EmailTemplate.findByIdentifier(emailTemplate.identifier)) {
            new EmailTemplate(emailTemplate).save()
        }
        if(!Permission.findByType("quote")) {
            Role role = Role.findByName("Admin")
            permissions.each { entry ->
                Permission permission = new Permission(name: entry[0], label: entry[0], applicableOnEntity: entry[1], type: "quote").save()
                new RolePermission(role: role, permission: permission, isAllowed: true).save()
            }
        }
    }

    def tenantDestroy = { tenant ->
        ConfigService.removeTab(QUOTE)
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeSiteMessage("get.quote")
            util.removeSiteConfig(QUOTE)
            util.removeEmailTemplates(emailTemplate.identifier)
            util.removePermission(QUOTE)
            DomainConstants.removeConstant(domain_constants)
        } catch(Exception e) {
            log.error "Could Not Deactivate Plugin quote From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }

    }

    def init = { servletContext ->
        TenantContext.eachParallelWithWait(tenantInit)
    }
}
