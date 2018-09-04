<div class="inner-table invitation-table equipment-invitation-table">
    <h2><g:message code="invitations"/></h2>
    <table>
        <tr>
            <td class="table-header"><g:message code="event"/></td>
            <td class="table-header"><g:message code="event.session"/></td>
            <td class="table-header"><g:message code="start.time"/></td>
            <td class="table-header"><g:message code="end.time"/></td>
            <td class="table-header"><g:message code="status"/></td>
            <td class="table-header actions-column"><g:message code="actions"/></td>
        </tr>
        <g:each in="${equipment.invitation}" var="invitation">
            <tr>
                <g:if test="${invitation.event}">
                    <td>${invitation.event.name.encodeAsBMHTML()}</td>
                    <td></td>
                </g:if>
                <g:else>
                    <td>${invitation.eventSession.event.name.encodeAsBMHTML()}</td>
                    <td>${invitation.eventSession.name.encodeAsBMHTML()}</td>
                </g:else>
                <g:if test="${invitation.event}">
                    <td>${invitation.event.startTime.toAdminFormat(true, false, session.timezone)}</td>
                    <td>${invitation.event.endTime.toAdminFormat(true, false, session.timezone)}</td>
                </g:if>
                <g:elseif  test="${invitation.eventSession}">
                    <td>${invitation.eventSession.startTime.toAdminFormat(true, false, session.timezone)}</td>
                    <td>${invitation.eventSession.endTime.toAdminFormat(true, false, session.timezone)}</td>
                </g:elseif>
                <td><g:message code="${invitation.status}"/></td>
                <td class="actions-column">
                    <span class="action-navigator collapsed" entity-status="${invitation.status}" entity-id="${invitation.id}"></span>
                </td>
            </tr>
        </g:each>
    </table>
</div>


