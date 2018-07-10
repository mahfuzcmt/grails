package com.webcommander.common

import com.webcommander.Page
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.design.Layout
import com.webcommander.manager.HookManager
import com.webcommander.plugin.PluginManager
import com.webcommander.util.AppUtil
import com.webcommander.widget.Widget

class EcommerceService {

    def getAllEcommerceWidgets() {
        def ecommerceUsedWidgets = []
        List<Widget> widgets = Widget.findAll()
        widgets.each {
            if(DomainConstants.ECOMMERCE_WIDGET_TYPE_CHECKLIST[it.widgetType] && (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')) {
                String used = ""
                if(it.containerType == "layout") {
                    used = Layout.findById(it.containerId).name
                } else if(it.containerType == "page") {
                    used = Page.findById(it.containerId).name
                } else if(it.containerType == "embedded") {
                    used = HookManager.hook("ecommerce-embedded-page-name", it.containerId)
                }
                ecommerceUsedWidgets.addAll([name: NamedConstants.WIDGET_MESSAGE_KEYS[it.widgetType + '.label'], used: "${used} (${it.containerType})"] )
            }
        }
        return ecommerceUsedWidgets
    }
}
