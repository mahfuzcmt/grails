<%@ page import="com.webcommander.conversion.MassConversions; com.webcommander.conversion.LengthConversions" %>
<g:applyLayout name="_productwidget">
    <g:if test="${config.show_length == "true"}">
        <div class="info-row condition">
            <label><g:message code="length"/>:</label>
            <span class="value">${LengthConversions.convertSIToLength(unitLength, productData.length).toLength()}</span>
            <span class="unit"><g:message code="${unitLength}"/></span>
        </div>
    </g:if>
    <g:if test="${config.show_width == "true"}">
        <div class="info-row condition">
            <label><g:message code="width"/>:</label>
            <span class="value">${LengthConversions.convertSIToLength(unitLength, productData.width).toLength()}</span>
            <span class="unit"><g:message code="${unitLength}"/></span>
        </div>
    </g:if>
    <g:if test="${config.show_height == "true"}">
        <div class="info-row condition">
            <label><g:message code="height"/>:</label>
            <span class="value">${ LengthConversions.convertSIToLength(unitLength, productData.height).toLength()}</span>
            <span class="unit"><g:message code="${unitLength}"/></span>
        </div>
    </g:if>
    <g:if test="${config.show_weight == "true"}">
        <div class="info-row condition">
            <label><g:message code="weight"/>:</label>
            <span class="value">${MassConversions.convertSIToMass(unitWeight, productData.weight).toWeight()}</span>
            <span class="unit"><g:message code="${unitWeight}"/></span>
        </div>
    </g:if>

</g:applyLayout>