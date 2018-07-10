<div class="left-panel">
    <span class="item-group entity-count title">
        <g:message code="form"/> (<span class="count">${count}</span>)
    </span>
    <div class=left-panel-header>
        <div class="toolbar">
            <form class="search-form tool-group widget-search-from">
                <input type="text" class="search-text" placeholder="Search"><button type="submit" class="icon-search"></button>
            </form>
            <div class="tool-group toolbar-btn create"><i></i><g:message code="new"/></div>
        </div>
    </div>

    <div class="body explorer-items blocktype-list">
        <g:each in="${forms}" var="form">
            <div class="explorer-item blocklist-item ${form.id == selected ? "selected" : ""}" entity-id="${form.id}" entity-name="${form.name}">
                <span class="float-menu-navigator"></span>
                <span class="title listitem-title">${form.name.encodeAsBMHTML()}</span>
                <span class="listitem-count blocklist-subitem-summary-view">${form.submissionCount} <g:message code="submissions"/> </span>
            </div>
        </g:each>
    </div>
</div>