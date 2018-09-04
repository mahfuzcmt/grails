<g:applyLayout name="_widget">
    <%
        if(request.page && !request.editMode && !request."isSnippetCsstLoadedFor${id}") {
            request.css_cache.push(appResource.snippetCssUrl(id: id))
            request."isSnippetCsstLoadedFor${id}" = true
        } else {
            out << appResource.snippetCssLink(id: id)
out << "<span class='snippetId' data-snippet-id='${id}' style='display:none;'></span>"
        }
    %>
    ${content}
</g:applyLayout>
