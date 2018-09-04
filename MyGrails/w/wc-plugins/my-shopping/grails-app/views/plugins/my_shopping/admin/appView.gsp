<div class="header" xmlns="http://www.w3.org/1999/html">
    <div class="toolbar toolbar-left">
        <div class="tool-group action-header">
            <g:select class="small explorer category-type-selector" name="categoryType" from="${[ g.message(code: "all"), g.message(code: "mapped"), g.message(code: "unmapped")]}" keys="['', 'mapped', 'unmapped']"/>
        </div>
    </div>
    <div class="toolbar toolbar-right">
        <form class="search-form tool-group">
            <span class="search-indicator">
                <span class="next" title="<g:message code="next"/>"></span>
                <span class="previous" title="<g:message code="previous"/>"></span>
            </span>
            <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
        </form>
        <div class="tool-group action-tool action-menu">
            <span class="tool-text"><g:message code="actions"/></span><span class="action-dropper collapsed"></span>
        </div>
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>

<div class="app-tab-content-container">
    <div class="three-column multi-column">
        <div class="first-column columns">
            <h3><g:message code="my.shopping"/></h3>
            <div class="info-content"><g:message code="section.text.my.shopping"/></div>
        </div><div class="middle-column columns">
            <div class="column-content left-panel panel tree-panel with-title">
                <div class="tree-title"><g:message code="local.category"/> </div>
                <div class="local-category-tree body"></div>
            </div>
        </div><div class="last-column columns">
        <div class="column-content right-panel panel tree-panel with-title">
            <div class="tree-title"><g:message code="my.shopping.category"/> </div>
            <div class="my-shopping-category-tree body"></div>
            </div>
        </div>
    </div>
</div>
