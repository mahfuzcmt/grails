<div class="header">
    <span class="item-group entity-count title">
        <g:message code="event.session"/> (<span class="count">${eventSessions.size()}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group action-header" style="display: none">
            <g:select class="action-on-selection" name="action" from="${[g.message(code: "with.selected"), g.message(code: "remove")]}" keys="['', 'remove']"/>
        </div>
        <form class="search-form tool-group">
            <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
        </form>
        <div class="tool-group toolbar-btn create"><i></i><g:message code="create"/></div>
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>

<div class="app-tab-content-container body">
    <table class="content">
        <colgroup>
            <col class="select-column">
            <col class="name-column">
            <col class="start-time-column">
            <col class="end-time-column">
            <col class="actions-column">
        </colgroup>
        <tr>
            <th class="select-column"><input class="check-all multiple" type="checkbox"/></th>
            <th><g:message code="name"/></th>
            <th><g:message code="start.time"/></th>
            <th><g:message code="end.time"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:each in="${eventSessions}" var="eventSession" status="i">
            <tr class="${deleteStatus[i]} ${eventSessions[i].venueLocation ? 'disable-venue-invitation' : ''} ${eventSessions[i].equipment ? 'disable-equipment-invitation' : ''}
                ${!eventSessions[i].venueLocation ? 'disable-manage-ticket' : ''}">
                <td class="select-column"><input entity-id="${eventSession.id}" type="checkbox" class="multiple"></td>
                <td>${eventSession.name.encodeAsBMHTML()}</td>
                <td>${eventSession.startTime.toAdminFormat(true, false, session.timezone)}</td>
                <td>${eventSession.endTime.toAdminFormat(true, false, session.timezone)}</td>
                <td class="actions-column">
                    <span class="action-navigator collapsed" entity-id="${eventSession.id}" entity-name="${eventSession.name.encodeAsBMHTML()}"></span>
                </td>
            </tr>
        </g:each>
    </table>
</div>
<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>