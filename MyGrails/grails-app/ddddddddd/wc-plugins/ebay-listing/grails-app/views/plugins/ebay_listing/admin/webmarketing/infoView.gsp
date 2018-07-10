<div class="info-row">
    <label><g:message code="profile.name"/></label>
    <span class="value">${profile.name.encodeAsBMHTML()}</span>
</div>
<g:if test="${profile.note}">
    <div class="info-row">
        <label><g:message code="note"/></label>
        <span class="value">${profile.note.encodeAsBMHTML()}</span>
    </div>
</g:if>
<div class="info-row">
    <label><g:message code="number.of.categories"/></label>
    <span class="value">${profile.categories.size()}</span>
</div>
<div class="info-row">
    <label><g:message code="number.of.products"/></label>
    <span class="value">${profile.products.size()}</span>
</div>