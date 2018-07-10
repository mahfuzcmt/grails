package com.webcommander.plugin.live_chat

import com.webcommander.admin.ConfigService
import com.webcommander.admin.MessageSource
import com.webcommander.common.FileService
import com.webcommander.common.LargeData
import com.webcommander.config.EmailTemplate
import com.webcommander.config.SiteConfig
import com.webcommander.config.StoreDetail
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.design.WidgetService
import com.webcommander.manager.HookManager
import com.webcommander.manager.LicenseManager
import com.webcommander.plugin.live_chat.mixin_service.WidgetService as WS
import com.webcommander.plugin.live_chat.socket.endpoints.ChatroomEndpoint
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil
import grails.util.Holders

import javax.websocket.server.ServerContainer

class BootStrap {
    FileService fileService

    static final LIVE_CHAT_IAPP_IDENTIFIRE = "live_chat_i_app_cer"
    static final IAPP_CERTIFICATE_PATH = "certificate/iAppCertificate.p12"
    private final String LIVE_CHAT = "liveChat"
    private final String config_type = "live_chat"

    List domain_constants = [
            [constant:"SITE_CONFIG_TYPES", key: "LIVE_CHAT", value: LIVE_CHAT],
            [constant:"WIDGET_TYPE", key: "LIVE_CHAT", value: LIVE_CHAT]
    ]

    List named_constants = [
            [constant:"WIDGET_MESSAGE_KEYS", key: LIVE_CHAT + ".title", value:"live.chat.widget"],
            [constant:"WIDGET_MESSAGE_KEYS", key: LIVE_CHAT + ".label", value:"live.chat"],
            [constant:"WIDGET_LICENSE", key: LIVE_CHAT, value:"allow_live_chat_feature"]
    ]

    List templates = [
            [
                    label: "live.chat.offline.email",
                    identifier: "live-chat-offline-email",
                    subject: "Live chat offline message",
                    isActiveReadonly: true,
                    type: DomainConstants.EMAIL_TYPE.ADMIN
            ],
            [
                    label: "send.chat.to.mail",
                    identifier: "send-chat-to-mail",
                    subject: "Chat history",
                    isActiveReadonly: true,
                    type: DomainConstants.EMAIL_TYPE.ADMIN
            ]
    ]



    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        NamedConstants.addConstant(named_constants)
        ConfigService.addTab(config_type, [
                url: "liveChatAdmin/config",
                message_key: "live.chat",
                license: "allow_live_chat_feature"
        ])
        templates.each { tpt ->
            if (!EmailTemplate.findAllByIdentifier(tpt.identifier)) {
                new EmailTemplate(tpt).save()
            }
        }
        Map initialData = [
                ask_for_info: "true",
                offline_message_recipient: StoreDetail.first()?.address?.email ?: null,
                welcome_message: ""
        ]
        if (SiteConfig.countByType(config_type) == 0) {
            initialData.each { entry ->
                new SiteConfig(type: config_type, configKey: entry.key, value: entry.value).save()
            }
        }
        if(!MessageSource.countByMessageKey("chat.with.us")) {
            new MessageSource(messageKey: "chat.with.us", locale: "all", message: "Chat With Us").save()
            new MessageSource(messageKey: "need.help", locale: "all", message: "Need Help?").save()
            new MessageSource(messageKey: "send.us.message", locale: "all", message: "Send Us Message").save()
        }
        if(LargeData.countByIdentifire(LIVE_CHAT_IAPP_IDENTIFIRE) == 0) {
//            Byte[] data = fileService.getRestrictedResourceStream(IAPP_CERTIFICATE_PATH).getBytes()
//            new LargeData(identifire: LIVE_CHAT_IAPP_IDENTIFIRE, name: "iAppResource", content: data).save()
        }
    }

    def tenantDestroy = { tenant ->
        ConfigService.removeTab(config_type)
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeSiteMessage("chat.with.us", "need.help", "send.us.message")
            util.removeSiteConfig(config_type)
            util.removeEmailTemplates(*templates.identifier)
            util.removeWidget(LIVE_CHAT)
            LargeData.findByIdentifire(LIVE_CHAT_IAPP_IDENTIFIRE)?.delete()
            NamedConstants.removeConstant(named_constants)
            DomainConstants.removeConstant(domain_constants)
        } catch(Exception e) {
            log.error "Could Not Deactivate Plugin blog From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        ServerContainer serverContainer = servletContext.getAttribute("javax.websocket.server.ServerContainer")
        serverContainer.addEndpoint(ChatroomEndpoint)
        Holders.grailsApplication.mainContext.getBean(WidgetService).metaClass.mixin WS

        TenantContext.eachParallelWithWait(tenantInit)

        HookManager.register("available-widgets") { widgets ->
            if(LicenseManager.isProvisionActive() && !LicenseManager.license("allow_live_chat_feature")) {
                def modified = new ArrayList<String>(widgets)
                modified.removeAll {it == "liveChat"}
                modified
            }
        }
    }
}
