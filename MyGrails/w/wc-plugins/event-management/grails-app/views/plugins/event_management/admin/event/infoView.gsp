<div class="info-row">
    <label><g:message code="name"/></label>
    <span class="value">${event.name.encodeAsBMHTML()}</span>
</div>
<div class="info-row">
    <label><g:message code="event.organiser"/></label>
    <span class="value">${event.organiser.fullName.encodeAsBMHTML()}</span>
</div>
<div class="info-row">
    <label><g:message code="tax.profile"/></label>
    <span class="value">${event.taxProfile?.name.encodeAsBMHTML()}</span>
</div>
<div class="info-row">
    <label><g:message code="start.time"/></label>
    <g:if test="${event.eventSessions.size() > 0}">
        <span class="value">${event.eventSessions.startTime.min()?.toAdminFormat(true, false, session.timezone)}</span>
    </g:if>
    <g:else>
        <span class="value">${event.startTime?.toAdminFormat(true, false, session.timezone)}</span>
    </g:else>
</div>
<div class="info-row">
    <label><g:message code="end.time"/></label>
    <g:if test="${event.eventSessions.size() > 0}">
        <span class="value">${event.eventSessions.endTime.max()?.toAdminFormat(true, false, session.timezone)}</span>
    </g:if>
    <g:else>
        <span class="value">${event.endTime?.toAdminFormat(true, false, session.timezone)}</span>
    </g:else>
</div>
<g:if test="${event.eventSessions.size() > 0 }">
    <div class="info-row">
        <label><g:message code="event.sessions"/></label>
    </div>
    <div class="view-content-block description-view-block">
        <g:each in="${event.eventSessions}" var="eSession">
            <div class="info-row"><span class="value">${eSession.name.encodeAsBMHTML()}</span></div>
        </g:each>
    </div>
</g:if>
<g:else>
    <div class="info-row">
        <label><g:message code="venue.location"/></label>
        <span class="value">${event.venueLocation?.name.encodeAsBMHTML()}</span>
    </div>
</g:else>
<div class="info-row">
    <label><g:message code="event.summary"/></label>
</div>
<div class="view-content-block description-view-block">
    ${event.summary.encodeAsBMHTML()}
</div>
<div class="info-row">
    <label><g:message code="event.description"/></label>
</div>
<div class="view-content-block description-view-block">
    ${event.description}
</div>
