<div class="header">
    <span class="header-title"><g:message code="events"/>(${count})</span>
    <div class="toolbar toolbar-right">
        <div class="tool-group action-header" style="display: none">
            <g:select class="action-on-selection" name="action" from="${[g.message(code: "with.selected"), g.message(code: "remove")]}" keys="['', 'remove']"/>
        </div>
        <form class="search-form tool-group">
            <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
        </form>
        <div class="tool-group">
            <span class="toolbar-item add-filter" title="<g:message code="advance.search"/>"><i></i></span>
            <span class="toolbar-item remove-filter disabled" title="<g:message code="remove.search"/>"><i></i></span>
        </div>
        <div class="tool-group toolbar-btn create menu"><i></i><g:message code="create"/></div>
        <div class="tool-group">
            <span class="toolbar-item switch-menu collapsed"><i></i></span>
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="app-tab-content-container event-table">
    <table class="content">
        <colgroup>
            <col class="collapse-controller-column">
            <col class="select-column">
            <col class="name-column">
            <col class="status-column">
            <col class="start-time-column">
            <col class="end-time-column">
            <col class="public-column">
            <col class="total-item-sold-column">
            <col class="event-status-column">
            <col class="actions-column">
        </colgroup>
        <tr>
            <th></th>
            <th class="select-column"><input class="check-all multiple" type="checkbox"></th>
            <th><g:message code="name"/></th>
            <th class="status-column"><g:message code="recurring"/></th>
            <th><g:message code="start.time"/></th>
            <th><g:message code="end.time"/></th>
            <th><g:message code="public"/></th>
            <th><g:message code="event.status"/></th>
            <th><g:message code="sold.ticket"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:if test="${events}">
            <g:each in="${events}" var="event" status="i">
                <tr>
                    <g:set var="recurring" value="${[true: 'positive', false: 'negative']}"/>
                    <g:set var="tooltipNotification" value="${[true: 'Yes', false: 'No']}"/>
                    <td><span class="${event.isRecurring.toBoolean()?'tool-icon collapsed toggle-cell' : ''}" ></span></td>
                    <td class="select-column"><input entity-id="${event.id}" type="checkbox" class="multiple"></td>
                    <td>${event.name.encodeAsBMHTML()}</td>
                    <td><span class="status ${recurring[event.isRecurring.toString()]}" title="${tooltipNotification[event.isRecurring.toString()]}"></span></td>
                    <td>${event.startDateTime?.toAdminFormat(true, false, session.timezone)}</td>
                    <td>${event.endDateTime?.toAdminFormat(true, false, session.timezone)}</td>
                    <td>${event.isPublic ? g.message(code: "yes") : g.message(code: "no")}</td>
                    <td><g:message code="${status[i]}"/></td>
                    <td>${event.totalSoldTicket}</td>
                    <td class="actions-column">
                        <span class="action-navigator collapsed" entity-hasChild=${event.events ? true : false} entity-id="${event.id}" entity-name="${event.name.encodeAsBMHTML()}" ></span>
                    </td>
                </tr>
                <tr class="toggle-table-row" style="display: none">
                    <g:if test="${event.events}">
                        <td colspan="9">
                            <g:include view="/plugins/general_event/admin/event/recurringEventsAppView.gsp" model="[events: event.events]"/>
                        </td>
                    </g:if>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="10"><g:message code="no.event.created"/> </td>
            </tr>
        </g:else>
    </table>
</div>
<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>