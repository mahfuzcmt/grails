<div class="header">
    <span class="item-group entity-count title">
        <g:message code="brands"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group action-header" style="display: none">
            <g:select class="action-on-selection" name="action" from="${[ g.message(code: "with.selected"), g.message(code: "remove")]}" keys="['', 'remove']"/>
        </div>
        <form class="search-form tool-group">
            <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
        </form>
        <div class="tool-group">
            <span class="toolbar-item add-filter" title="<g:message code="advance.search"/>"><i></i></span>
            <span class="toolbar-item remove-filter disabled" title="<g:message code="remove.search"/>"><i></i></span>
        </div>
        <div class="tool-group toolbar-btn create"><i></i><g:message code="create"/></div>
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>

<div class="app-tab-content-container">
    <table class="content">
        <colgroup>
            <col class="select-column">
            <col class="name-column">
            <col class="image-column">
            <col class="url-column">
            <col class="no-of-product-column">
            <col class="description-column">
            <col class="actions-column">
        </colgroup>
        <tr>
            <th class="select-column"><input class="check-all multiple" type="checkbox"></th>
            <th><g:message code="brand.name"/></th>
            <th><g:message code="image"/></th>
            <th><g:message code="url"/></th>
            <th><g:message code="number.of.products"/></th>
            <th><g:message code="description"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:if test="${brands}">
            <g:each in="${brands}" var="brand">
                <tr>
                    <td class="select-column"><input entity-id="${brand.id}" type="checkbox" class="multiple"></td>
                    <td>${brand.name.encodeAsBMHTML()}</td>
                    <td><img class="item-logo" src="${appResource.getBrandImageURL(image: brand, sizeOrPrefix: "thumb")}"></td>
                    <td><a  target="_blank" href="${brand.brandUrl.encodeAsBMHTML()}">${brand.brandUrl.encodeAsBMHTML()}</a></td>
                    <td>${brand.productCount}</td>
                    <td>${brand.description?.truncate(100).encodeAsBMHTML()}</td>
                    <td class="actions-column">
                        <span class="action-navigator collapsed" entity-id="${brand.id}" entity-name="${brand.name.encodeAsBMHTML()}"></span>
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="7"><g:message code="no.brand.created"/></td>
            </tr>
        </g:else>
    </table>
</div>

<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>