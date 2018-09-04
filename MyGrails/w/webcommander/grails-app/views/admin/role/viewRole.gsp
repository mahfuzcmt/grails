<div class="info-row">
    <label><g:message code="role.name"/></label>
    <span class="value">${role.name.encodeAsBMHTML()}</span>
</div>

<div class="info-row">
    <label><g:message code="role.description"/></label>
    <span class="value">${role.description.encodeAsBMHTML()}</span>
</div>

<div class="info-row">
    <label><g:message code="created"/></label>
    <span class="value">${role.created.toAdminFormat(true, false, session.timezone)}</span>
</div>

<div class="info-row">
    <label><g:message code="updated"/></label>
    <span class="value">${role.updated.toAdminFormat(true, false, session.timezone)}</span>
</div>