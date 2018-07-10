package com.webcommander.plugin.blog.controllers.rest.elastic

import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.blog.content.BlogPost
import com.webcommander.plugin.blog.content.BlogService
import com.webcommander.util.AppUtil
import com.webcommander.util.RestProcessor


class ElasticCrawlerHelperController extends RestProcessor {
    BlogService blogService

    def blogList() {
        String submitRestricted = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SEARCH_PAGE, "submit_restricted_item")
        List<BlogPost> blogPosts = blogService.getPosts([
                max: -1,
                offset: 0,
                visibility: submitRestricted == "true" ? null : "open",
                isPublished: "true"
        ])
        List results = blogPosts.collect {
            [id: it.id, name: it.name, content: it.content, url: it.url, updated: it.updated]
        }
        rest(blogPosts: results)
    }
}
