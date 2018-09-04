<div class="info-row">
    <label><g:message code="full.name"/></label>
    <span class="value">${user.fullName.encodeAsBMHTML()}</span>
</div>
<div class="info-row">
    <label><g:message code="email"/></label>
    <span class="value">${user.email.encodeAsBMHTML()}</span>
</div>
<div class="info-row">
    <label><g:message code="status"/></label>
    <span class="value">${user.isActive ? g.message(code: 'active') : g.message(code: 'inactive')}</span>
</div>
<div class="info-row">
    <label><g:message code="created"/></label>
    <span class="value">${user.created.toAdminFormat(true, false, session.timezone)}</span>
</div>
<div class="info-row">
    <label><g:message code="updated"/></label>
    <span class="value">${user.updated.toAdminFormat(true, false, session.timezone)}</span>
</div>