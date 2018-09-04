<div class="info-row">
    <label><g:message code="name"/></label>
    <span class="value">${article.name.encodeAsBMHTML()}</span>
</div>
<div class="info-row">
    <label><g:message code="visibility"/></label>
    <span class="value"><g:message code="${article.isPublished ? "published" : "unpublished"}"/></span>
</div>
<div class="info-row">
    <label><g:message code="section"/></label>
    <span class="value">${article.section?.name.encodeAsBMHTML()}</span>
</div>
<div class="info-row">
    <label><g:message code="summary"/></label>
</div>
<div class="view-content-block">${article.summary.encodeAsBMHTML()}</div>
<div class="info-row">
    <label><g:message code="content"/></label>
</div>
<div class="view-content-block">${article.content}</div>