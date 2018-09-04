package com.webcommander.plugin.flash_widget.mixin_service

import com.webcommander.models.TemplateData
import com.webcommander.widget.Widget

class TemplateDataProviderService {
    void collectFlashWidgetData(TemplateData templateData, Widget widget) {
        templateData.resources.add "flash-widget/${widget.uuid}/"
    }
}
