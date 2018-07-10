package com.webcommander.plugin.snippet

import com.webcommander.UITagLib
import com.webcommander.common.FileService
import com.webcommander.constants.NamedConstants
import com.webcommander.manager.PathManager
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

class SnippetTagLib {
    static namespace = "snippet"
    @Autowired
    @Qualifier("org.grails.plugins.web.taglib.ApplicationTagLib")
    ApplicationTagLib g;
    @Autowired
    @Qualifier("com.webcommander.UITagLib")
    UITagLib ui

    FileService fileService

    private String cloudType = NamedConstants.CLOUD_CONFIG.SYSTEM_DEFAULT
    private String identifier
    private String path
    private InputStream inputStream

    def adminJss = { Map attrs, body ->
        out << body();
        out << app.javascript(src: "plugins/snippet/js/utility/jquery.forwardevents.js")
        out << app.javascript(src: "plugins/snippet/js/utility/imagesloaded.pkgd.min.js")
        out << app.javascript(src: "plugins/snippet/js/utility/masonry.pkgd.min.js")
    }

    def popupCreateForm = { Map attrs, body ->
        out << body()
        out << "<div class='form-row content-type-snippet'>"
        out << "<label>${g.message(code: "snippet")}</label>"
        out << ui.domainSelect(domain: Snippet, name: "contentId", value: pageScope.popup.contentId, validation: "skip@if{self::hidden} required")
        out << "</div>"
    }

    def sitePopup = { Map attrs, body ->
        def popup = pageScope.popup
        if (popup.contentType == "snippet") {
            identifier = popup.contentId
            path = SnippetResourceTagLib.getResourceRelativePath(identifier)
            inputStream = fileService.readResourceFileContentFromSystem("${path}${SnippetResourceTagLib.SNIPPET_HTML}", cloudType)
            String content = inputStream ? inputStream.text : ""
            if (request.page) {
                request.css_cache.push(SnippetResourceTagLib.getResourceCssURL(identifier))
            } else {
                out << "<link rel='stylesheet' href='${SnippetResourceTagLib.getResourceCssURL(identifier)}'>"
            }
            out << content
        } else {
            out << body()
        }
    }

    def snippetContent = { Map attrs, body ->
        if (attrs['uuid']) {
            identifier = attrs["uuid"]
            path = SnippetResourceTagLib.getModifiableResourceRelativePath(identifier)

            if(attrs['fromFrontEndEditor'])
            {
                inputStream =fileService.readModifiableResourceFromSystem("${path}/${SnippetResourceTagLib.SNIPPET_HTML}", cloudType)
                if (inputStream) {
                    out << "<link rel='stylesheet' href='${app.relativeBaseUrl()}frontEndEditor/snippetCss?uuid=${attrs['uuid']}'>"
                    out << inputStream.text
                }else{
                    out << ""
                }
            }else
            {
                inputStream =fileService.readModifiableResourceFromSystem("${path}/${SnippetResourceTagLib.SNIPPET_HTML}", cloudType)
                out << inputStream ? inputStream.text : ""
            }

        } else if(attrs["id"]) {
            identifier = attrs["id"]
            path = SnippetResourceTagLib.getResourceRelativePath(identifier)
            inputStream =fileService.readModifiableResourceFromSystem("${path}/${SnippetResourceTagLib.SNIPPET_HTML}", cloudType)
            if (inputStream) {
                out << "<link rel='stylesheet' href='${SnippetResourceTagLib.getResourceCssURL(identifier)}'>"
                out << inputStream.text
            }else{
                out << ""
            }
        } else {
            out << body()
        }
    }
}
