<g:if test="${product.parent}">
    <g:applyLayout name="_productwidget">
        <div class="info-row category">
            <label><g:message code="category"/>:</label>
            <span class="value">${product.parent.name.encodeAsBMHTML()}</span>
        </div>
    </g:applyLayout>
</g:if>
