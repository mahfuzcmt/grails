package com.webcommander.plugin.flash_widget.mixin_service

import com.webcommander.installation.template.InstallationDataHolder
import com.webcommander.widget.Widget

class TemplateInstallationService {

    void afterFlashWidgetSave(Map widgetData, Widget widget, InstallationDataHolder installationDataHolder) {
        this.moveTemplateData(installationDataHolder, "resources/flash-widget/${widgetData.uuid}", "flash-widget/${widget.uuid}")
        widget.content = widget.content.replace(widgetData.uuid, widget.uuid)
        widget.save()
    }
}
