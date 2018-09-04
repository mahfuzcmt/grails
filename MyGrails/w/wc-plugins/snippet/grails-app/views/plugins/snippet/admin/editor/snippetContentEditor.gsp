<%@ page import="com.webcommander.plugin.snippet.constants.SnippetConstants" %>
<div class="snippet-template-repository">
    <div class="repository-header header">
        <div class="toolbar toolbar-left">
            <label><g:message code="filter.by"/>:</label>
            <ui:namedSelect key="${SnippetConstants.SNIPPET_REPOSITORY_NAMES}" class="snippet-repository-selector"/>
            <span class="toolbar-btn back clear-filter"><g:message code="back"/></span>
        </div>
        <div class="toolbar toolbar-right">
            <div class="tool-group toolbar-btn save disabled"><g:message code="save"/></div>
            <div class="tool-group toolbar-btn cancel"><g:message code="cancel"/></div>
        </div>
    </div>
    <div class="template-list">
    </div>
</div>
<div class="left-bar config-bar">
    <div class="config-bar-head">
        <span class="close"></span>
    </div>
    <div class="side-bar-config"></div>
</div>
<div class="app-tab-content-container">
    <div class="snipper-editor-header header">
        <div class="toolbar toolbar-left">
             <div class="tool-group toolbar-btn back"><g:message code="back.to.library"/></div>
        </div>
         <div class="toolbar toolbar-right">
             <div class="tool-group toolbar-btn save disabled"><g:message code="save"/></div>
             <div class="tool-group toolbar-btn cancel"><g:message code="cancel"/></div>
         </div>
    </div>
    <iframe class="content-editor-iframe" id="snippet-page-${id}" src="${app.baseUrl()}snippetAdmin/renderSnippet?id=${id}"></iframe>
</div>
<span style="display: none" class="icon-cache">${icons}</span>