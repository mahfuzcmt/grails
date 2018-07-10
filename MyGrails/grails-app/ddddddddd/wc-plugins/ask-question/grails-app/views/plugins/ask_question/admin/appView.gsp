<div class="header">
    <span class="item-group entity-count title">
        <g:message code="product.questions"/> (<span class="count">${count}</span>)
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
            <col class="select-column">
            <col class="product-cloumn">
            <col class="asked-by-column">
            <col class="date-column">
            <col class="status-column">
            <col class="actions-column">
        </colgroup>
        <tr>
            <th class="select-column"><input class="check-all multiple" type="checkbox"></th>
            <th><g:message code="product"/> </th>
            <th><g:message code="asked.by"/></th>
            <th><g:message code="date"/></th>
            <th class="status-column"><g:message code="status"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:if test="${questions}">
            <g:each in="${questions}" var="question">
                <tr>
                    <td class="select-column"><input entity-id="${question.id}" class="multiple" type="checkbox"></td>
                    <td>${question.product.name.encodeAsBMHTML()}</td>
                    <td>${question.name.encodeAsBMHTML()}</td>
                    <td>${question.created.toAdminFormat(true, false, session.timezone)}</td>
                    <td class="status-column"><span class="status ${ question.status ? "positive" : "negative" }" title="${question.status ? g.message(code: "replied") : g.message(code: "reply.pending") }">
                        </span></td>
                    <td class="actions-column">
                        <span class="action-navigator collapsed" entity-id="${question.id}" entity-name="<g:message code="question"/>"></span>
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="6"><g:message code="no.product.question.found"/> </td>
            </tr>
        </g:else>
    </table>
</div>

<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>