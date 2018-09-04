package com.webcommander.plugin.ask_question

import com.webcommander.admin.ConfigService
import com.webcommander.config.EmailTemplate
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil

import javax.servlet.ServletContext
import com.webcommander.plugin.PluginMeta

import java.util.logging.Level

class BootStrap {

    private static final ASK_QUESTION = "ask_question"

    List domain_constants = [
            [constant:"SITE_CONFIG_TYPES", key: "ASK_QUESTION", value: ASK_QUESTION],
            [constant:"EMAIL_TYPE", key: "ASK_QUESTION", value: ASK_QUESTION],
            [constant:"ECOMMERCE_EMAIL_TYPE_CHECKLIST", key: ASK_QUESTION, value: true],
            [constant:"ECOMMERCE_EMAIL_TEMPLATE_CHECKLIST", key: "product_question", value: true],
            [constant:"ECOMMERCE_DASHLET_CHECKLIST", key: "ask_question", value: true],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: ASK_QUESTION, value: true],
    ]

    List named_constants = [
            [constant:"EMAIL_SETTING_MESSAGE_KEYS", key: ASK_QUESTION, value: "ask.question"]
    ]

    List templates = [
        [
            label: "product.question.answer",
            identifier: "product-question-answer",
            subject: "Reply to your question",
            isActiveReadonly: true,
            type: ASK_QUESTION
        ],
        [
            label: "product.question",
            identifier: "product-question",
            subject: "A customer has asked a question on %product_name%",
            isActiveReadonly: false,
            active: false,
            type: DomainConstants.EMAIL_TYPE.ADMIN
        ]
    ]

    Map initialData = [
        ask_question: "on"
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        NamedConstants.addConstant(named_constants)

        ConfigService.addTab(ASK_QUESTION,[
            url: "askQuestionAdmin/config",
            message_key: "ask.question",
            ecommerce  : true
        ])

        templates.each { template ->
            if (!EmailTemplate.findAllByIdentifier(template.identifier)) {
                new EmailTemplate(template).save()
            }
        }

        if (SiteConfig.countByType(ASK_QUESTION) == 0) {
            initialData.each { entry ->
                new SiteConfig(type: ASK_QUESTION, configKey: entry.key, value: entry.value).save()
            }
        }
    }

    def tenantDestroy = { tenant ->
        ConfigService.removeTab(ASK_QUESTION)
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeSiteConfig(DomainConstants.SITE_CONFIG_TYPES.ASK_QUESTION)
            util.removeEmailTemplates(*templates.identifier)
            NamedConstants.removeConstant(named_constants)
            DomainConstants.removeConstant(domain_constants)
        } catch (Exception e) {
            log.log Level.SEVERE, "Could Not Deactivate Plugin ask-question From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        TenantContext.eachParallelWithWait(tenantInit)
    }
}
