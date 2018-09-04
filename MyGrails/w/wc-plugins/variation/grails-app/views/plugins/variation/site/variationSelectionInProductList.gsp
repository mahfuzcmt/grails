<%@ page import="com.webcommander.webcommerce.Product; com.webcommander.plugin.variation.VariationOption; com.webcommander.plugin.variation.VariationType; com.webcommander.plugin.variation.ProductVariation; com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<g:set var="config" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT)}"/>
<g:set var="variations" value="${ProductVariation.findAllByProduct(productVariation)}"/>
<div class="product-widget widget-variation-product-list" widget-type="variation">
        <g:if test="${variations}">
            <h3>${g.message(code: "variation.combination")}</h3>
            <g:include view="/plugins/variation/site/variationContainer.gsp" model="[productData: productDataVariation, product: productVariation, variations: variations, config: config]"/>
        </g:if>
</div>