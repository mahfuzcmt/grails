<div class="inner-table invitation-table venue-location-invitation-table">
    <h2><g:message code="invitations"/></h2>
    <table>
        <tr>
            <td class="table-header"><g:message code="event"/></td>
            <td class="table-header"><g:message code="event.session"/></td>
            <td class="table-header"><g:message code="start.time"/></td>
            <td class="table-header"><g:message code="end.time"/></td>
            <td class="table-header"><g:message code="status"/></td>
            <td class=" table-header actions-column"><g:message code="actions"/></td>
        </tr>
        <g:if test="${location.venueLocationInvitation}">
            <g:each in="${location.venueLocationInvitation}" var="invitation">
                <tr>
                    <g:if test="${invitation.event}">
                        <td>${invitation.event.name.encodeAsBMHTML()}</td>
                        <td></td>
                    </g:if>
                    <g:else>
                        <td>${invitation.eventSession.event.name.encodeAsBMHTML()}</td>
                        <td>${invitation.eventSession.name.encodeAsBMHTML()}</td>
                    </g:else>
                    <td>${invitation.event ? invitation.event.startTime?.toAdminFormat(true, false, session.timezone) : invitation.eventSession.startTime?.toAdminFormat(true, false, session.timezone)}</td>
                    <td>${invitation.event ? invitation.event.endTime?.toAdminFormat(true, false, session.timezone) : invitation.eventSession.endTime?.toAdminFormat(true, false, session.timezone)}</td>
                    <td><g:message code="${invitation.status}"/></td>
                    <td class="actions-column">
                        <span class="action-navigator collapsed" entity-status="${invitation.status}" entity-id="${invitation.id}"></span>
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="6"><g:message code="no.invitation.found"/> </td>
            </tr>
        </g:else>

    </table>
</div>


