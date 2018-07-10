<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants" %>
<g:set var="e_commerceConfig" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE)}"/>
<g:applyLayout name="_productwidget">
    <g:if test="${productData.isAvailable}">
        <g:if test="${productData.isInventoryEnabled}">
            <g:if test="${productData.availableStock && productData.availableStock > productData.lowStockLevel}">
                <span class="available stock-mark"><site:message code="${e_commerceConfig.available_stock_message}"/></span>
            </g:if>
            <g:elseif test="${productData.availableStock > 0}">
                <span class="low-stock stock-mark"><site:message code="${e_commerceConfig.low_stock_message}"/></span>
            </g:elseif>
            <g:else>
                <span class="out-of-stock stock-mark"><site:message code="${e_commerceConfig.out_stock_message}"/></span>
            </g:else>
        </g:if>
        <g:else>
            <span class="available stock-mark"><site:message code="${e_commerceConfig.available_stock_message}"/></span>
        </g:else>
    </g:if>
    <g:else>
        <span class="product-status message-block error"><g:message code="product.not.available"/></span>
    </g:else>
</g:applyLayout>