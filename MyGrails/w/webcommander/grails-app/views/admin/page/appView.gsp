<div class="header">
    <span class="item-group entity-count title">
        <g:message code="pages"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group action-header" style="display: none">
            <g:select class="action-on-selection" name="action" from="${[g.message(code: "with.selected"), g.message(code: "administrative.status"), g.message(code: "visibility"), g.message(code: "copy"), g.message(code: "remove")]}"
                      keys="['', 'administrative_status', 'visibility', 'copy', 'remove']"/>
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
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>

<div class="app-tab-content-container">
    <table class="content">
        <colgroup>
            <col class="collapse-controller-column">
            <col class="select-column">
            <col class="status-column">
            <col class="name-column">
            <col class="title-column">
            <col class="layout-column">
            <col class="created-column">
            <col class="updated-column">
            <col class="created-by-column">
            <col class="actions-column">
        </colgroup>
        <tr>
            <th></th>
            <th class="select-column"><input class="check-all multiple" type="checkbox"></th>
            <th class="status-column"><g:message code="visibility"/></th>
            <th><g:message code="page.name"/></th>
            <th><g:message code="page.title"/></th>
            <th><g:message code="page.layout"/></th>
            <th><g:message code="created"/></th>
            <th><g:message code="updated"/></th>
            <th><g:message code="created.by"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:if test="${pages}">
            <g:each in="${pages}" var="page">
                <tr class="${page.url == landingPage ? "landing highlighted" : ""}">
                    <td><span class="${childPages[page.id.toString()]? "tool-icon collapsed toggle-cell" : ""}"></span></td>
                    <g:set var="status" value="${[open: 'positive', hidden: 'diplomatic', restricted: 'negative']}"/>
                    <g:set var="tooltipNotification" value="${[open: 'Open', hidden: 'Hidden', restricted: 'Restricted']}"/>
                    <td class="select-column"><input entity-id="${page.id}" type="checkbox" class="multiple"></td>
                    <td><span class="status ${status[page.visibility]}" title="${tooltipNotification[page.visibility]}"></span></td>
                    <td>${page.name.encodeAsBMHTML()}</td>
                    <td>${page.title.encodeAsBMHTML()}</td>
                    <td>${page.layout?.name?.encodeAsBMHTML()}</td>
                    <td>${page.created.toAdminFormat(true, false, session.timezone)}</td>
                    <td >${page.updated.toAdminFormat(true, false, session.timezone)}</td>
                    <td>${page.createdBy?.fullName?.encodeAsBMHTML()}</td>
                    <td class="actions-column">
                        <span class="action-navigator collapsed" entity-id="${page.id}" entity-url="${page.url.encodeAsBMHTML()}" entity-name="${page.name.encodeAsBMHTML()}" entity-owner_id="${page.createdBy?.id}"></span>
                    </td>
                </tr>
                <g:if test="${childPages[page.id.toString()]}">
                    <tr class="toggle-table-row" style="display: none">
                        <td colspan="9">
                            <g:include view="/admin/page/childPageAppView.gsp" model="[childPages: childPages[page.id.toString()]]"/>
                        </td>
                    </tr>
                </g:if>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="9"><g:message code="no.page.created"/> </td>
            </tr>
        </g:else>
    </table>
</div>

<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>