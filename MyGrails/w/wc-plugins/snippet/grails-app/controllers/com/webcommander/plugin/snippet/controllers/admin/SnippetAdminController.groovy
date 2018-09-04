package com.webcommander.plugin.snippet.controllers.admin

import com.webcommander.authentication.annotations.License
import com.webcommander.authentication.annotations.Restriction
import com.webcommander.authentication.annotations.Restrictions
import com.webcommander.common.CommonService
import com.webcommander.manager.PathManager
import com.webcommander.plugin.snippet.SnippetRepositoryService
import com.webcommander.plugin.snippet.constants.SnippetConstants
import com.webcommander.util.AppUtil
import grails.converters.JSON
import com.webcommander.plugin.snippet.Snippet
import com.webcommander.plugin.snippet.SnippetService
import grails.util.Holders

class SnippetAdminController {
    CommonService commonService
    SnippetService snippetService
    SnippetRepositoryService snippetRepositoryService

    @Restriction(permission = "snippet.edit", params_exist = "id", entity_param = "id", domain = Snippet)
    def editContent() {
        // TODO
        String icons = new File(PathManager.getSystemResourceRoot("plugins/snippet/images/icons/info.json")).text
        render(view: "/plugins/snippet/admin/editor/snippetContentEditor", model: [id: params.id, icons: icons])
    }

    def renderSnippet() {
        String content = snippetService.getSnippetContent(params)
        render(view: "/plugins/snippet/admin/editor/snippetPage", model: [snippetContent: content])
    }

    def snippetCss() {
        response.setContentType("text/css");
        render(text: snippetService.getSnippetCss(params))
    }

    def templateThumbView() {
        List templates = snippetRepositoryService.getSnippetTemplates(params.repositoryType ?: SnippetConstants.SNIPPET_REPOSITORY_TYPE.STANDARD, params)
        render(view: "/plugins/snippet/admin/editor/templateThumbView", model: [templates: templates])
    }

    @License(required = "allow_snippet_feature")
    @Restriction(permission = "snippet.edit", params_exist = "id", entity_param = "id", domain = Snippet)
    def saveContent() {
        Boolean result = snippetService.saveContent(params);
        if(result) {
            render([status: "success", message: g.message(code: "snippet.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "snippet.save.failure")] as JSON)
        }
    }

    @License(required = "allow_snippet_feature")
    @Restriction(permission = "snippet.view.list")
    def loadSnippetView() {
        params.max = params.max ?: "10";
        Integer count = snippetService.getSnippetCount(params);
        List<Snippet> snippetList = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            snippetService.getSnippetList(params)
        }
        render(view: "/plugins/snippet/admin/snippetView", model: [count: count, snippetList: snippetList])
    }

    @License(required = "allow_snippet_feature")
    @Restrictions([
        @Restriction(permission = "snippet.create", params_not_exist = "id"),
        @Restriction(permission = "snippet.edit", params_exist = "id", entity_param = "id", domain = Snippet)
    ])
    def editSnippet() {
        Snippet snippet = params.id? snippetService.getSnippet(params.long("id")) : new Snippet();
        render(view: "/plugins/snippet/admin/editSnippet", model: [snippet: snippet]);
    }

    @License(required = "allow_snippet_feature")
    @Restrictions([
        @Restriction(permission = "snippet.create", params_not_exist = "id"),
        @Restriction(permission = "snippet.edit", params_exist = "id", entity_param = "id", domain = Snippet)
    ])
    def saveSnippet() {
        def isSaved = snippetService.saveSnippet(params);
        if(isSaved) {
            render([status: "success", message: g.message(code: "snippet.save.success")] as JSON);
        } else {
            render([status: "error", message: g.message(code: "snippet.save.failure")] as JSON);
        }
    }

    @Restriction(permission = "snippet.remove", entity_param = "id", domain = Snippet, owner_field = "createdBy")
    def deleteSnippet() {
        Long id = params.long("id");
        boolean isDeleted = snippetService.deleteSnippet(id);
        if(isDeleted) {
            render([status: "success", message: g.message(code: "snippet.delete.success")] as JSON);
        } else {
            render([status: "error", message: g.message(code: "snippet.delete.failure")] as JSON)
        }
    }

    @Restriction(permission = "snippet.remove", entity_param = "ids", domain = Snippet, owner_field = "createdBy")
    def deleteSelectedSnippets() {
        def ids = [];
        params.list("ids").each {
            ids.add(it.toLong(0))
        }
        int deleteCount = snippetService.deleteSelectedSnippets(ids);
        int total = ids.size();
        if (deleteCount == total) {
            render([status: "success", message: g.message(code: "selected.snippets.delete.success")] as JSON)
        } else if(deleteCount == 0) {
            render([status: "error", message: g.message(code: "selected.snippets.delete.failure")] as JSON)
        } else {
            render([status: "warning", message: g.message(code: "selected.not.deleted", args: [total - deleteCount, total, g.message(code: "snippets")])] as JSON)
        }
    }

    def isSnippetUnique() {
        render(commonService.responseForUniqueField(Snippet, params.long("id"), params.field, params.value) as JSON);
    }

    def advanceFilter() {
        render(view: "/plugins/snippet/admin/filter", model: [d: true])
    }

    @License(required = "allow_snippet_feature")
    @Restriction(permission = "snippet.create")
    def copySnippet(){
        Long id = params.long("id")
        def isCopied = snippetService.copy(id, session.admin)
        if(isCopied) {
            render([status: "success", message: g.message(code: "snippet.copy.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "snippet.copy.failure")] as JSON)
        }

    }
}
