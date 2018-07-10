<div class="header">
    <span class="item-group entity-count title">
        <g:message code="blog.categories"/> (<span class="count">${count}</span>)
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
            <span class="toolbar-item switch-menu collapsed"><i></i></span>
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="app-tab-content-container">
    <table class="content">
        <colgroup>
            <col style="width: 3%">
            <col style="width: 30%">
            <col style="width: 10%">
            <col style="width: 25%">
            <col style="width: 25%">
            <col style="width: 7%">
        </colgroup>
        <tr>
            <th class="select-column"><input class="check-all multiple" type="checkbox"></th>
            <th><g:message code="name"/></th>
            <th><g:message code="no.of.posts"/></th>
            <th><g:message code="created"/></th>
            <th><g:message code="updated"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:if test="${categories}">
            <g:each in="${categories}" var="category">
                <tr>
                    <td class="select-column"><input entity-id="${category.id}" type="checkbox" class="multiple"></td>
                    <td>${category.name.encodeAsBMHTML()}</td>
                    <td>${category.posts.size()}</td>
                    <td>${category.created.toAdminFormat(true, false, session.timezone)}</td>
                    <td>${category.updated.toAdminFormat(true, false, session.timezone)}</td>
                    <td class="actions-column">
                        <span class="action-navigator collapsed" entity-id="${category.id}" entity-url=${category.url.encodeAsBMHTML()} entity-name="${category.name.encodeAsBMHTML()}"></span>
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="6"><g:message code="no.blog.category.created"/></td>
            </tr>
        </g:else>
    </table>
</div>
<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>