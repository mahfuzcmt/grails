package com.webcommander.util

import com.webcommander.AutoGeneratedPage
import com.webcommander.AutoPageContent
import com.webcommander.acl.*
import com.webcommander.admin.MessageSource
import com.webcommander.beans.SiteMessageSource
import com.webcommander.common.FileService
import com.webcommander.config.EmailTemplate
import com.webcommander.config.SiteConfig
import com.webcommander.constants.NamedConstants
import com.webcommander.design.DockSection
import com.webcommander.design.Layout
import com.webcommander.manager.CacheManager
import com.webcommander.manager.PathManager
import com.webcommander.webcommerce.PaymentGateway
import com.webcommander.webcommerce.PaymentGatewayMeta
import com.webcommander.webcommerce.SurchargeRange
import com.webcommander.widget.Widget
import groovy.sql.Sql
import org.hibernate.SessionFactory

import java.sql.Connection

class PluginDestroyUtil {
    private Sql sql

    PluginDestroyUtil() {
        sql = new Sql(AppUtil.getBean("dataSource"))
    }


    def removeAutoGeneratePageWidget(String widgetName, String type) {
        String regex = "<wi:" + widgetName + "\\s+[\\w-]+=\"\\b" + type + "\\b\"(/>|></wi:" + widgetName + ">)"
        AutoPageContent.list().each {
            it.body = it.body ? (it.body.replaceAll(regex, "")) : it.body
            it.save(flush: true)
        }
    }

    List executeQuery(String query) {
        return sql.rows(query)
    }

    PluginDestroyUtil executeStatement(String query) {
        sql.execute(query)
        return this
    }

    PluginDestroyUtil dropTable(String... name) {
        name.each {
            executeStatement("DROP TABLE IF EXISTS $it")
        }
        return this
    }

    PluginDestroyUtil removeSiteConfig(String type, String... key = null) {
        List keys = []
        if (key) {
            keys.addAll(key)
        }
        removeSiteConfig(type, keys)
        return this
    }

    PluginDestroyUtil removeSiteConfig(String type, Collection keys) {
        SiteConfig.createCriteria().list {
            eq("type", type)
            if (keys) {
                inList("configKey", keys)
            }
        }*.delete(flush: true)
        AppUtil.clearConfig(type)
        return this
    }

    PluginDestroyUtil deleteWebInfFolder(String... paths) {
        paths.each {
            File file = new File(PathManager.getCustomRestrictedResourceRoot(it))
            if (file.exists()) {
                if (file.isDirectory()) {
                    file.deleteDir()
                } else {
                    file.delete()
                }
            }
        }
        return this
    }

    PluginDestroyUtil deleteResourceFolders(String... paths) {
        File file
        paths.each {
            file = new File(PathManager.getResourceRoot(it))
            if (file.exists()) {
                if (file.isDirectory()) {
                    file.deleteDir()
                } else {
                    file.delete()
                }
            }
        }
        return this
    }

    PluginDestroyUtil removeFoldersFromSysResource(String... folders) {
        File folderToRemove
        for (String folder : folders) {
            if (folder) {
                folderToRemove = new File(PathManager.getRestrictedResourceRoot(folder))
                if (folderToRemove.exists()) {
                    folderToRemove.deleteDir()
                }
            }
        }
        return this
    }

    PluginDestroyUtil removeFoldersFromModifiableResource(String... folders) {
        FileService fileService = AppUtil.getBean(FileService)
        for (String folder : folders) {
            if (folder) {
                fileService.removeBulkModifiableResource(folder)
            }
        }
        return this
    }

    PluginDestroyUtil removeSiteMessage(String... keys) {
        MessageSource.createCriteria().list {
            inList "messageKey", keys
        }*.delete()
        AppUtil.getBean(SiteMessageSource).clearCache()
        return this
    }

    PluginDestroyUtil removeDefaultImages(String... names) {
        File fileToRemove
        for (String name : names) {
            fileToRemove = new File(PathManager.getRestrictedResourceRoot("default-images/$name"))
            if (fileToRemove.exists()) fileToRemove.deleteDir()
        }
        return this
    }

    PluginDestroyUtil removePaymentGateway(String code) {
        SurchargeRange.where {
            paymentGateway {
                code == code
            }
        }.list()*.delete()
        PaymentGateway.where {
            code == code
        }.list()*.delete()
        return this
    }

    PluginDestroyUtil removeCreditCardProcessor(String processor, String optionLabel, String optionValue = null) {
        if (!optionValue) {
            optionValue = processor
        }
        removePaymentMeta(processor)
        PaymentGatewayMeta.createCriteria().list {
            eq "fieldFor", 'CRD'
            eq "name", 'creditCardProcessor'
        }.each { PaymentGatewayMeta meta ->
            meta.optionLabel.remove(optionLabel)
            meta.optionValue.remove(optionValue)
            meta.save()
        }
        return this
    }

    PluginDestroyUtil removePaymentMeta(String fieldFor) {
        PaymentGatewayMeta.createCriteria().list {
            eq "fieldFor", fieldFor
        }*.delete()
        return this
    }

    PluginDestroyUtil removeAutoPage(String... names) {
        for (String name : names) {
            AutoGeneratedPage.createCriteria().list {
                eq "name", name
            }*.delete()
        }
        return this
    }

    PluginDestroyUtil removeEmailTemplates(String... identifiers) {
        for (String identifier : identifiers) {
            removeFoldersFromModifiableResource("email-templates/${identifier}/")
            EmailTemplate template = EmailTemplate.findByIdentifier(identifier)
            template.delete()
            AppUtil.getBean(SessionFactory).currentSession.flush()
        }
        return this
    }

    PluginDestroyUtil removePermission(String type) {
        def permissions = Permission.findAllByType(type)
        UserPermission.createCriteria().list {
            inList "permission", permissions
        }*.delete()
        RolePermission.createCriteria().list {
            inList "permission", permissions
        }*.delete()
        OwnerPermission.createCriteria().list {
            inList "permission", permissions
        }*.delete()
        EntityPermission.createCriteria().list {
            inList "permission", permissions
        }*.delete()
        permissions*.delete()
        CacheManager.removeCache(NamedConstants.CACHE.SCOPE_APP, NamedConstants.CACHE.ACCESS_CONTROL)
        return this
    }

    PluginDestroyUtil removeWidget(String type, String paramsLike = null) {

        def widgetList = Widget.createCriteria().list {
            eq("widgetType", type)
        }
        widgetList?.each { widget ->
            def layoutObj = Layout.get(widget.containerId)
            if (widget && layoutObj) {
                layoutObj.removeFromHeaderWidgets(widget)
                layoutObj.removeFromFooterWidgets(widget)
            } else if (widget) {
                DockSection dockSection = DockSection.createCriteria().get {
                    widgets {
                        eq("uuid", widget.uuid)
                    }
                }
                if (dockSection) {
                    dockSection.removeFromWidgets(widget)
                    dockSection.save()
                }
            }
        }

        Widget.createCriteria().list {
            eq "widgetType", type
            if (paramsLike) {
                like paramsLike.encodeAsLikeText()
            }
        }*.delete()
        return this
    }

    PluginDestroyUtil removeWidgetContent(String type) {
        Widget.createCriteria().list {
            eq "type", type
        }*.delete()
        return this
    }

    PluginDestroyUtil removeProductWidget(String type) {
        return this
    }

    Connection getConnection() {
    }

    void closeConnection() {
    }
}
