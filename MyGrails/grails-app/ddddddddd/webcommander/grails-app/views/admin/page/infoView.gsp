<div class="info-row">
    <label><g:message code="page.name"/></label>
    <span class="value">${page.name.encodeAsBMHTML()}</span>
</div>
<div class="info-row">
    <label><g:message code="page.title"/></label>
    <span class="value">${page.title.encodeAsBMHTML()}</span>
</div>
<div class="info-row">
    <label><g:message code="url.identifier"/></label>
    <span class="value">${page.url.encodeAsBMHTML()}</span>
</div>
<div class="info-row">
    <label><g:message code="created.by"/></label>
    <span class="value">${page.createdBy?.fullName?.encodeAsBMHTML()}</span>
</div>
<div class="info-row">
    <label><g:message code="created"/></label>
    <span class="value">${page.created.toAdminFormat(true, false, session.timezone)}</span>
</div>
<div class="info-row">
    <label><g:message code="updated"/></label>
    <span class="value">${page.updated.toAdminFormat(true, false, session.timezone)}</span>
</div>
<g:if test="${page.layout}">
    <div class="info-row">
        <label><g:message code="attached.layout"/></label>
        <span class="value">${page.layout.name}</span>
    </div>
</g:if>