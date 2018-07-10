<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<g:set var="eCommerceConfig" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE)}"/>
<g:set var="fields" bean="configService"/>
<g:each in="${fields.getSortedFields(overViewConfig)}" var="field">
    <g:set var="active" value="${overViewConfig[field + '_active'].toBoolean(null)}"/>
    <g:set var="order" value="${overViewConfig[field + '_order']}"/>
    <g:set var="label" value="${overViewConfig[field + '_label']}"/>
    <g:if test="${onSale && field == 'overview_onsale_product' && overViewConfig.overview_onsale_product_active == 'true'}">
        <div class="special-product">
            <h3 class="title">${label}</h3>
            <g:include view="widget/productListings.gsp" model="[productList: onSale, ecommerceConfig: eCommerceConfig, config: config + [item_per_page: onSale.size()]]"/>
        </div>
    </g:if>
    <g:if test="${featured &&  field == 'overview_featured_product' && overViewConfig.overview_featured_product_active == 'true'}">
        <div class="special-product">
            <h3 class="title">${label}</h3>
            <g:include view="widget/productListings.gsp" model="[productList: featured, ecommerceConfig: eCommerceConfig, config: config + [item_per_page: featured.size()]]"/>
        </div>
    </g:if>
    <g:if test="${newProduct && field == 'overview_new_product' && overViewConfig.overview_new_product_active == 'true'}">
        <div class="special-product">
            <h3 class="title">${label}</h3>
            <g:include view="widget/productListings.gsp" model="[productList: newProduct, ecommerceConfig: eCommerceConfig, config: config + [item_per_page: newProduct.size()]]"/>
        </div>
    </g:if>
    <g:if test="${topSelling && field == 'overview_top_selling_product' && overViewConfig.overview_top_selling_product_active == 'true'}">
        <div class="special-product">
            <h3 class="title">${label}</h3>
            <g:include view="widget/productListings.gsp" model="[productList: topSelling, ecommerceConfig: eCommerceConfig, config: config + [item_per_page: topSelling.size()]]"/>
        </div>
    </g:if>
    <g:if test="${lastViewed && field == 'overview_last_viewed_product' && overViewConfig.overview_last_viewed_product_active == 'true'}">
        <div class="special-product small">
            <h3 class="title">${label}</h3>
            <g:include view="widget/productListings.gsp" model="[productList: lastViewed, ecommerceConfig: eCommerceConfig, config: config + [item_per_page: lastViewed.size(), thumbnail_view: 'true']]"/>
        </div>
    </g:if>
    <g:if test="${lastBought && field == 'overview_last_bought_product' && overViewConfig.overview_last_bought_product_active == 'true'}">
        <div class="special-product small">
            <h3 class="title">${label}</h3>
            <g:include view="widget/productListings.gsp" model="[productList: lastBought, ecommerceConfig: eCommerceConfig, config: config + [item_per_page: lastBought.size(), thumbnail_view: 'true']]"/>
        </div>
    </g:if>
</g:each>