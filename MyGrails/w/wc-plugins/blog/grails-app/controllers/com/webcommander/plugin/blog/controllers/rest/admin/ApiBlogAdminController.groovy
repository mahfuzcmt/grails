package com.webcommander.plugin.blog.controllers.rest.admin

import com.webcommander.authentication.annotations.Restriction
import com.webcommander.authentication.annotations.Restrictions
import com.webcommander.common.FileService
import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.blog.content.BlogComment
import com.webcommander.plugin.blog.content.BlogPost
import com.webcommander.plugin.blog.content.BlogService
import com.webcommander.rest.throwable.ApiException
import com.webcommander.util.RestProcessor
import com.webcommander.web.multipart.WebCommanderMultipartFile
import org.apache.commons.httpclient.HttpStatus
import grails.util.TypeConvertingMap
import org.springframework.web.multipart.MultipartFile

class ApiBlogAdminController extends RestProcessor {
    BlogService blogService
    FileService fileService

    @Restriction(permission = "blog.view.list")
    def list() {
        params.max = params.max ?: "10";
        params.offset = params.offset ?: "-1"
        List<BlogPost> posts = blogService.getPosts(params)
        Map config = [
                author: [default: ["id", "fullName"]]
        ]
        rest(posts: posts, config)
    }

    def count() {
        Integer count = blogService.getPostsCount(params)
        rest count: count
    }

    def info() {
        BlogPost blogPost = BlogPost.findByIdAndIsInTrash(params.long("id"), false)
        if(!blogPost) {
            throw ApiException("post.not.found", HttpStatus.SC_NOT_FOUND)
        }
        Map config = [
                author: [default: ["id", "fullName"]]
        ]
        rest posts: blogPost, config
    }

    @Restrictions([
            @Restriction(permission = "blog.create", params_not_exist = "id"),
            @Restriction(permission = "blog.edit", params_exist = "id", entity_param = "id", domain = BlogPost)
    ])
    def create() {
        Map blogData = request.getAttribute(DomainConstants.REQUEST_ATTR_KEYS.REQUEST_BODY);
        blogData = new TypeConvertingMap(blogData)
        MultipartFile uploadedFile
        if(blogData.image_url) {
            File file = fileService.downloadFile(blogData.image_url)
            uploadedFile = new WebCommanderMultipartFile(file.name, new FileInputStream(file))
        }
        BlogPost post = blogService.savePost(params, uploadedFile)
        if(post) {
            rest([status: "success", id: post.id])
        } else {
            throw new ApiException("blog.post.save.failed", HttpStatus.SC_BAD_REQUEST)
        }
    }

    @Restriction(permission = "blog.remove", entity_param = "id", domain = BlogPost)
    def delete() {
        Boolean result = blogService.putBlogPostInTrash(params.long("id"), "yes", "include")
        if(result) {
            rest(status: "success")
        } else {
            throw new ApiException("blog.post.save.failed", HttpStatus.SC_BAD_REQUEST)
        }
    }

    def commentList() {
        params.max = params.max ?: "-1"
        params.offset = params.offset ?: "0"
        List<BlogComment> comments = blogService.getComments(params)
        rest comments: comments
    }
}
