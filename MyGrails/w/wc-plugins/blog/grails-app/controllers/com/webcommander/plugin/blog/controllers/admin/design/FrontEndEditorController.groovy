package com.webcommander.plugin.blog.controllers.admin.design

import com.webcommander.plugin.blog.content.BlogPost
import com.webcommander.common.CommonService
import com.webcommander.plugin.blog.content.BlogService
import com.webcommander.widget.Widget
import grails.converters.JSON

class FrontEndEditorController {
    BlogService blogService
    CommonService commonService


    def isPostUrlUnique() {
        render(commonService.responseForUniqueField(BlogPost, params.long("id"), params.field, params.value) as JSON)
    }


    def isPostUnique() {
        render(commonService.responseForUniqueField(BlogPost, params.long("id"), params.field, params.value) as JSON)
    }


    def saveAsBlog() {
        render(view: "/plugins/blog/frontEndEditor/saveAsBlog", model: [

        ])
    }


    def blogPostConfig() {
        Widget widget = Widget.get(params.widgetId.toLong())
        Map config = widget.params ? JSON.parse(widget.params) : [:]

        render(view: "/plugins/blog/frontEndEditor/blogConfig", model: [
                config: config
        ])
    }


    def blogSelection() {
        params.widgetContent = ""
        if (!params.tableOnly && params.widgetId) {
            Widget widget = Widget.get(params.widgetId.toLong())
            params.widgetContent = widget.widgetContent.collect { it.contentId }.join(",")
        }
        int count = blogService.getPostsCount(params)
        List blogs = blogService.getPosts(params)
        render(view: "/plugins/blog/frontEndEditor/_blogSelection", model: [
                count: count, blogs: blogs
        ])
    }

}