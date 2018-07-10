<div class="multi-column two-column" style="min-width: 900px">
    <div class="columns first-column" >
        <div class="column-content">
            <div class="info-row">
                <label><g:message code="name"/></label>
                <span class="value">${post.name.encodeAsBMHTML()}</span>
            </div>
            <div class="info-row">
                <label><g:message code="categories"/></label>
                <span class="value">${post.categories ? post.categories.collect{return it.name}.join(", ") : g.message(code: "uncategorized")}</span>
            </div>
            <div class="info-row">
                <label><g:message code="date"/></label>
                <span class="value">${post.date.toAdminFormat(false, false, session.timezone)}</span>
            </div>
            <div class="info-row">
                <label><g:message code="status"/></label>
                <span class="value">${post.isPublished ? g.message(code: "published") : g.message(code: "unpublished")}</span>
            </div>
            <div class="info-row">
                <label><g:message code="visibility"/></label>
                <span class="value">${g.message(code: post.visibility)}</span>
            </div>
            <div class="info-row">
                <label><g:message code="created"/></label>
                <span class="value">${post.created.toAdminFormat(true, false, session.timezone)}</span>
            </div>
            <div class="info-row">
                <label><g:message code="updated"/></label>
                <span class="value">${post.updated.toAdminFormat(true, false, session.timezone)}</span>
            </div>
        </div>
    </div><div class="columns last-column">
    <g:if test="${post.image}">
        <g:set var="imgUrl" value="${app.customResourceBaseUrl()}resources/blog-post/post-${post.id}/450-${post.image}"/>
        <div class="column-content">
            <div class="image">
                <img src="${imgUrl}">
            </div>
        </div>
    </g:if>
</div>
    <g:if test="${post.content}">
        <h4 class="group-label"><g:message code="content"/></h4>
        <div class="view-content-block">
            <span class="value">${post.content}</span>
        </div>
    </g:if>
</div>