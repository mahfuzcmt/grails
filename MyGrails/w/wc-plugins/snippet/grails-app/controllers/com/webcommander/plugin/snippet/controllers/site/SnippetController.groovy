package com.webcommander.plugin.snippet.controllers.site

import com.webcommander.plugin.snippet.SnippetRepositoryService
import com.webcommander.plugin.snippet.SnippetService
import grails.converters.JSON

class SnippetController {
    SnippetRepositoryService snippetRepositoryService
    SnippetService snippetService

    def templateThumb() {
        Map thumb = snippetService.getThumbnail(params.uuid)
        if (thumb) {
            render(file: thumb.inputStream, contentType: thumb.mimeType)
        } else {
            render(text: "", status: 404)
        }
    }

    def templateList() {
        render snippetRepositoryService.getLocalSnippetTemplates() as JSON
    }
}
