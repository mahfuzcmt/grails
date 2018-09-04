package com.webcommander.plugin.blog.mixin_service

import com.webcommander.AppResourceTagLib
import com.webcommander.admin.Operator
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.installation.template.InstallationDataHolder
import com.webcommander.manager.CloudStorageManager
import com.webcommander.models.TemplateData
import com.webcommander.plugin.blog.app.BlogResourceTagLib
import com.webcommander.plugin.blog.content.BlogCategory
import com.webcommander.plugin.blog.content.BlogComment
import com.webcommander.plugin.blog.content.BlogPost
import com.webcommander.util.AppUtil
import com.webcommander.util.DomainUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

class TemplateInstallationService {


    private static CommonService _commonService
    private static CommonService getCommonService() {
        return _commonService ?: (_commonService = CommonService.getInstance())
    }

    Long saveBlogCategoryTypeWidgetContent(TemplateData templateData, InstallationDataHolder installationDataHolder, Map data) {
        BlogCategory blogCategory = new BlogCategory()
        DomainUtil.populateDomainInst(blogCategory, data)
        if(!commonService.isUnique(blogCategory, "name")) {
            blogCategory.name  = commonService.getCopyNameForDomain(blogCategory)
        }
        if(!commonService.isUnique(blogCategory, "url")) {
            blogCategory.url  = commonService.getUrlForDomain(blogCategory)
        }
        if(blogCategory.hasErrors()) {
            return null
        }
        blogCategory.cloudConfig =  CloudStorageManager.getDefaultCloudConfig()
        blogCategory.save()
        String srcPath = BlogResourceTagLib.getBlogCategoryRelativeUrl(data.id.toString())
        String destPath = BlogResourceTagLib.getBlogCategoryRelativeUrl(blogCategory.id.toString())
        moveTemplateData(installationDataHolder,"${AppResourceTagLib.RESOURCES}/${srcPath}", destPath)
        return blogCategory.id
    }

    Long saveBlogPostTypeWidgetContent(TemplateData templateData, InstallationDataHolder installationDataHolder, Map data) {
        BlogPost blogPost = new BlogPost()
        data.date = data.date.toDate("yyyy-MM-dd'T'HH:mm:ss")
        List categories = []
        data.categories?.each {
            Long parentId = installationDataHolder.getContentMapping(DomainConstants.WIDGET_CONTENT_TYPE.BLOG_CATEGORY, it, "id")
            if(parentId) {
                categories.add(parentId)
            }
        }
        data.categories = []
        DomainUtil.populateDomainInst(blogPost, data, [exclude: ["metaTags", "comments", "author", "customers"]])
        if(!commonService.isUnique(blogPost, "name")) {
            blogPost.name  = commonService.getCopyNameForDomain(blogPost)
        }
        if(!commonService.isUnique(blogPost, "url")) {
            blogPost.url  = commonService.getUrlForDomain(blogPost)
        }
        if(!blogPost.author){
            blogPost.author =  Operator.get(AppUtil.getLoggedOperator())
        }
        if(blogPost.hasErrors()) {
            return null
        }
        blogPost.cloudConfig =   CloudStorageManager.getDefaultCloudConfig()
        blogPost.save()
        if(categories) {
            BlogCategory.findAllByIdInList(categories).each {
                it.addToPosts(blogPost)
                it.merge()
            }
        }
        data.comments.each {
            BlogComment comment = new BlogComment(post: blogPost)
            DomainUtil.populateDomainInst(comment, it, [exclude: ["post"]])
            comment.save()
        }
        String srcPath = BlogResourceTagLib.getBlogPostRelativeUrl(data.id.toString())
        String destPath = BlogResourceTagLib.getBlogPostRelativeUrl(blogPost.id.toString())
        moveTemplateData(installationDataHolder, "${AppResourceTagLib.RESOURCES}/${srcPath}", destPath)
        return blogPost.id
    }
}
