package com.webcommander.plugin.embedded_page.mixin_services

import com.webcommander.models.TemplateData
import com.webcommander.plugin.embedded_page.EmbeddedPage
import com.webcommander.util.DomainUtil
import com.webcommander.widget.Widget

class TemplateDataProviderService {

    Map collectEmbeddedPageTypeContent(TemplateData templateData,  EmbeddedPage page) {
        Map data = DomainUtil.toMap(page, [exclude: ["createdBy"]])
        List<Widget> widgets = Widget.createCriteria().list {
            eq "containerId", page.id
            eq "containerType", 'embedded'
        }
        data.widgets = []
        widgets.each {
            data.widgets.add this.collectWidgetData(templateData, it)
        }
        return data
    }

    List<Map> collectEmbeddedPageTypeContents(TemplateData templateData) {
        return EmbeddedPage.list().collect {
            return collectEmbeddedPageTypeContent(templateData, it)
        }
    }
}
