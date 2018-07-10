<div class="multi-column two-column">
    <div class="columns first-column">
        <div class="column-content">
            <div class="info-row">
                <label><g:message code="category.name"/></label>
                <span class="value">${category.name.encodeAsBMHTML()}</span>
            </div>
            <div class="info-row">
                <label><g:message code="sku"/></label>
                <span class="value">${category.sku.encodeAsBMHTML()}</span>
            </div>
            <div class="info-row">
                <label><g:message code="parent.category"/></label>
                <span class="value">${category.parent?.name.encodeAsBMHTML()}</span>
            </div>
            <div class="info-row">
                <label><g:message code="availability"/></label>
                <span class="value"><g:message code="${category.isAvailable ? "available" : "not.available"}"/></span>
            </div>
        </div>
    </div><div class="columns last-column">
        <div class="column-content">
            <img src="${appResource.getCategoryImageURL(category: category, imageSize: "300")}">
        </div>
    </div>
</div>
<g:if test="${category.summary}">
    <h4 class="group-label"><g:message code="summary"/></h4>
    <div class="view-content-block">${category.summary}</div>
</g:if>
<g:if test="${category.description}">
    <div class="info-row">
        <label><g:message code="description"/></label>
    </div>
    <div class="view-content-block description-view-block">${category.description}</div>
</g:if>


