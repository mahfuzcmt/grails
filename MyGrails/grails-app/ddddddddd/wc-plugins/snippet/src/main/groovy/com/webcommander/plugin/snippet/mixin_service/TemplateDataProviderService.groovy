package com.webcommander.plugin.snippet.mixin_service

import com.webcommander.models.TemplateData
import com.webcommander.plugin.snippet.Snippet
import com.webcommander.util.AppUtil
import com.webcommander.util.DomainUtil

class TemplateDataProviderService {

    Map collectSnippetTypeContent(TemplateData templateData, Snippet snippet) {
        Map data = DomainUtil.toMap(snippet, [exclude: ["createdBy", "parent"]])
        templateData.resources.add "snippet/snippet-${snippet.id}/"
        return data
    }

    List<Map> collectSnippetTypeContents(TemplateData templateData) {
        return Snippet.list().collect {
            return collectSnippetTypeContent(templateData, it)
        }
    }
}
