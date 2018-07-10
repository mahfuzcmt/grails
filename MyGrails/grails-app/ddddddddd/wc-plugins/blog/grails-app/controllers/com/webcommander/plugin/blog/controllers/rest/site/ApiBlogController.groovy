package com.webcommander.plugin.blog.controllers.rest.site

import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.blog.content.BlogPost
import com.webcommander.plugin.blog.content.BlogService
import com.webcommander.rest.throwable.ApiException
import com.webcommander.util.AppUtil
import com.webcommander.util.RestProcessor
import org.apache.commons.httpclient.HttpStatus

class ApiBlogController extends RestProcessor {
    BlogService blogService

    def list() {
        List<BlogPost> posts = blogService.filterOutAvailableBlogPost(null, 0, params)
        Map config = [
                author: [default: ["id", "fullName"]]
        ]
        rest(posts: posts, config)
    }

    def info() {
        BlogPost blogPost = BlogPost.get(params.id)
        if (blogPost == null) {
            throw new ApiException("blog.post.not.found", HttpStatus.SC_NOT_FOUND)
        }
        if(blogPost.visibility == "hidden" || !blogPost.isPublished || blogPost.isInTrash) {
            throw new Exception("access.forbidden", HttpStatus.SC_FORBIDDEN)
        }
        if(blogPost.visibility == "restricted") {
            def found = blogPost.customers.id.contains(session.customer)
            if (!found) {
                found = blogPost.groups.find {
                    return it.customers.id.contains(session.customer)
                }
            }
            if(!found) {
                throw new ApiException("unauthorized", HttpStatus.SC_UNAUTHORIZED)
            }
        }
        rest(post: blogPost)
    }

    def commentList() {
        BlogPost blogPost = BlogPost.get(params.id)
        Map filter = [
                post: blogPost.id,
                status: "approved",
                isSpam: "false",
                offset: params.offset ?: 0,
                max: params.max ?: -1,
                sort: "created",
                dir: "desc"
        ]
        def comments = blogService.getComments(filter)
        rest(comments: comments)
    }

    def commentAdd() {
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.BLOG)
        Map commentData = request.getAttribute(DomainConstants.REQUEST_ATTR_KEYS.REQUEST_BODY);
        Boolean result = blogService.saveComment(commentData, config)
        if(result){
            if(config.comment_moderator_approval == "true") {
                rest([ status: "pending", message: "thank.you.for.comment.comment.will.be.displayed.after.approval"])
            } else {
                rest([status: "success", status: "approved", message: "thank.you.for.comment"])
            }
        } else {
            rest([status: "error", message: "comment.can.not.be.saved"])
        }
    }
}
