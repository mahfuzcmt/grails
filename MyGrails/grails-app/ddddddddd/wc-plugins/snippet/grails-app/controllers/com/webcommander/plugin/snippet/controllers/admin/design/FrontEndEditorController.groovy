package com.webcommander.plugin.snippet.controllers.admin.design

import com.webcommander.plugin.snippet.SnippetRepositoryService
import com.webcommander.util.AppUtil
import grails.converters.JSON
import grails.util.Holders


class FrontEndEditorController {
    SnippetRepositoryService snippetRepositoryService



    def snippetConfig() {
        def contentId = params?.widget?.widgetContent?.contentId
        List<Map> localArchiveTemplates = snippetRepositoryService.getSystemSnippetTemplates([:])
        List<Map> snippetTemplates = snippetRepositoryService.getTemplateSnippetTemplates([:])
        snippetTemplates.addAll(localArchiveTemplates);
        render(view: "/plugins/snippet/admin/widget/frontEndEditorConfig", model: [templates: snippetTemplates, contentId: contentId])
    }


    def iconList() {
        String icons = new File(Holders.servletContext.getRealPath("plugins/snippet/images/icons/info.json")).text
        render([icons: icons] as JSON)
    }


    def snippetContent() {
        render(text: snippet.snippetContent([uuid: params.uuid, id: params.id, fromFrontEndEditor: true]), encoding: "UTF-8")
    }


    def snippetCss() {
        render text: snippetRepositoryService.getLocalSnippetTemplateCss(params.uuid), contentType: 'text/css'
    }
}
