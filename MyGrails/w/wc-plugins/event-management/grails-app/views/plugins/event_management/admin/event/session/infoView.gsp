<div class="info-row">
    <label><g:message code="name"/></label>
    <span class="value">${eventSession.name.encodeAsBMHTML()}</span>
</div>
<div class="info-row">
    <label><g:message code="start.time"/></label>
    <span class="value">${eventSession.startTime?.toAdminFormat(true, true, session.timezone)}</span>
</div>
<div class="info-row">
    <label><g:message code="end.time"/></label>
    <span class="value">${eventSession.endTime?.toAdminFormat(true, true, session.timezone)}</span>
</div>
<g:if test="${eventSession.description}">
    <div class="info-row">
        <label><g:message code="description"/></label>
        <span class="value">${eventSession.description}</span>
    </div>
</g:if>