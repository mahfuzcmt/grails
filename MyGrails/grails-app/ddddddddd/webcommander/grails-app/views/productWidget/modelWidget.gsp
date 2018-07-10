<g:if test="${productData.model}">
    <g:applyLayout name="_productwidget">
        <div class="info-row model">
            <label><g:message code="model"/>:</label>
            <span class="value">${productData.model.encodeAsBMHTML()}</span>
        </div>
    </g:applyLayout>
</g:if>
