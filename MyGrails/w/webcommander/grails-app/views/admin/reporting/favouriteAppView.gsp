<div class="header">
    <span class="item-group entity-count header-title">
        <g:message code="favourite.report"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="app-tab-content-container">
    <table class="content">
        <colgroup>
            <col>
            <col class="created-column">
            <col class="actions-column">
        </colgroup>
        <tr>
            <th><g:message code="name"/></th>
            <th><g:message code="created"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:if test="${reports}">
            <g:each in="${reports}" var="report">
                <tr>
                    <td>${report.name.encodeAsBMHTML()}</td>
                    <td>${report.created.toAdminFormat(true, false, session.timezone)}</td>
                    <td class="actions-column">
                        <span class="action-navigator collapsed" entity-id="${report.id}" entity-name="${report.name.encodeAsBMHTML()}" entity-filters='${report.filters}' entity-type='${report.type}'></span>
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="3"><g:message code="no.report.created"/> </td>
            </tr>
        </g:else>
    </table>
</div>
<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset ?: 0}" max="${params.max ?: 10}"></paginator>
</div>