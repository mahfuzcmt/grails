package com.webcommander.plugin.blog.mixin_service

import com.webcommander.models.TemplateData
import com.webcommander.plugin.blog.content.BlogCategory
import com.webcommander.plugin.blog.content.BlogPost
import com.webcommander.plugin.blog.content.BlogService
import com.webcommander.util.DomainUtil
import grails.util.Holders

class TemplateDataProviderService {
    private static BlogService _blogService

    private static BlogService getBlogService() {
        return _blogService ?: (_blogService = Holders.grailsApplication.mainContext.getBean(BlogService))
    }

    Map collectBlogCategoryTypeContent(TemplateData templateData,  BlogCategory blogCategory) {
        Map data = DomainUtil.toMap(blogCategory, [exclude: ["posts"]])
        templateData.resources.add "blog-category/category-${blogCategory.id}/"
        return data
    }

    List<Map> collectBlogCategoryTypeContents(TemplateData templateData) {
        List<BlogCategory> categories = blogService.getBlogCategories([:])
        return categories.collect {
            return collectBlogCategoryTypeContent(templateData, it)
        }
    }

    Map collectBlogPostTypeContent(TemplateData templateData,  BlogPost blogPost) {
        Map data = DomainUtil.toMap(blogPost, [exclude: ["metaTags", "comments", "author", "customers"]])
        data.comments = []
        blogPost.comments.each {
            data.comments.add DomainUtil.toMap(it, [exclude: ["blogPost"]])
        }
        templateData.resources.add "blog-post/post-${blogPost.id}/"
        return data
    }

    List<Map> collectBlogPostTypeContents(TemplateData templateData) {
        List<BlogPost> blogPosts = blogService.getPosts([:])
        return blogPosts.collect {
            return collectBlogPostTypeContent(templateData, it)
        }
    }
}
