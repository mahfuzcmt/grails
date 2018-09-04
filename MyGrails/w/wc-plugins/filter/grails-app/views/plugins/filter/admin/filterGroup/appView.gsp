<div class="header">
    <span class="item-group entity-count title">
        <g:message code="filter.groups"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group action-header" style="display: none">
            <g:select class="action-on-selection" name="action" from="${[ g.message(code: "with.selected"), g.message(code: "remove")]}" keys="['', 'remove']"/>
        </div>
        <form class="search-form tool-group">
            <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
        </form>
        <div class="tool-group">
            <span class="toolbar-item remove-filter disabled" title="<g:message code="remove.search"/>"><i></i></span>
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
            <col style="width: 3%">
            <col style="width: 20%">
            <col style="width: 40%">
            <col style="width: 17%">
            <col style="width: 10%">
            <col style="width: 10%">
        </colgroup>
        <tr>
            <th class="select-column"><input class="check-all multiple" type="checkbox"></th>
            <th><g:message code="name"/></th>
            <th><g:message code="filter.group.items"/></th>
            <th><g:message code="created"/></th>
            <th class="status-column"><g:message code="status"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:if test="${filterGroups}">
            <g:each in="${filterGroups}" var="group">
                <tr>
                    <td class="select-column"><input entity-id="${group.id}" type="checkbox" class="multiple"></td>
                    <td>${group.name.encodeAsBMHTML()}</td>
                    <td class="navigation-item-column" items="${group.items.size()}">
                        <g:if test="${group.items.size() > 0 }">
                            <span class="navigation-item-count">${group.items.size()} <g:message code="items"/>:</span>
                            <span class="navigation-items">
                                <g:join in="${group.items.size() <= 3 ? group.items.heading.collect { it.encodeAsBMHTML() } : group.items.subList(0, 3).heading.collect {it.encodeAsBMHTML()} }" delimiter=", " />
                                <g:if test="${group.items.size() > 3}"> ... </g:if>
                            </span><span class="item-action" type="edit" entity-id="${group.id}" entity-name="${group.name.encodeAsBMHTML()}"><span class="tool-icon edit"></span><span class="label fake-link"><g:message code="edit.items"/></span></span>
                        </g:if>
                        <g:else>
                            <span class="navigation-item-count-no-item"><g:message code="no.filter.group.item.available"/></span><span class="item-action" type="add" entity-id="${group.id}" entity-name="${group.name.encodeAsBMHTML()}"><span class="tool-icon add"></span><span class="label fake-link"><g:message code="add.items"/></span></span>
                        </g:else>
                    </td>
                    <td>${group.created.toAdminFormat(true, false, session.timezone)}</td>
                    <td class="status-column order-status">
                        <span class="status ${group.isActive ? 'positive' : 'negative'}" title="${g.message(code: 'filter.group.status' )}"></span>
                    </td>
                    <td class="actions-column">
                        <span class="action-navigator collapsed" entity-id="${group.id}" entity-name="${group.name.encodeAsBMHTML()}"></span>
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="6"><g:message code="no.filter.group.created"/></td>
            </tr>
        </g:else>
    </table>
</div>
<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>