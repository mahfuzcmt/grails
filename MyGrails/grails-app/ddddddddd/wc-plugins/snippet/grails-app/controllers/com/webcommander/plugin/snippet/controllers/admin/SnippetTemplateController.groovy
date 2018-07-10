package com.webcommander.plugin.snippet.controllers.admin

import com.webcommander.authentication.annotations.Restriction
import com.webcommander.authentication.annotations.Restrictions
import com.webcommander.plugin.snippet.SnippetRepositoryService
import com.webcommander.plugin.snippet.SnippetService
import grails.converters.JSON

class SnippetTemplateController {
    SnippetRepositoryService snippetRepositoryService
    SnippetService snippetService

    @Restriction(permission = "snippet_template.view.list")
    def loadAppView() {
        Integer max = params.max = params.int("max") ?: 10
        Integer offset = params.offset = params.int("offset") ?: 0
        List templates = snippetRepositoryService.getLocalSnippetTemplates(params)
        Integer count = templates.size()
        templates = templates.subList(offset, count < (max + offset) ? count: offset + max)
        render(view: "/plugins/snippet/admin/snippetTemplate/appView", model: [templates: templates, count: count])
    }

    @Restrictions([
        @Restriction(permission = "snippet_template.create", params_not_exist = "uuid"),
        @Restriction(permission = "snippet_template.edit", params_exist = "uuid")
    ])
    def create() {
        Map info = [:]
        if(params.uuid) {
            info = snippetRepositoryService.getLocalSnippetTemplate(params.uuid, true)
        }
        render(view: "/plugins/snippet/admin/snippetTemplate/create", model: [info: info])
    }

    @Restrictions([
        @Restriction(permission = "snippet_template.create", params_not_exist = "uuid"),
        @Restriction(permission = "snippet_template.edit", params_exist = "uuid")
    ])
    def save() {
        def imgFile = request.getFile("thumb");
        Boolean result = snippetService.saveSnippetTemplate(params, imgFile)
        if(result) {
            render([status: "success", message: g.message(code: "snippet.template.upload.success")] as JSON);
        } else {
            render([status: "error", message: g.message(code: "snippet.template.upload.failure")] as JSON)
        }
    }

    @Restriction(permission = "snippet_template.remove")
    def delete() {
        Boolean result = snippetService.deleteSnippetTemplate(params.id)
        if(result) {
            render([status: "success", message: g.message(code: "snippet.template.delete.success")] as JSON);
        } else {
            render([status: "error", message: g.message(code: "snippet.template.delete.failure")] as JSON)
        }
    }
}
