<g:if test="${product.brand}">
    <g:applyLayout name="_productwidget">
        <div class="info-row brand">
            <label><g:message code="brand"/>:</label>
            <a href="${app.relativeBaseUrl() + 'brand/' + product.brand.url}">
                <span class="name">${product.brand.name.encodeAsBMHTML()}</span>
            </a>
        </div>
    </g:applyLayout>
</g:if>
