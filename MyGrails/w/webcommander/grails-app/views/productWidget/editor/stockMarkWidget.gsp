<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<g:applyLayout name="_productwidget">
    <g:set var="e_commerceConfig" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE)}"/>
    <span class="available stock-mark"><site:message code="${e_commerceConfig.available_stock_message}"/></span>
</g:applyLayout>