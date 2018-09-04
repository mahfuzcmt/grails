package com.webcommander.plugin.blog.mixin_service

import com.webcommander.plugin.blog.content.BlogService
import com.webcommander.util.AppUtil
import com.webcommander.widget.Widget
import grails.converters.JSON
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap

/**
 * Created by pritom on 21/06/2017.
 */
class FrontEndEditorService {
    static BlogService _blogService
    static WidgetService _widgetService

    def beforeSaveBlogPostWidget(Widget widget, GrailsParameterMap params) {
        if (!widget.id) {
            widgetService.populateBlogPostInitialContentNConfig(widget)
            if (widget.params) {
                JSON.parse(widget.params).each {
                    params[it.key] = it.value
                }
            }

            params.selection = "custom"
            if (params.name) {
                Map blogParams = [
                        name: params.name,
                        visibility: "open",
                        content: params.content,
                        date: new Date().gmt(),
                        isPublished: "true",
                        categories: params.categories
                ]
                def blogPost = blogService.savePost(blogParams, params.postImage ? AppUtil.request.getFile("postImage") : null)
                if (blogPost) {
                    params.post = [blogPost.id]
                }
            }
        }
    }

    static BlogService getBlogService() {
        if(_blogService) {
            return _blogService
        }
        return _blogService = Holders.grailsApplication.mainContext.getBean(BlogService)
    }

    static WidgetService getWidgetService() {
        return _widgetService ?: (_widgetService = Holders.grailsApplication.mainContext.getBean(WidgetService))
    }
}