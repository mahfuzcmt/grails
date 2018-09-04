<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.webcommerce.Category" %>
<div class="header">
    <span class="item-group entity-count title">
        <g:message code="categories"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar">
        <div class="tool-group">
            <ui:hierarchicalSelect class="medium category-selector" domain="${Category}" prepend="${["": g.message(code: "all.categories"), "root": g.message(code: "root")]}" />
        </div>
    </div>
    <div class="toolbar toolbar-right">
        <div class="tool-group action-header" style="display: none">
            <g:select class="action-on-selection" name="action" from="${[ g.message(code: "with.selected"), g.message(code: "remove"), g.message(code: "bulk.edit")]}" keys="['', 'remove', 'bulkEdit']"/>
        </div>
        <form class="search-form tool-group">
            <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
        </form>
        <div class="tool-group">
            <span class="toolbar-item add-filter" title="<g:message code="advance.search"/>"><i></i></span>
            <span class="toolbar-item remove-filter disabled" title="<g:message code="remove.search"/>"><i></i></span>
        </div>
        <div class="tool-group action-tool action-menu collapsed">
            <span class="tool-text"><g:message code="actions"/></span><span class="action-dropper"></span>
        </div>
        <div class="tool-group toolbar-btn create"><i></i><g:message code="create"/></div>
        <div class="tool-group">
            <span class="toolbar-item switch-menu"><i></i></span>
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="app-tab-content-container">
    <table class="content">
        <colgroup>
            <col class="select-column">
            <col class="status-column">
            <col class="name-column">
            <col class="sku-column">
            <col class="parent-column">
            <col class="create-column">
            <col class="updated-column">
            <col class="no-of-product-column">
            <col class="ordering-column">
            <col class="actions-column">
        </colgroup>
        <tr>
            <th class="select-column"><input type="checkbox" class="check-all multiple"></th>
            <th class="status-column"><g:message code="availability"/></th>
            <th><g:message code="category.name"/></th>
            <th><g:message code="sku"/></th>
            <th><g:message code="parent.category"/></th>
            <th><g:message code="created"/></th>
            <th><g:message code="updated"/></th>
            <th><g:message code="number.of.products"/></th>
            <th><g:message code="ordering"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:if test="${categories}">
            <g:set var="status" value="${[everyone: 'positive', customer: 'negative', selected: 'diplomatic']}"/>
            <g:each in="${categories}" var="category">
                <g:set var="tooltipNotification" value="${[positive: 'Everyone', negative: 'Not Available', diplomatic: 'Custom']}"/>
                <tr>
                    <td class="select-column"><input type="checkbox" class="multiple" entity-id=${category.id}></td>
                    <td class="status-column"><span class="status ${category.isAvailable ? (category.availableFor == DomainConstants.CATEGORY_AVAILABLE_FOR.EVERYONE ? DomainConstants.STATUS.POSITIVE : DomainConstants.STATUS.DIPLOMATIC) : DomainConstants.STATUS.NEGATIVE}"
                                                    title="${tooltipNotification[category.isAvailable ? (category.availableFor == DomainConstants.CATEGORY_AVAILABLE_FOR.EVERYONE ? DomainConstants.STATUS.POSITIVE : DomainConstants.STATUS.DIPLOMATIC) : DomainConstants.STATUS.NEGATIVE]}"></span></td>
                    <td>${category.name.encodeAsBMHTML()}</td>
                    <td>${category.sku.encodeAsBMHTML()}</td>
                    <td>${category.parent?.name.encodeAsBMHTML()}</td>
                    <td>${category.created.toAdminFormat(true, false, session.timezone)}</td>
                    <td>${category.updated.toAdminFormat(true, false, session.timezone)}</td>
                    <td>${category.productCount}</td>
                    <td class="order editable" entity-type="category" entity-id="${category.id}">
                        <span>${category.idx}</span><span class="tool-icon edit"></span>
                    </td>
                    <td class="actions-column">
                        <g:if test="${params.parent && params.sort == "idx"}">
                            <span class="tool-icon move-controls">
                                <span order-dir="up" class="move-up"></span>
                                <span order-dir="down" class="move-down"></span>
                            </span>
                        </g:if>
                        <span class="action-navigator collapsed" entity-id="${category.id}" entity-url="${category.url.encodeAsBMHTML()}" entity-name="${category.name.encodeAsBMHTML()}" entity-owner_id="${category.createdBy?.id}" entity-type="category"></span>
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="10"><g:message code="no.category.created"/></td>
            </tr>
        </g:else>
    </table>
</div>
<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" max="${params.max}" offset="${params.offset}"></paginator>
</div>