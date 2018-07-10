package com.webcommander.plugin.blog.mixin_service

import com.webcommander.plugin.blog.content.BlogService
import com.webcommander.util.AppUtil
import com.webcommander.util.SortAndSearchUtil
import grails.util.Holders

class GalleryWidgetService {
    BlogService _blogService
    BlogService getBlogService() {
        return _blogService ?: (_blogService = Holders.grailsApplication.mainContext.getBean(BlogService))
    }
    Map getGalleryModelForBlogPost(Map model, Map params) {
        Map config = model.config
        List postIds = model.widget.widgetContent.contentId.collect { it.longValue() };
        List posts = blogService.filterOutAvailableBlogPost(postIds, AppUtil.session.customer, [:])
        model.items = SortAndSearchUtil.sortInCustomOrder(posts, "id", postIds);
        return model
    }
}
