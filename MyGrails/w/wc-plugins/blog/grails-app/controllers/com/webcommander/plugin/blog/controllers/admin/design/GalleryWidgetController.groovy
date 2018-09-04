package com.webcommander.plugin.blog.controllers.admin.design

import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.blog.content.BlogPost
import com.webcommander.plugin.blog.content.BlogService
import com.webcommander.widget.Widget
import grails.converters.JSON
import grails.util.Holders
import grails.web.Action

class GalleryWidgetController {
    BlogService blogService = Holders.grailsApplication.mainContext.getBean(BlogService)

    def loadBlogPostConfig(Widget widget) {
        Map config = JSON.parse(widget.params)
        List<BlogPost> posts =  config.galleryContentType == DomainConstants.GALLERY_CONTENT_TYPES.BLOG_POST ? blogService.getBlogPostsInOrder(widget.widgetContent.contentId) : []
        render(view: "/plugins/blog/admin/widget/galleryConfig/blog", model: [posts: posts, config: config])
    }
}
