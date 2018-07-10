<div class="info-row">
    <label><g:message code="news.title"/></label>
    <span class="value">${news.title.encodeAsBMHTML()}</span>
</div>
<div class="info-row">
    <label><g:message code="news.date"/></label>
    <span class="value">${news.newsDate.toAdminFormat(false, false, session.timezone)}</span>
</div>
<g:if test="${news.article}">
    <div class="info-row">
        <label><g:message code="article.name"/></label>
        <span class="value">${news.article.name.encodeAsBMHTML()}</span>
    </div>
</g:if>
<g:if test="${news.summary}">
    <div class="info-row">
        <label><g:message code="summary"/></label>
    </div>
    <div class="view-content-block description-view-block">
        ${news.summary.encodeAsBMHTML()}
    </div>
</g:if>
