<%@ page import="com.webcommander.models.ProductData; com.webcommander.manager.HookManager; com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants" %>
<g:set var="imageSize" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE, "size")[NamedConstants.PRODUCT_IMAGE_SETTINGS.GRIDVIEW]}"/>
<g:set var="loggedCustomerGroupIds" value="${AppUtil.loggedCustomerGroupIds}"/>
<g:each in="${productList}" var="product">
    <%
        config['is_price_restricted'] = product.isPriceRestricted(AppUtil.loggedCustomer, loggedCustomerGroupIds)
        config['is_purchase_restricted'] = config['is_price_restricted'] || product.isPurchaseRestricted(AppUtil.loggedCustomer, loggedCustomerGroupIds)
    %>
    <g:render template="/site/singleProductImageView" model="${[config: config, product: product, imageSize: imageSize]}"/>
</g:each>