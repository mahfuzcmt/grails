<%@ page import="com.webcommander.webcommerce.ShippingClass" %>
<div class="left-panel ">
    <span class="item-group entity-count title">
        <g:message code="profile"/> (<span class="count">${profiles?.size()}</span>)
    </span>

    <div class="left-panel-header text-center">
        <div class="create-profile btn btn-cta">
            <i></i><g:message code="add.new.filter"/>
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
        <g:each in="${profiles}" var="profile">
            <div class="explorer-item blocklist-item" entity-id="${profile.id}" entity-name="${profile.name}">
                <span class="float-menu-navigator"></span>
                <span class="title listitem-title">${profile.name.encodeAsBMHTML()}</span>
                <g:if test="${profile.isDefault}">
                    <span class="wc-tag">Default</span>
                </g:if>
                <span class="listitem-count blocklist-subitem-summary-view">${profile.description.encodeAsBMHTML()}</span>
            </div>
        </g:each>
    </div>
</div>


