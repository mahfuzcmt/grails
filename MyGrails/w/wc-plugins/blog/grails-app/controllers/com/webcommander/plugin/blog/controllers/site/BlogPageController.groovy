package com.webcommander.plugin.blog.controllers.site

import com.webcommander.authentication.annotations.AutoGeneratedPage
import com.webcommander.authentication.annotations.License
import com.webcommander.captcha.CaptchaService
import com.webcommander.constants.DomainConstants
import com.webcommander.manager.HookManager
import com.webcommander.plugin.blog.content.BlogCategory
import com.webcommander.plugin.blog.content.BlogComment
import com.webcommander.plugin.blog.content.BlogPost
import com.webcommander.plugin.blog.content.BlogService
import com.webcommander.util.AppUtil
import grails.converters.JSON


class BlogPageController {
    BlogService blogService
    CaptchaService captchaService

    @AutoGeneratedPage("blog.post")
    def post() {
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.BLOG)
        BlogPost blogPost = BlogPost.findByUrl(params.url)
        if (blogPost == null) {
            response.setStatus(404)
            forward(controller: "exception", action: "handle404")
            return;
        }
        if(!(session.admin && params.adminView)) {
            if(blogPost.visibility == "hidden" || !blogPost.isPublished || blogPost.isInTrash) {
                response.setStatus(403)
                forward(controller: "exception", action: "handle403")
                return;
            }
            if(blogPost.visibility == "restricted") {
                if (!session.customer) {
                    flash.param = [referer: "/blog/" + blogPost.url];
                    redirect(controller: "customer", action: "login")
                    return;
                }
                if(blogPost.visibleTo == "selected") {
                    def found = blogPost.customers.id.contains(session.customer)
                    if (!found) {
                        found = blogPost.groups.find {
                            return it.customers.id.contains(session.customer)
                        }
                    }
                    if(!found) {
                        response.setStatus(403)
                        forward(controller: "exception", action: "handle403")
                        return;
                    }
                }
            }
        }
        Map filter = [
            post: blogPost.id,
            status: "approved",
            isSpam: "false",
            offset: params.offset ?: 0,
            max: config.comment_per_page.toLong(),
            sort: "created",
            dir: "desc"
        ]
        def comments = blogService.getComments(filter)
        def commentsCount = blogService.getCommentsCount(filter)
        request.blogPost = blogPost.id
        def macros = [ 'POST': blogPost.name]
        String view = "/site/siteAutoPage";
        Map model = [name : DomainConstants.AUTO_GENERATED_PAGES.BLOG_POST_DETAILS_PAGE, macros: macros, view: "/plugins/blog/site/blogPostDetails.gsp", post: blogPost, metaTags: blogPost.metaTags, config: config, comments: comments, count: commentsCount];
        view = HookManager.hook("auto-page-view-model", view, model);
        render (view: view, model: model)
    }

    @AutoGeneratedPage("blog.category")
    def blogCategory() {
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.BLOG)
        BlogCategory blogCategory = BlogCategory.findByUrl(params.url)
        if (!blogCategory) {
            response.setStatus(404)
            forward(controller: "exception", action: "handle404")
            return
        }
        def postIds =  blogCategory.posts.collect{ return it.id }
        List<BlogPost> posts = blogService.filterOutAvailableBlogPost(postIds, session.customer, [:])
        posts = posts.sort {a, b->
            b.date <=> a.date
        }
        request.blogCategory = blogCategory.id
        def macros = [ 'CATEGORY': blogCategory.name]
        String view = "/site/siteAutoPage";
        Map model = [name : DomainConstants.AUTO_GENERATED_PAGES.BLOG_CATEGORY_DETAILS_PAGE, macros: macros, view: "/plugins/blog/site/blogCategoryDetails.gsp",
                     category: blogCategory, posts: posts, config: config]
        view = HookManager.hook("auto-page-view-model", view, model);
        render (view: view, model: model)
    }

    def loadPostComments = {
        def max = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.BLOG, "comment_per_page")
        Map filter = [
            post: params.id,
            status: "approved",
            isSpam: "false",
            offset: params.offset ?: 0,
            max: params.max ?: max,
            sort: "created",
            dir: "desc"
        ]
        def comments = blogService.getComments(filter)
        def commentsCount = blogService.getCommentsCount(filter);
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.BLOG)
        def html = g.include(view: "/plugins/blog/site/commentList.gsp", model: [comments: comments, count: commentsCount, config : config ])
        render([status: "success", html: html] as JSON)
    }

    @License(required = "allow_blog_feature")
    def saveBlogComment() {
        params.remove("action")
        params.remove("controller")

        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.BLOG)
        def captchaSettings = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "captcha_setting");
        def captchaType = captchaSettings == "enable" ? AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "captcha_type") : "none";
        if(captchaSettings == "enable" && config.captcha == "true" && !captchaService.validateCaptcha(params, request)) {
            render([status: "error", captchaValidation: "failure", captchaType: captchaType, message: g.message(code: "invalid.captcha.entry")] as JSON)
        } else {
            if(blogService.saveComment(params, config)) {
                if(config.comment_moderator_approval == "true") {
                    render([status: "success", status: "pending", message: g.message(code: "thank.you.for.comment.comment.will.be.displayed.after.approval"), captchaType: captchaType] as JSON)
                } else {
                    render([status: "success", status: "approved", message: g.message(code: "thank.you.for.comment"), captchaType: captchaType] as JSON)
                }
            } else {
                render([status: "error", message: g.message(code: "comment.can.not.be.saved"), captchaType: captchaType] as JSON)
            }
        }
    }
    def getReplyPopup() {
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.BLOG)
        Map model = [:]
        render(view: "/plugins/blog/site/blogCommentReplyPopup.gsp", model: [popupTitle: "Reply", success: true, config : config, parent : BlogComment.get(params.id)])

    }

    def logingToReact() {
        render(view: "/plugins/blog/site/logingToReact.gsp", model: [message: params.purpose, url : params.url])

    }

    @License(required = "allow_blog_feature")
    def saveCommentReply() {
        params.remove("action")
        params.remove("controller")

        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.BLOG)
        def captchaSettings = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "captcha_setting");
        def captchaType = captchaSettings == "enable" ? AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "captcha_type") : "none";
        if(captchaSettings == "enable" && config.captcha == "true" && !captchaService.validateCaptcha(params, request)) {
            render([status: "error", captchaValidation: "failure", captchaType: captchaType, message: g.message(code: "invalid.captcha.entry")] as JSON)
        } else {
            if(blogService.saveReply(params, config)) {
                if(config.comment_moderator_approval == "true") {
                    render([status: "success", status: "pending", message: g.message(code: "thank.you.for.reply.reply.will.be.displayed.after.approval"), captchaType: captchaType] as JSON)
                } else {
                    render([status: "success", status: "approved", message: g.message(code: "thank.you.for.reply"), captchaType: captchaType] as JSON)
                }
            } else {
                render([status: "error", message: g.message(code: "reply.can.not.be.saved"), captchaType: captchaType] as JSON)
            }
        }
    }

    @License(required = "allow_blog_feature")
    def reactBlogComment() {
        params.remove("action")
        params.remove("controller")

        def captchaSettings = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "captcha_setting");
        def captchaType = captchaSettings == "enable" ? AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "captcha_type") : "none";
        if(blogService.reactComment(params)) {
            render([status: "success", status: "approved", message: g.message(code: "thank.you.for.reaction"), captchaType: captchaType] as JSON)
        } else {
            render([status: "error", message: g.message(code: "reaction.can.not.be.saved"), captchaType: captchaType] as JSON)
        }
    }
}
