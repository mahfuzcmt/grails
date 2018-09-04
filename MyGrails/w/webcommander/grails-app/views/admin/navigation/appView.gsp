<div class="header">
    <span class="item-group entity-count title">
        <g:message code="navigations"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group action-header" style="display: none">
            <g:select class="action-on-selection" name="action" from="${[ g.message(code: "with.selected"), g.message(code: "remove"), g.message(code: "restricted.item")]}" keys="['', 'remove', 'restricted']"/>
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
            <col style="width: 5%">
            <col style="width: 20%">
            <col style="width: 36%">
            <col style="width: 17%">
            <col style="width: 17%">
            <col style="width: 5%">
        </colgroup>
        <tr>
            <th class="select-column"><input class="check-all multiple" type="checkbox"></th>
            <th><g:message code="name"/></th>
            <th><g:message code="navigation.items"/></th>
            <th><g:message code="created"/></th>
            <th><g:message code="updated"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:if test="${navigations}">
        <g:each in="${navigations}" var="navigation">
            <tr>
                <td class="select-column"><input entity-id="${navigation.id}" class="multiple" type="checkbox"></td>
                <td>${navigation.name.encodeAsBMHTML()}</td>
                <td class="navigation-item-column" items="${navigation.items.size()}">
                    <g:if test="${navigation.items.size() > 0 }">
                        <span class="navigation-item-count">${navigation.items.size()} <g:message code="items"/></span>
                        <span class="navigation-items">
                            <g:join in="${navigation.items.size() <= 3 ? navigation.items.label.collect { it.encodeAsBMHTML() } : navigation.items.subList(0, 3).label.collect {it.encodeAsBMHTML()} }" delimiter=", " />
                            <g:if test="${navigation.items.size() > 3}"> ... </g:if>
                        </span><span class="item-action" type="edit" entity-id="${navigation.id}" entity-name="${navigation.name.encodeAsBMHTML()}"><span class="tool-icon edit"></span><span class="label fake-link"><g:message code="edit.items"/></span></span>
                    </g:if>
                    <g:else>
                        <span class="navigation-item-count-no-item"><g:message code="no.navigation.item.available"/></span><span class="item-action" type="add" entity-id="${navigation.id}" entity-name="${navigation.name.encodeAsBMHTML()}"><span class="tool-icon add"></span><span class="label fake-link"><g:message code="add.items"/></span></span>
                    </g:else>
                </td>
                <td>${navigation.created.toAdminFormat(true, false, session.timezone)}</td>
                <td >${navigation.updated.toAdminFormat(true, false, session.timezone)}</td>
                <td class="actions-column">
                    <span class="action-navigator collapsed" entity-id="${navigation.id}" entity-name="${navigation.name.encodeAsBMHTML()}" is-default="${navigation.name == "Main Menu"}"></span>
                </td>
            </tr>
        </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="6"><g:message code="no.navigation.created"/> </td>
            </tr>
        </g:else>
    </table>
</div>
<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>