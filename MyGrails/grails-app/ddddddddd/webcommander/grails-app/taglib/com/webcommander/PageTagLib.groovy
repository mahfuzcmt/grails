package com.webcommander

import com.webcommander.common.MetaTag
import com.webcommander.content.AutoPageService
import com.webcommander.util.TemplateMatcher

class PageTagLib {
    static namespace = "page";

    AutoPageService autoPageService;

    def convertAutoPage = { attrs, body ->
        String fixedPageName = attrs.name;
        AutoGeneratedPage fixedPage = autoPageService.getAutoGeneratedPage(fixedPageName);
        String title = fixedPage.title
        if(attrs.macros) {
            TemplateMatcher engine = new TemplateMatcher("%", "%")
            title = engine.replace(title, attrs.macros)
        }
        Map properties = [
            name : fixedPage.name,
            title : title,
            layout : fixedPage.layout,
            body : "",
            metaTags : [],
            disableGooglePageTracking: fixedPage.disableGooglePageTracking
        ]
        if(fixedPage.editorEnable) {
            AutoPageContent pageContent = AutoPageContent.createCriteria().get {
                eq("belong.id", fixedPage.id)
            };
            if(pageContent.js) {
                request.jsFromContent = pageContent.id
            }
            if(pageContent.css) {
                request.cssFromContent = pageContent.id
            }
            properties.body = pageContent.body
            request.hasContent = true
        }
        Page page = new Page(properties);
        fixedPage.metaTags.each {
            page.metaTags.add(new MetaTag(name: it.name, value: it.value))
        }
        if(request.metaTags) {
            request.metaTags.each {
                page.metaTags.add(new MetaTag(name: it.name, value: it.value))
            }
        }
        if (request.title) {
            page.title = request.title;
        }
        request.page = page;
        request.isAutoPage = true;
    }
}