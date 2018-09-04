<div class="inner-table recurring-event-table">
    <h2><g:message code="recurring.events"/>(${events.size()})</h2>
    <table>
        <g:each in="${events}" var="event">
            <tr>
                <td>${event.start.toAdminFormat(true, false, session.timezone)}</td>
                <td>${event.end.toAdminFormat(true, false, session.timezone)}</td>
                <td><g:message code="${event.start < java.util.Calendar.getInstance().getTime() ? 'complete' : 'incomplete'}"/></td>
                <td>${event.totalSoldTicket}</td>
                <td class="actions-column">
                    <span class="action-navigator collapsed" entity-start="${event.start}" entity-end="${event.end}" entity-isrecurring="${true}" entity-id="${event.id}"></span>
                </td>
            </tr>
        </g:each>
    </table>
</div>