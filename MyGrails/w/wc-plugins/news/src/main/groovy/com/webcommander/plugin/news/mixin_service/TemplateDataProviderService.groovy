package com.webcommander.plugin.news.mixin_service

import com.webcommander.models.TemplateData
import com.webcommander.plugin.news.News
import com.webcommander.plugin.news.NewsService
import com.webcommander.util.DomainUtil
import grails.util.Holders


class TemplateDataProviderService {
    private static NewsService _newsService
    private static NewsService getNewsService(){
        return _newsService ?: (Holders.grailsApplication.mainContext.getBean(NewsService))
    }

    Map collectNewsTypeContent(TemplateData templateData,  News news) {
        Map data = DomainUtil.toMap(news)
        return data
    }

    List<Map> collectNewsTypeContents(TemplateData templateData) {
        List<News> newses = newsService.getNewses([:])
        return newses.collect {
            return collectNewsTypeContent(templateData, it)
        }
    }
}
