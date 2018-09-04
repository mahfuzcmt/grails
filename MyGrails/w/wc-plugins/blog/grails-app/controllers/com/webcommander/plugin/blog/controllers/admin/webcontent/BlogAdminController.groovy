package com.webcommander.plugin.blog.controllers.admin.webcontent

import com.webcommander.authentication.annotations.License
import com.webcommander.authentication.annotations.Restriction
import com.webcommander.authentication.annotations.Restrictions
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.admin.Operator
import com.webcommander.plugin.blog.content.BlogCategory
import com.webcommander.plugin.blog.content.BlogComment
import com.webcommander.plugin.blog.content.BlogPost
import com.webcommander.plugin.blog.content.BlogService
import com.webcommander.throwables.AttachmentExistanceException
import com.webcommander.util.AppUtil
import grails.converters.JSON

class BlogAdminController {
    BlogService blogService
    CommonService commonService

    @License(required = "allow_blog_feature")
    @Restriction(permission = "blog.view.list")
    def loadPostAppView() {
        Integer count = blogService.getPostsCount(params)
        params.max = params.max ?: "10";
        List<BlogPost> posts = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            blogService.getPosts(params)
        }
        render(view: "/plugins/blog/admin/blogPost/appView", model: [posts : posts, count: count])
    }

    @License(required = "allow_blog_feature")
    @Restriction(permission = "blog.view.list")
    def loadCategoryAppView() {
        Integer count = blogService.getCategoriesCount(params)
        params.max = params.max ?: "10";
        List<BlogCategory> categories = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            blogService.getBlogCategories(params)
        }
        render(view: "/plugins/blog/admin/blogCategory/appView", model: [categories : categories, count: count])
    }

    @License(required = "allow_blog_feature")
    @Restriction(permission = "blog.view.list")
    def loadCommentAppView() {
        Integer count = blogService.getCommentsCount(params)
        params.max = params.max ?: "10";
        List<BlogComment> comments = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            blogService.getComments(params)
        }
        render(view: "/plugins/blog/admin/blogComment/appView", model: [comments : comments, count: count])
    }

    @License(required = "allow_blog_feature")
    @Restrictions([
            @Restriction(permission = "blog.create", params_not_exist = "id"),
            @Restriction(permission = "blog.edit", params_exist = "id", entity_param = "id", domain = BlogPost)
    ])
    def createPost() {
        Long id = params.id.toLong(0)
        BlogPost blogPost = id ? BlogPost.get(id) : new BlogPost();
        List<BlogCategory> selectedCategories = id ? blogPost.categories : []
        render view: "/plugins/blog/admin/blogPost/infoEdit", model: [post: blogPost, selectedCategories: selectedCategories]
    }

    @License(required = "allow_blog_feature")
    @Restrictions([
            @Restriction(permission = "blog.create", params_not_exist = "id"),
            @Restriction(permission = "blog.edit", params_exist = "id", entity_param = "id", domain = BlogCategory)
    ])
    def createCategory() {
        BlogCategory blogCategory = params.id ? BlogCategory.get(params.long("id")) : new BlogCategory();
        render view: "/plugins/blog/admin/blogCategory/infoEdit", model: [category: blogCategory]
    }

    @Restriction(permission = "blog.edit", params_exist = "id", entity_param = "id", domain = BlogComment)
    def editComment() {
        params.remove("controller")
        params.remove("action")
        BlogComment blogComment = BlogComment.get(params.long("id"))
        if(blogService.updateComment(params)) {
            render([status: "success", message: g.message(code: "blog.comment.update.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "blog.comment.update.failed")] as JSON)
        }
    }

    def saveCategory() {
        params.remove("action")
        params.remove("controller")
        def uploadedFile = request.getFile("image")
        if(blogService.saveCategory(params, uploadedFile)) {
            render([status: "success", message: g.message(code: "blog.category.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "blog.category.save.failed")] as JSON)
        }
    }

    def savePost() {
        params.remove("action")
        params.remove("controller")
        def uploadedFile = request.getFile("postImage")
        if(blogService.savePost(params, uploadedFile)) {
            render([status: "success", message: g.message(code: "blog.post.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "blog.post.save.failed")] as JSON)
        }
    }
    @Restriction(permission = "blog.view.list")
    def viewPost() {
        BlogPost blogPost = BlogPost.get(params.long("id"))
        render view: "/plugins/blog/admin/blogPost/view", model: [post: blogPost]
    }

    @Restriction(permission = "blog.view.list")
    def viewCategory() {
        BlogCategory blogCategory = BlogCategory.get(params.long("id"))
        render view: "/plugins/blog/admin/blogCategory/view", model: [category: blogCategory]
    }

    @Restriction(permission = "blog.view.list")
    def viewComment() {
        BlogComment blogComment = BlogComment.get(params.long("id"))
        render view: "/plugins/blog/admin/blogComment/view", model: [comment: blogComment]
    }

    def advanceFilterPost() {
        def categories = blogService.getBlogCategories([:])
        List<Operator> operatorList = Operator.all
        render(view: "/plugins/blog/admin/blogPost/filter", model: [categories: categories, operators: operatorList]);
    }

    def advanceFilterCategory() {
        render(view: "/plugins/blog/admin/blogCategory/filter", model: [get: 0]);
    }

    def advanceFilterComment() {
        def posts = blogService.getPosts([:])
        render(view: "/plugins/blog/admin/blogComment/filter", model: [posts: posts]);
    }

    @Restriction(permission = "blog.remove", entity_param = "id", domain = BlogPost)
    def deletePost () {
        try {
            if (blogService.putBlogPostInTrash(params.long("id"), params.at2_reply, params.at1_reply)) {
                render([status: "success", message: g.message(code: "blog.post.delete.success")] as JSON)
            } else {
                render([status: "error", message: g.message(code: "blog.post.delete.failure")] as JSON)
            }
        } catch (AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    @Restriction(permission = "blog.remove", entity_param = "ids", domain = BlogPost)
    def deleteSelectedPosts () {
        if (blogService.putSelectedBlogPostsInTrash(params.list("ids").collect{it.toLong()})) {
            render([status: "success", message: g.message(code: "selected.blog.posts.delete.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "selected.blog.posts.could.not.delete")] as JSON)
        }
    }

    @Restriction(permission = "blog.remove", entity_param = "id", domain = BlogCategory)
    def deleteCategory () {
        try {
            boolean deleted = blogService.deleteBlogCategory(params.long("id"), params.at2_reply, params.at1_reply)
            if (deleted) {
                render([status: "success", message: g.message(code: "blog.category.delete.success")] as JSON)
            } else {
                render([status: "error", message: g.message(code: "blog.category.delete.failure")] as JSON)
            }
        } catch (AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }

    }

    @Restriction(permission = "blog.remove", entity_param = "ids", domain = BlogCategory)
    def deleteSelectedCategories (){
        if (blogService.deleteSelectedCategories(params.list("ids").collect{it.toLong()})) {
            render([status: "success", message: g.message(code: "selected.blog.categories.delete.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "selected.blog.categories.could.not.delete")] as JSON)
        }
    }

    @Restriction(permission = "blog.remove", entity_param = "id", domain = BlogComment)
    def deleteComment () {
        if(blogService.deleteBlogComment(params.long("id"))) {
            render([status: "success", message: g.message(code: "blog.comment.delete.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "blog.comment.delete.failure")] as JSON)
        }
    }

    @Restriction(permission = "blog.remove", entity_param = "ids", domain = BlogComment)
    def deleteSelectedComments () {
        if (blogService.deleteSelectedComments(params.list("ids").collect{it.toLong()})) {
            render([status: "success", message: g.message(code: "selected.comments.delete.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "selected.comments.could.not.delete")] as JSON);
        }
    }

    @License(required = "allow_blog_feature")
    def loadBlogSettingView () {
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.BLOG)
        render (view: "/plugins/blog/admin/blogSetting", model: [config: config])
    }

    @License(required = "allow_blog_feature")
    def loadPostForMultiSelect() {
        params.max = params.max ?: "10"
        Integer count = blogService.getPostsCount(params)
        params.isPublished = "true";
        List<BlogPost> postList = commonService.withOffset(params.max, params.offset, count){max, offset, _count ->
            params.offset = offset;
            blogService.getPosts(params)
        }
        render(view: "/plugins/blog/admin/blogPost/loadPostMultiSelect", model: [count: count, postList: postList])
    }

    def selectPosts() {
        List<Long> postIds = params.list("post").collect { it.toLong() }
        List<BlogPost> posts = blogService.getPostsInOrder(postIds)
        render(view: "/plugins/blog/admin/blogPost/postSelection", model: [posts: posts])
    }

    def isUnique() {
        render(commonService.responseForUniqueField(BlogCategory, params.long("id"), params.field, params.value) as JSON)
    }
    def isBlogCategoryUrlUnique() {
        render(commonService.responseForUniqueField(BlogCategory, params.long("id"), params.field, params.value) as JSON)
    }
    def isPostUrlUnique() {
        render(commonService.responseForUniqueField(BlogPost, params.long("id"), params.field, params.value) as JSON)
    }
    def isPostUnique() {
        render(commonService.responseForUniqueField(BlogPost, params.long("id"), params.field, params.value) as JSON)
    }

    def loadBlogPostForSelection() {
        params.max = params.max ?: "10"
        Integer count = blogService.getPostsCount(params)
        List<BlogPost> posts = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset;
            blogService.getPosts(params);
        }
        render(view: "/plugins/blog/admin/blogPost/selectionPanel", model: [count: count, posts: posts]);
    }

    def loadCommentStatusOption() {
        render view: "/plugins/blog/admin/blogComment/statusOption";
    }

    def changeCommentStatus() {
        List<Long> ids = params.list("id")*.toLong();
        String status = params.status;
        if(blogService.changeCommentStatus(ids, status)) {
            render([status: "success", message: g.message(code: "blog.comment.update.success")] as JSON)
        }else {
            render([status: "success", message: g.message(code: "blog.comment.update.failed")] as JSON)
        }
    }

    def loadSpamOption() {
        render view: "/plugins/blog/admin/blogComment/spamOption";
    }

    def changeSpam() {
        List<Long> ids = params.list("id")*.toLong();
        Boolean spam = params.spam == "true";
        if(blogService.changeSpam(ids, spam)) {
            render([status: "success", message: g.message(code: "blog.comment.update.success")] as JSON)
        }else {
            render([status: "success", message: g.message(code: "blog.comment.update.failed")] as JSON)
        }
    }

    def loadPostStatusOption() {
        render view: "/plugins/blog/admin/blogPost/statusOption";
    }

    def changePostStatus() {
        List<Long> ids = params.list("id")*.toLong();
        Boolean status = params.status == "true";
        if(blogService.changePostStatus(ids, status)) {
            render([status: "success", message: g.message(code: "blog.post.update.success")] as JSON)
        }else {
            render([status: "success", message: g.message(code: "blog.post.update.failed")] as JSON)
        }
    }
}