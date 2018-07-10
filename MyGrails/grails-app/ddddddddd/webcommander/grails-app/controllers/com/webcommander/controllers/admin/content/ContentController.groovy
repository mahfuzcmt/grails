package com.webcommander.controllers.admin.content

import com.webcommander.authentication.annotations.Restriction
import com.webcommander.authentication.annotations.Restrictions
import com.webcommander.common.CommonService
import com.webcommander.content.Article
import com.webcommander.content.ContentService
import com.webcommander.content.Section
import com.webcommander.manager.HookManager
import com.webcommander.throwables.AttachmentExistanceException
import grails.converters.JSON

class ContentController {
    CommonService commonService
    ContentService contentService

    @Restriction(permission = "article.view.list")
    def loadArticleView() {
        params.max = params.max ?: "10";
        Integer count = contentService.getArticlesCount(params)
        List<Article> articles = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            contentService.getArticles(params)
        }
        render(view: "/admin/content/articleView", model: [count: count, articles: articles])
    }

    @Restriction(permission = "section.view.list")
    def loadSectionView() {
        params.max = params.max ?: "10";
        Integer count = contentService.getSectionCount(params)
        List<Section> sections = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            contentService.getSections(params)
        }
        render(view: "/admin/content/sectionView", model: [count: count, sections: sections])
    }

    @Restrictions([
        @Restriction(permission = "article.create", params_not_exist = "id"),
        @Restriction(permission = "article.edit", params_exist = "id", entity_param = "id", domain = Article)
    ])
    def editArticle() {
        Article article = params.id ? contentService.getArticle(params.long("id")) : new Article();
        render(view: "/admin/content/article", model: [article: article]);
    }

    @Restrictions([
            @Restriction(permission = "section.edit", params_exist = "id", entity_param = "id", domain = Section),
            @Restriction(permission = "section.create", params_not_exist = "id")
    ])
    def editSection() {
        Long id = params.long("id")
        Section section = params.id ? contentService.getSection(id) : new Section();
        render(view: "/admin/content/section", model: [section: section]);
    }

    def viewArticle() {
        Article article = contentService.getArticle(params.long("id"));
        render(view: "/admin/content/viewArticle", model: [article: article])
    }

    @Restrictions([
        @Restriction(permission = "article.edit", params_exist = "id", entity_param = "id", domain = Article, owner_field = "createdBy"),
        @Restriction(permission = "article.create", params_not_exist = "id")
    ])
    def saveArticle() {
        params.remove("controller");
        params.remove("action");
        if (params.deleteTrashItem) {
            contentService.deleteTrashItemAndSaveCurrent(params.name)
        }
        if (contentService.saveArticle(params, session.admin)) {
            render([status: "success", message: g.message(code: "article.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "article.save.failure")] as JSON)
        }
    }

    @Restriction(permission = "section.create", params_not_exist = "id")
    def saveSection() {
        params.remove("controller");
        params.remove("action");
        if (contentService.saveSection(params, session.admin)) {
            render([status: "success", message: g.message(code: "section.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "section.save.failure")] as JSON)
        }
    }

    @Restriction(permission = "article.remove", entity_param = "id", domain = Article, owner_field = "createdBy")
    def deleteArticle() {
        Long id = params.long("id");
        try {
            boolean deleted = contentService.putArticleInTrash(id, params.at2_reply, params.at1_reply);
            if (deleted) {
                render([status: "success", message: g.message(code: "article.delete.success")] as JSON)
            } else {
                render([status: "error", message: g.message(code: "article.delete.failure")] as JSON)
            }
        } catch(AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    @Restriction(permission = "article.create", params_exist = "id")
    def copyArticle() {
        Long id = params.long("id")
        boolean copied = contentService.copyArticle(id, session.admin)
        if (copied) {
            render([status: "success", message: g.message(code: "article.copy.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "article.copy.failure")] as JSON)
        }
    }

    def advanceFilter() {
        render(view: "/admin/content/filter", model: [d: true])
    }

    @Restriction(permission = "section.remove", entity_param = "id", domain = Section)
    def deleteSection() {
        Long id = params.long("id");
        Integer articleCount = Article.where {
            section { id == id }
        }.count()
        Integer sectionCount = Section.where {
            parent { id == id }
        }.count()
        render([articleCount: articleCount, sectionCount: sectionCount] as JSON)
    }

    @Restriction(permission = "section.remove", entity_param = "id", domain = Section)
    def confirmDeleteSection() {
        try{
            Long id = params.long("id")
            boolean deleted = contentService.deleteSection(id, params.at2_reply, params.at1_reply)
            if (deleted) {
                render([status: "success", message: g.message(code: "section.delete.success")] as JSON)
            } else {
                render([status: "error", message: g.message(code: "section.delete.failure")] as JSON)
            }
        } catch(AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    @Restriction(permission = "article.remove", entity_param = "ids", domain = Article, owner_field = "createdBy")
    def deleteSelectedArticles() {
        def ids = [];
        params.list("ids").each {
            ids.add(it.toLong(0))
        }
        int deleteCount = contentService.putSelectedArticlesInTrash(ids);
        int total = ids.size();
        if (deleteCount == total) {
            render([status: "success", message: g.message(code: "selected.articles.delete.success")] as JSON)
        } else if(deleteCount == 0) {
            render([status: "error", message: g.message(code: "selected.articles.could.not.delete")] as JSON)
        } else {
            render([status: "warning", message: g.message(code: "selected.not.deleted", args: [total - deleteCount, total, g.message(code: "articles")])] as JSON)
        }
    }

    def copySelectedArticles() {
        def ids = [];
        params.list("ids").each {
            ids.add(it.toLong(0))
        }
        if (contentService.copySelectedArticles(ids, session.admin)) {
            render([status: "success", message: g.message(code: "selected.articles.copy.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "selected.articles.could.not.copy")] as JSON)
        }
    }

    @Restriction(permission = "section.remove", entity_param = "ids", domain = Section)
    def deleteSelectedSections() {
        def ids = [];
        params.list("ids").each {
            ids.add(it.toLong(0))
        }
        if (contentService.deleteSelectedSections(ids)) {
            render([status: "success", message: g.message(code: "selected.sections.delete.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "selected.sections.could.not.delete")] as JSON);
        }
    }

    @Restrictions([
        @Restriction(permission = "section.view.list"),
        @Restriction(permission = "article.view.list")
    ])
    def loadExplorerView() {
        render(view: "/admin/content/explorerView", model: [d: true])
    }

    @Restrictions([
        @Restriction(permission = "section.view.list"),
        @Restriction(permission = "article.view.list")
    ])
    def explorePanel() {
        Long id = params.id ? params.long("id") : 0;
        Integer max = params.max = params.max?.toInteger() ?: 10;
        Integer offset = params.offset = params.offset?.toInteger() ?: 0

        Integer articleCount = contentService.getChildArticleCount(id, params)
        Integer sectionCount = contentService.getChildSectionCount(id, params);
        Integer count = sectionCount + articleCount;

        List<Section> sections = [];
        List<Article> articles = [];
        if(max == -1 || sectionCount > offset) {
            sections = contentService.getSections(params, [offset: offset, max: max])
        }
        if(max == -1 || sectionCount + articleCount > offset && (sections.size() < max)) {
            max = max == -1 ? max : max - sections.size()
            offset = offset - sectionCount
            articles = contentService.getArticleForExplorer(params, [offset: offset, max: max]);
        }
        Map model = [contents: [section: sections, article: articles], count: count, params: params, max: max, offset: offset]
        model = HookManager.hook("content-explorer-view-model", model)
        render(view: "/admin/content/explorerPanel", model: model);
    }

    def sectionTree() {
        Long parentId = params.long("key");
        List childs = contentService.getChildSections(parentId);
        for(it in childs)
        {
            it.name=it.name.encodeAsBMHTML();
        }
        render(childs as JSON)
    }

    def isArticleUnique() {
        render(commonService.responseForUniqueField(Article, params.long("id"), params.field, params.value) as JSON)
    }

    def isSectionUnique() {
        if (commonService.isUnique(Section, params)) {
            render([status: "success", message: g.message(code: "provided.field.available", args: [params.field, params.value])] as JSON)
        } else {
            render([status: "error", message: g.message(code: "provided.field.value.exists", args: [params.field, params.value])] as JSON)
        }
    }

    def restoreArticleFromTrash() {
        String field = params.field;
        String value = params.value;
        Long id = contentService.restoreArticleFromTrash(field, value);
        if (id) {
            render([status: "success", message: g.message(code: "article.save.success"), type: "article", id: id] as JSON)
        }
    }

    @Restriction(permission = "article.view.list")
    def loadArticlesForSelection() {
        params.max = params.max ?: "10";
        Integer count = contentService.getArticlesCount(params)
        List<Article> articles = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            contentService.getArticlesForWidget(params)
        }
        render(view: "/admin/content/selectionPanel",model: [count: count, articles: articles])
    }
}
