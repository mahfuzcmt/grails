package com.webcommander.controllers.rest.admin.content

import com.webcommander.authentication.annotations.Restriction
import com.webcommander.constants.DomainConstants
import com.webcommander.content.Article
import com.webcommander.content.ContentService
import com.webcommander.content.Section
import com.webcommander.rest.throwable.ApiException
import com.webcommander.util.RestProcessor
import org.apache.commons.httpclient.HttpStatus

class ApiArticleAdminController extends RestProcessor{
    ContentService contentService

    @Restriction(permission = "article.view.list")
    def list() {
        params.max = params.max ?: "-1"
        params.offset = params.offset ?: "-1"
        def config = [
            createdBy: [
                default: ["id", "fullName"]
            ]
        ]
        List<Article> articles = contentService.getArticles(params)
        rest(articles: articles, config)
    }

    @Restriction(permission = "article.view.list")
    def info() {
        Article article = contentService.getArticle(params.long("id"))
        if(!article && article.isInTrash) {
            throw new ApiException("article.not.found", HttpStatus.SC_NOT_FOUND)
        }
        def config = [
            createdBy: [
                default: ["id", "fullName"]
            ]
        ]
        rest(article: article, config)
    }

    @Restriction(permission = "section.create", params_not_exist = "id")
    def create() {
        Article article = contentService.saveArticle(params, request.getAttribute(DomainConstants.REQUEST_ATTR_KEYS.ADMIN))
        if (article) {
            rest([status: "success", id: article.id])
        } else {
            throw new ApiException("article.save.failure")
        }
    }

    @Restriction(permission = "article.remove", entity_param = "id", domain = Article, owner_field = "createdBy")
    def delete() {
        Boolean result = contentService.putArticleInTrash(params.long("id"), true)
        if(result) {
            rest([status: "success", message: g.message(code: "article.delete.success")])
        } else {
            throw new ApiException("article.delete.failure")
        }
    }

    @Restriction(permission = "section.view.list")
    def sectionList() {
        params.max = params.max ?: "-1"
        params.offset = params.offset ?: "-1"
        List<Section> sections = contentService.getSections(params)
        rest(sections: sections)
    }
}
