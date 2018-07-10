<div class="toolbar-share">
    <span class="header-title event"><g:message code="events"/> (${count})</span>
    <div class="toolbar before toolbar-right">
        <div class="tool-group action-header" style="display: none">
            <g:select class="event-action-on-selection" name="action" from="${[g.message(code: "with.selected"), g.message(code: "remove")]}" keys="['', 'remove']"/>
        </div>
        <form class="tool-group search-form">
            <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
        </form>
        <div class="tool-group">
            <span class="toolbar-item add-filter" title="<g:message code="advance.search"/>"><i></i></span>
            <span class="toolbar-item remove-filter disabled" title="<g:message code="remove.search"/>"><i></i></span>
        </div>
        <div class="tool-group toolbar-btn create create-event"><i></i><g:message code="create"/></div>
    </div>
</div>
<div class="event-tab event-table table-view">
    <div class="body">
        <table class="content">
            <colgroup>
                <col class="select-column">
                <col class="name-column">
                <col class="start-time-column">
                <col class="end-time-column">
                <col class="public-column">
                <col class="event-status-column">
                <col class="actions-column">
            </colgroup>
            <tr>
                <th class="select-column"><input class="check-all multiple" type="checkbox"></th>
                <th><g:message code="name"/></th>
                <th><g:message code="start.time"/></th>
                <th><g:message code="end.time"/></th>
                <th><g:message code="public"/></th>
                <th><g:message code="event.status"/></th>
                <th class="actions-column"><g:message code="actions"/></th>
            </tr>
            <g:if test="${events}">
                <g:each in="${events}" var="event" status="i">
                    <tr class="${deleteStatus[i]} ${manageSessionStatus[i]} ${event.venueLocation || event.eventSessions.size() ? 'disable-venue-invitation' : ''}
                    ${event.equipment || event.eventSessions.size() ? 'disable-equipment-invitation' : ''} ${!event.venueLocation || event.eventSessions.size() ? 'disable-manage-ticket' : ''}">
                        <td class="select-column"><input entity-id="${event.id}" type="checkbox" class="multiple"></td>
                        <td>${event.name.encodeAsBMHTML()}</td>
                        <td>${event.startTime?.toAdminFormat(true, false, session.timezone)}</td>
                        <td>${event.endTime?.toAdminFormat(true, false, session.timezone)}</td>
                        <td>${event.isPublic ? g.message(code: "yes") : g.message(code: "no")}</td>
                        <td><g:message code="${status[i]}"/></td>
                        <td class="actions-column">
                            <span class="action-navigator collapsed" entity-id="${event.id}" entity-name="${event.name.encodeAsBMHTML()}" ></span>
                        </td>
                    </tr>
                </g:each>
            </g:if>
            <g:else>
                <tr class="table-no-entry-row">
                    <td colspan="7"><g:message code="no.event.created"/> </td>
                </tr>
            </g:else>
        </table>
    </div>
    <div class="footer">
        <ui:perPageCountSelector/>
        <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
    </div>
</div>