<div class="info-row">
    <label><g:message code="name"/></label>
    <span class="value">${topic.name.encodeAsBMHTML()}</span>
</div>
<g:if test="${topic.description.size() > 0}">
    <div class="info-row">
        <label><g:message code="description"/></label>
        <span class="value">${topic.description.encodeAsBMHTML()}</span>
    </div>
</g:if>