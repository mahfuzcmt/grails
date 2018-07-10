package com.webcommander.plugin.blog.app

import com.webcommander.AppResourceTagLib
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

class BlogResourceTagLib {

    static namespace = "appResource"

    static final String BLOG_CATEGORY = "blog-category"
    static final String BLOG_POST = "blog-post"
    static final String BLOG = "blog"
    static final String POST = "post"
    static final String CATEGORY = "category"

    public static final RESOURCES_PATH = [
         "BLOG_CATEGORY" : "blog-category"   ,
         "BLOG_POST" : "blog-post"   ,
    ]

    @Autowired
    @Qualifier("com.webcommander.AppResourceTagLib")
    AppResourceTagLib parent

    def getBlogImageRelativeUrl(def id) {
        return "${RESOURCES_PATH.BLOG_POST}/post-$id/"
    }

    def getBlogPostImageUrl = { attrs, body->
        def blog = attrs.image
        String sizeOrPrefix = attrs.sizeOrPrefix ?: ""
        String url = blog.image ? parent.getAbstractStaticResourceImageURL(blog, sizeOrPrefix) : parent.getDefaultImageWithPrefix(sizeOrPrefix, parent.PRODUCT)
        out << url
    }


    def getBlogCategoryImageUrl = { attrs, body ->
        def blogCategory = attrs.image
        String sizeOrPrefix = attrs.sizeOrPrefix ?: ""
        String url = blogCategory.image ? parent.getAbstractStaticResourceImageURL(blogCategory, sizeOrPrefix) : parent.getDefaultImageWithPrefix(sizeOrPrefix, parent.PRODUCT)
        out << url
    }

    static String  getBlogCategoryRelativeUrl(def blogCategoryId) {
        return "${BLOG_CATEGORY}/${CATEGORY}-${blogCategoryId}/"
    }

    static  String getBlogPostRelativeUrl(def blogPostId) {
        return "${BLOG_POST}/${POST}-${blogPostId}/"
    }

}