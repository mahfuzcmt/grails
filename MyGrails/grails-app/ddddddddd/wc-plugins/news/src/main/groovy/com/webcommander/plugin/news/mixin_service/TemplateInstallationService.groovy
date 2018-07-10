package com.webcommander.plugin.news.mixin_service

import com.webcommander.constants.DomainConstants
import com.webcommander.installation.template.InstallationDataHolder
import com.webcommander.models.TemplateData
import com.webcommander.plugin.news.News
import com.webcommander.util.DomainUtil

class TemplateInstallationService {

    Long saveNewsTypeWidgetContent(TemplateData templateData, InstallationDataHolder installationDataHolder, Map data) {
        News news = new News()
        data.article = installationDataHolder.getContentMapping(DomainConstants.WIDGET_CONTENT_TYPE.ARTICLE, data.article, "id")
        data.newsDate = data.newsDate.toDate("yyyy-MM-dd'T'HH:mm:ss")
        DomainUtil.populateDomainInst(news, data)
        news.save()
        if(news.hasErrors()) {
            return null
        }
        return news.id
    }
}
