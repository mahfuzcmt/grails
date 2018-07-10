package com.webcommander.controllers.admin

import com.webcommander.common.EcommerceService
import com.webcommander.plugin.PluginManager

class EcommerceController {
    EcommerceService ecommerceService

    def getUsedWidgetAndPlugins() {
        def widgets = ecommerceService.getAllEcommerceWidgets()
        def plugins = PluginManager.getActivePlugins()
        render(view: "/admin/setting/ecommercePopup", model:[widgets: widgets, plugins: plugins]);
    }
}