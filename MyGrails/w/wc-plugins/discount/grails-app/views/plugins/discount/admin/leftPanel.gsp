<%@ page import="com.webcommander.plugin.discount.NameConstants" %>
<div class="left-panel">
    <span class="item-group entity-count title">
        <g:message code="discount"/> (<span class="count">${count}</span>)
    </span>

    <div class="left-panel-header text-center">
        <div class="toolbar">
            <div class="create btn btn-cta"><i></i><g:message code="add.new.discount"/></div>
        </div>
    </div>

    <div class="left-panel-header filter-search-wrapper">
        <div class="toolbar">
            <form class="search-form tool-group">
                <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
            </form>
        </div>
    </div>

    <div class="body explorer-items blocktype-list sortable-container">
        <g:each in="${discounts}" var="discount">
            <div class="explorer-item blocklist-item ${discount.id == selected ? "selected" : ""}" entity-id="${discount.id}" entity-name="${discount.name}">
                <span class="float-menu-navigator"></span>
                <span class="title list item-title">${discount.name.encodeAsBMHTML()}</span>
                <div class="description"></div>
                <span class="hidden type">${discount.type}</span>
            </div>
        </g:each>
    </div>

</div>