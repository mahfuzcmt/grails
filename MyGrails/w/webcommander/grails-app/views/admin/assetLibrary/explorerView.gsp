<div class="header">
    <div class="directory-info">
        <span class="folder-icon"></span>
        <span class="dir-name"><g:message code="public"/></span>
    </div>
    <div class="toolbar toolbar-right">
        <form class="search-form tool-group">
            <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
        </form>
        <div class="tool-group action-header">
            <g:select class="select-repository small" name="repository" from="${[ g.message(code: "public"), g.message(code: "template")]}" keys="['pub', 'template']"/>
        </div>
        <div class="tool-group toolbar-btn upload"><i></i><g:message code="upload"/></div>
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="app-tab-content-container">
    <div class="left-panel panel tree-panel with-title">
        <div class="body"></div>
    </div>
    <div class="wcui-resizable-layout-scroller"></div>
    <g:include controller="assetLibrary" action="explorerPanel" params="${new HashMap(params)}"/>
</div>