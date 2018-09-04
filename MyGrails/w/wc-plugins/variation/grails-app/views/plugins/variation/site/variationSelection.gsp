<%@ page import="com.webcommander.plugin.variation.VariationOption; com.webcommander.plugin.variation.VariationType; com.webcommander.plugin.variation.ProductVariation; com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<g:set var="config" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT)}"/>
<g:set var="variations" value="${ProductVariation.findAllByProduct(product)}"/>
<g:if test="${variations}">
    <g:include view="/plugins/variation/site/variationContainer.gsp" model="[productData: productData, product: product, variations: variations, config: config]"/>
</g:if>