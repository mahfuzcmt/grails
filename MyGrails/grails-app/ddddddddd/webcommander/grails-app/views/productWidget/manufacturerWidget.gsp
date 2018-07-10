<g:if test="${product.manufacturer}">
    <g:applyLayout name="_productwidget">
        <div class="info-row manufacturer">
            <label><g:message code="manufacturer"/>:</label>
            <a href="${app.relativeBaseUrl() + 'manufacturer/' + product.manufacturer.url}">
                <span class="name">${product.manufacturer.name.encodeAsBMHTML()}</span>
            </a>
        </div>
    </g:applyLayout>
</g:if>
