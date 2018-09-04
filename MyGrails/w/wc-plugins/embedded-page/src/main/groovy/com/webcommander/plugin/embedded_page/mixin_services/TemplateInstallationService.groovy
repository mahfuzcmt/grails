package com.webcommander.plugin.embedded_page.mixin_services

import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.installation.template.InstallationDataHolder
import com.webcommander.models.TemplateData
import com.webcommander.plugin.embedded_page.EmbeddedPage
import com.webcommander.util.DomainUtil

class TemplateInstallationService {

    private static CommonService _commonService
    private static CommonService getCommonService() {
        return _commonService ?: (_commonService = CommonService.getInstance())
    }

    Long saveEmbeddedPageTypeWidgetContent(TemplateData templateData, InstallationDataHolder installationDataHolder, Map data) {
        EmbeddedPage embeddedPage = new EmbeddedPage()
        DomainUtil.populateDomainInst(embeddedPage, data, [exclude: ["widget"]])
        if(!commonService.isUnique(embeddedPage, "name")) {
            embeddedPage.name = commonService.getCopyNameForDomain(embeddedPage)
        }
        if(embeddedPage.hasErrors()) {
            return null
        }
        embeddedPage.save()
        embeddedPage.id
    }

    void saveEmbeddedPageWidgets(TemplateData templateData, InstallationDataHolder installationDataHolder) {
        List<Map> pages = templateData.getWidgetContents(DomainConstants.WIDGET_CONTENT_TYPE.EMBEDDED_PAGE)
        pages.each { Map data ->
            EmbeddedPage embeddedPage = EmbeddedPage.get(installationDataHolder.getContentMapping(DomainConstants.WIDGET_CONTENT_TYPE.EMBEDDED_PAGE, data.id, "id"))
            if(embeddedPage) {
                this.addWidgets(data.widgets, embeddedPage.id, embeddedPage, "body", installationDataHolder)
            }
        }
    }
}
