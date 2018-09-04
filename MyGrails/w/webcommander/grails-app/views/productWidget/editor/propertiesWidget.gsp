<g:applyLayout name="_productwidget">
    <g:if test="${config.show_length == "true"}">
        <div class="info-row condition">
            <label><g:message code="length"/>:</label>
            <span class="value">10</span>
            <span class="unit"><g:message code="${unitLength}"/></span>
        </div>
    </g:if>
    <g:if test="${config.show_width == "true"}">
        <div class="info-row condition">
            <label><g:message code="width"/>:</label>
            <span class="value">10</span>
            <span class="unit"><g:message code="${unitLength}"/></span>
        </div>
    </g:if>
    <g:if test="${config.show_height == "true"}">
        <div class="info-row condition">
            <label><g:message code="height"/>:</label>
            <span class="value">10</span>
            <span class="unit"><g:message code="${unitLength}"/></span>
        </div>
    </g:if>
    <g:if test="${config.show_weight == "true"}">
        <div class="info-row condition">
            <label><g:message code="weight"/>:</label>
            <span class="value">10</span>
            <span class="unit"><g:message code="${unitWeight}"/></span>
        </div>
    </g:if>

</g:applyLayout>