<div class="multi-column two-column">
    <div class="columns first-column">
        <div class="column-content">
            <div class="info-row">
                <label><g:message code="name"/></label>
                <span class="value">${product.name.encodeAsBMHTML()}</span>
            </div>
            <div class="info-row">
                <label><g:message code="sku"/></label>
                <span class="value">${product.sku.encodeAsBMHTML()}</span>
            </div>
            <div class="info-row">
                <label><g:message code="parent"/></label>
                <span class="value">
                    <g:if test="${product.parent}">${product.parent.name.encodeAsBMHTML()}</g:if>
                    <g:else><g:message code="root"/></g:else>
                </span>
            </div>
            <div class="info-row">
                <label><g:message code="price"/></label>
                <span class="value">${product.basePrice.toAdminPrice()}</span>
            </div>
            <div class="info-row">
                <label><g:message code="cost.price"/></label>
                <span class="value">${product.costPrice.toAdminPrice()}</span>
            </div>
            <div class="info-row">
                <label><g:message code="available.stock"/></label>
                <span class="value">${product.availableStock}</span>
            </div>
            <div class="info-row">
                <label><g:message code="tax.profile"/></label>
                <span class="value">${product.taxProfile?.name.encodeAsBMHTML()}</span>
            </div>
            <div class="info-row">
                <label><g:message code="shipping.profile"/></label>
                <span class="value">${product.shippingProfile?.name.encodeAsBMHTML()}</span>
            </div>
        </div>
    </div><div class="columns last-column">
        <g:if test="${product.images?.size() > 0}">
            <g:set var="imgUrl" value="${app.customResourceBaseUrl()}resources/product/product-${product.id}/300-${product.images[0].name}"/>
         </g:if>
        <g:else>
            <g:set var="imgUrl" value="${app.customResourceBaseUrl() + "resources/product/default/300-default.png"}"/>
         </g:else>
        <div class="column-content">
            <img src="${imgUrl}">
        </div>
    </div>
</div>
<g:if test="${product.summary}">
    <h4 class="group-label"><g:message code="summary"/></h4>
    <div class="view-content-block">${product.summary}</div>
</g:if>
<g:if test="${product.description}">
    <h4 class="group-label"><g:message code="description"/></h4>
    <div class="view-content-block description-view-block">${product.description}</div>
</g:if>
