<%@page import="com.webcommander.constants.NamedConstants; com.webcommander.constants.DomainConstants; com.webcommander.webcommerce.Category" %>
<g:set var="productService" bean="productService"/>
<div class="header">
    <span class="item-group entity-count title">
        <g:message code="${params.isCombined ? "combined.product" : "product"}"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar">
        <div class="tool-group">
            <ui:hierarchicalSelect class="medium category-selector" domain="${Category}" prepend="${["": g.message(code: "all.categories"), "root" : g.message(code: "root")]}"/>
        </div>
        <div class="tool-group">
            <ui:namedSelect class="medium type-selector" key="${["": g.message(code: "all.product.type")] + NamedConstants.PRODUCT_TYPE}" name="productType"/>
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
            <span class="toolbar-item switch-menu collapsed"><i></i></span>
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="app-tab-content-container">
    <table class="content">
        <colgroup>
            <col class="select-column">
            <col class="name-column">
            <col class="sku-column">
            <col class="price-column">
            <col class="stock-column">
            <col class="availability-column">
            <col class="created-column">
            <col class="updated-column">
            <g:if test="${params.parent}">
                <col class="parent-column">
            </g:if>
            <col class="actions-column">
        </colgroup>
        <tr>
            <th class="select-column"><input type="checkbox" class="check-all multiple"></th>
            <th><g:message code="product.name"/></th>
            <th><g:message code="sku"/></th>
            <th><g:message code="price"/></th>
            <th><g:message code="stock"/></th>
            <th class="mark-column"><g:message code="availability"/></th>
            <th><g:message code="created"/></th>
            <th><g:message code="updated"/></th>
            <g:if test="${params.parent}">
                <th><g:message code="ordering"/></th>
            </g:if>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:if test="${products}">
            <g:each in="${products}" var="product">
                <g:set var="tooltipNotification" value="${[positive: 'Everyone', negative: 'Not Available', diplomatic: 'Custom']}"/>
                <tr>
                    <td class="select-column"><input type="checkbox" class="multiple" entity-id="${product.id}"></td>
                    <td>${product.name.encodeAsBMHTML()}</td>
                    <td>${product.sku.encodeAsBMHTML()}</td>
                    <td class="numeric-column"><span>${product.basePrice.toAdminPrice()}</span></td>
                    <td class="numeric-column">
                    <g:if test="${product.isInventoryEnabled}">
                        <span class="stock-status ${product.availableStock < 1 ? 'out-of-stock' : (product.availableStock <= product.lowStockLevel ? 'low-stock' : 'in-stock')}">${product.availableStock}</span>
                    </g:if>
                    <g:else>
                        <span class="stock-status stock-not-tracked"><g:message code="n.a"/></span>
                    </g:else>
                    </td>
                    <td><span class="status ${product.isAvailable ?
                            (product.availableFor == DomainConstants.PRODUCT_AVAILABLE_FOR.EVERYONE ? DomainConstants.STATUS.POSITIVE : DomainConstants.STATUS.DIPLOMATIC) : DomainConstants.STATUS.NEGATIVE}"
                              title="${tooltipNotification[product.isAvailable ? (product.availableFor == DomainConstants.PRODUCT_AVAILABLE_FOR.EVERYONE ? DomainConstants.STATUS.POSITIVE : DomainConstants.STATUS.DIPLOMATIC) : DomainConstants.STATUS.NEGATIVE]}"></span>
                    </td>
                    <td>${product.created.toAdminFormat(true, false, session.timezone)}</td>
                    <td>${product.updated.toAdminFormat(true, false, session.timezone)}</td>
                    <g:if test="${params.parent}">
                        <td class="order editable" entity-id="${product.id}" restrict="numeric">
                            ${product.idx}
                        </td>
                    </g:if>
                    <td class="actions-column">
                        <g:if test="${params.parent && params.sort == "idx"}">
                            <span class="tool-icon move-controls">
                                <span order-dir="up" class="move-up"></span>
                                <span order-dir="down" class="move-down"></span>
                            </span>
                        </g:if>
                        <span class="action-navigator collapsed" product-type="${product.productType}" auto-type-exclude="url" entity-id="${product.id}" entity-name="${product.name.encodeAsBMHTML()}" entity-url="${product.url.encodeAsBMHTML()}" entity-owner_id="${product.createdBy?.id}" entity-type="product"></span>
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="9"><g:message code="no.product.created"/></td>
            </tr>
        </g:else>
    </table>
</div>
<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>