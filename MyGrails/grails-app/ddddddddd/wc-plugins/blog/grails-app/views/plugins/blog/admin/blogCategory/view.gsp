<div class="multi-column two-column">
    <div class="columns first-column" >
        <div class="column-content">
            <div class="info-row">
                <label><g:message code="name"/></label>
                <span class="value">${category.name.encodeAsBMHTML()}</span>
            </div>
            <div class="info-row">
                <label><g:message code="created"/></label>
                <span class="value">${category.created.toAdminFormat(true, false, session.timezone)}</span>
            </div>
            <div class="info-row">
                <label><g:message code="updated"/></label>
                <span class="value">${category.updated.toAdminFormat(true, false, session.timezone)}</span>
            </div>
        </div>
    </div><div class="columns last-column">
    <g:if test="${category.image}">
        <g:set var="imgUrl" value="${appResource.getBlogCategoryImageUrl(image: category, sizeOrPrefix: "450")}"/>
        <div class="column-content">
            <div class="image">
                <img src="${imgUrl}">
            </div>
        </div>
    </g:if>
</div>
    <g:if test="${category.description}">
        <h4 class="group-label"><g:message code="description"/></h4>
        <div class="view-content-block">
            <span class="value">${category.description}</span>
        </div>
    </g:if>
</div>