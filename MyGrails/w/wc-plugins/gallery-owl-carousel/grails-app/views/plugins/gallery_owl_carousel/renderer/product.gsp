<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil; com.webcommander.constants.NamedConstants" %>
<g:set var="imageSize" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE, "size")[NamedConstants.PRODUCT_IMAGE_SETTINGS.GRIDVIEW]}"/>
<g:set var="loggedCustomerGroupIds" value="${AppUtil.loggedCustomerGroupIds}"/>
<div id="owl-carousel-${widget.uuid}" class="owl-carousel product-view image-view">
    <%
        Map productConfig = [
            label_for_expect_to_pay: "true",
            add_to_cart: "true",
            is_rating_active: "true",
            label_for_call_for_price: "s:call.for.price"
        ]

    %>
    <g:each in="${items}" var="product">
        <%
            productConfig['is_price_restricted'] = product.isPriceRestricted(AppUtil.loggedCustomer, loggedCustomerGroupIds)
            productConfig['is_purchase_restricted'] = config['is_price_restricted'] || product.isPurchaseRestricted(AppUtil.loggedCustomer, loggedCustomerGroupIds)
        %>
        <div class="item">
            <g:render template="/site/singleProductImageView" model="${[product: product, imageSize: imageSize, config: productConfig]}"/>
        </div>
    </g:each>
</div>