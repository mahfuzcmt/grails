package com.webcommander.plugin.snippet.controllers.site

import com.webcommander.plugin.snippet.SnippetRepositoryService
import grails.converters.JSON

import grails.web.Action

class DeployController {
    SnippetRepositoryService snippetRepositoryService


    def snippetTemplateList() {
        List snippets = snippetRepositoryService.getLocalSnippetTemplates(params)
        render(snippets as JSON)
    }

    def snippetContent(){
        render text: snippetRepositoryService.getLocalSnippetTemplateContent(params.uuid)
    }


    def snippetCss() {
        render text: snippetRepositoryService.getLocalSnippetTemplateCss(params.uuid)
    }
}
