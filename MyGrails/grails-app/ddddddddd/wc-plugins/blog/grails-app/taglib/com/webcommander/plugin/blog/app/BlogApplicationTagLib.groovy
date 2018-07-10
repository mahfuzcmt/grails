package com.webcommander.plugin.blog.app

import com.webcommander.plugin.blog.content.BlogService
import com.webcommander.util.AppUtil
import com.webcommander.constants.DomainConstants

class BlogApplicationTagLib {
    static namespace = "blogApp"
    BlogService blogService

    def embeddableCss = { attrs, body ->
        def postImageConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.BLOG);
        out << body()
        out << """<style>
            .post.post-list-view-height {
                height: ${postImageConfig.listview_height}px;
            }
            .post.post-list-view-width {
                width: ${postImageConfig.listview_width}px;
            }
            .post.post-list-view img {
                max-width: ${postImageConfig.listview_width}px;
                max-height: ${postImageConfig.listview_height}px;
            }
            .category.post-list-view-height {
                height: ${postImageConfig.cat_details_height}px;
            }
            .category.post-list-view-width {
                width: ${postImageConfig.cat_details_width}px;
            }
            .category.post-list-view img {
                max-width: ${postImageConfig.cat_details_width}px;
                max-height: ${postImageConfig.cat_details_height}px;
            }
         </style>"""
    }

    def siteSearchResult = { attrs, body ->
        out << body()
        List posts =  blogService.filterOutAvailableBlogPost(pageScope.results.blogPosts.id.collect { it.toLong() }, AppUtil.loggedCustomer, [:])
        if(posts) {
            String imageSize = AppUtil.getConfig("blog-post-size", "cat_details")
            out << '<div class="search-result blogs">'
            out << '<div class="title">'
            out << g.message(code: "blog")
            out <<  '</div>'
            out << g.include( view :"/plugins/blog/widget/blogPostWidgetView.gsp", model: [config: [
                    image: 'true',
                    content_length : 160,
                    date: 'true',
                    read_more: 's:read.more',
                    author: 'true',
                    categories: 'true',
                    comment_count: 'true'
            ], posts: posts, imageSize: imageSize, clazz: 'search'])
            out << "<paginator data-urlprefix='blog-search' total='${pageScope.results.totalBlogPost}' offset='${pageScope.siteSearchConfig.blog.offset}' max='${pageScope.siteSearchConfig.blog.max}'></paginator>"
            out << '</div>'
        }
    }
}
