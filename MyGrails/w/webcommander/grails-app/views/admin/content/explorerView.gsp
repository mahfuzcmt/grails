<div class="header">
    <span class="header-title"><g:message code="sections"/></span>
    <div class="toolbar toolbar-right">
        <form class="search-form tool-group">
            <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
        </form>
        <div class="tool-group action-tool action-menu collapsed">
            <span class="tool-text"><g:message code="actions"/></span><span class="action-dropper"></span>
        </div>
        <div class="tool-group toolbar-btn create menu"><i></i><g:message code="create"/></div>
        <div class="tool-group">
            <span class="toolbar-item switch-menu collapsed"><i></i></span>
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="app-tab-content-container">
    <div class="left-panel panel tree-panel with-title">
        <span class="tree-title"><g:message code="sections"/></span>
        <div class="body"></div>
    </div>
    <div class="wcui-resizable-layout-scroller"></div>
    <g:include controller="content" action="explorePanel" params="${new HashMap(params)}"/>
</div>