package com.webcommander.plugin.blog.controllers.admin.design

import com.webcommander.authentication.annotations.License
import com.webcommander.plugin.blog.content.BlogService


class WidgetController {
    static BlogService blogService

    @License(required = "allow_blog_feature")
    def blogPostShortConfig() {
        render view: "/plugins/blog/admin/widget/loadBlogPostShort", model: [noAdvance: true, posts: blogService.getPostsInOrder(params.widget.widgetContent.contentId)];
    }
}