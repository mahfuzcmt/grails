<div class="header">
    <span class="item-group entity-count title">
        <g:message code="review.rating"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group action-header" style="display: none">
            <g:select class="action-on-selection" name="action" from="${[ g.message(code: "with.selected"), g.message(code: "remove")]}" keys="['', 'remove']"/>
        </div>
        <form class="search-form tool-group">
            <input type="text" class="search-text" name="title" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
        </form>
        <div class="tool-group">
            <span class="toolbar-item add-filter" title="<g:message code="advance.search"/>"><i></i></span>
            <span class="toolbar-item remove-filter disabled" title="<g:message code="remove.search"/>"><i></i></span>
        </div>
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>

<div class="app-tab-content-container">
    <table class="content">
        <colgroup>
            <col style="width: 3%">
            <col style="width: 20%">
            <col style="width: 17%">
            <col style="width: 20%">
            <col style="width: 15%">
            <col style="width: 13%">
            <col style="width: 7%">
        </colgroup>
        <tr>
            <th class="select-column"><input class="check-all multiple" type="checkbox"></th>
            <th><g:message code="product.name"/> </th>
            <th><g:message code="review.date"/></th>
            <th><g:message code="reviewer.name"/></th>
            <th><g:message code="rating"/></th>
            <th class="status-column"><g:message code="status"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:if test="${reviews}">
            <g:each in="${reviews}" var="review">
                <tr>
                    <td class="select-column"><input entity-id="${review.id}" class="multiple" type="checkbox"></td>
                    <td>${review.product.name.encodeAsBMHTML()}</td>
                    <td>${review.created.toAdminFormat(true, false, session.timezone)}</td>
                    <td>${review.customer ? review.customer.firstName.encodeAsBMHTML() + " " + review.customer.lastName.encodeAsBMHTML() : review.name.encodeAsBMHTML() }</td>
                    <td>
                        <div class="rating" score="${review.rating}">
                        </div>
                    </td>

                    <td class="status-column">
                        <span class="status ${ review.isActive == true ? "positive" : review.isActive == false ? "negative" : "diplomatic" }" title="${review.isActive == true ? g.message(code: "active") :  review.isActive == false ? g.message(code: "inactive") : g.message(code: "awaiting") }"></span>
                    </td>
                    <td class="actions-column">
                        <span class="action-navigator collapsed${review.isActive == true ? " active" : ""}" entity-id="${review.id}" entity-name="${review.name}"></span>
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="7"><g:message code="no.product.review.found"/> </td>
            </tr>
        </g:else>
    </table>
</div>

<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>