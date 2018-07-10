<%@ page import="com.webcommander.util.AppUtil" %>
<g:applyLayout name="_widget">
    <div class="form-row">
        <g:if test="${config.label}">
            <label><site:message code="${config.label}"/> :</label>
        </g:if>
        <ui:domainSelect name="currency" class="medium" domain="${com.webcommander.webcommerce.Currency}" value="${session.currency?.id ?: AppUtil.baseCurrency?.id}"
                         text="${config.displayOption}" filter="${{ eq("active", true) }}"/>
    </div>
</g:applyLayout>