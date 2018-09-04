<g:if test="${comment.name}">
    <div class="info-row">
        <label><g:message code="writer.name"/></label>
        <span class="value">${comment.name.encodeAsBMHTML()}</span>
    </div>
</g:if>
<g:if test="${comment.email}">
    <div class="info-row">
        <label><g:message code="writer.email"/></label>
        <span class="value">${comment.email.encodeAsBMHTML()}</span>
    </div>
</g:if>
<div class="info-row">
    <label><g:message code="status"/></label>
    <span><g:message code="${comment.status}"/></span>
</div>
<g:if test="${comment.isSpam}">
    <div class="info-row">
        <label>&nbsp;</label>
        <span><g:message code="marked.as.spam"/></span>
    </div>
</g:if>
<div class="info-row">
    <label><g:message code="created"/></label>
    <span class="value">${comment.created.toAdminFormat(true, false, session.timezone)}</span>
</div>
<div class="info-row">
    <label><g:message code="updated"/></label>
    <span class="value">${comment.updated.toAdminFormat(true, false, session.timezone)}</span>
</div>
<h4 class="group-label"><g:message code="content"/></h4>
<div class="view-content-block">
    <span class="value">${comment.content.encodeAsBMHTML()}</span>
</div>