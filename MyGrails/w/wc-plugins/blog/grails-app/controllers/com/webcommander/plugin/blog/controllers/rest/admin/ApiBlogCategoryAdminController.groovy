package com.webcommander.plugin.blog.controllers.rest.admin

import com.webcommander.authentication.annotations.Restriction
import com.webcommander.plugin.blog.content.BlogCategory
import com.webcommander.plugin.blog.content.BlogService
import com.webcommander.util.RestProcessor

class ApiBlogCategoryAdminController extends RestProcessor {
    BlogService blogService

    @Restriction(permission = "blog.view.list")
    def list() {
        params.max = params.max ?: "-1"
        params.offset = params.offset ?: "0"
        List<BlogCategory> categories = blogService.getBlogCategories(params)
        rest categories: categories
    }
}
