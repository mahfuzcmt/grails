<div class="header">
    <span class="albums-icon"></span>
    <span class="item-group entity-count title">
        <g:message code="all.albums"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group">
            <label><g:message code="album.type"/>:</label>
            <g:select name="albumType" from="${[g.message(code: "none"), g.message(code: "disposable")]}" keys="${["all", "disposable"]}" class="small"/>
        </div>
        <form class="search-form tool-group">
            <input type="text" class="search-text" placeholder="<g:message code="search.image"/>"><button class="icon-search"></button>
        </form>
        <div class="tool-group action-tool action-menu collapsed">
            <span class="tool-text"><g:message code="actions"/></span><span class="action-dropper"></span>
        </div>
        <div class="tool-group toolbar-btn create"><i></i><g:message code="create"/></div>
        <div class="tool-group">
            <span class="toolbar-item switch-menu"><i></i></span>
            <span class="toolbar-item reload right" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="app-tab-content-container">
    <g:include controller="album" action="leftPanel"/>
    <g:include controller="album" action="explorerView"/>
</div>