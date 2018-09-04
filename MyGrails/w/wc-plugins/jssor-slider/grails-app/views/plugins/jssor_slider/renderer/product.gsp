<%@ page import="com.webcommander.constants.NamedConstants; com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants;" %>
<g:set var="imageSize" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE, "size")[NamedConstants.PRODUCT_IMAGE_SETTINGS.GRIDVIEW]}"/>
<g:set var="loggedCustomerGroupIds" value="${AppUtil.loggedCustomerGroupIds}"/>
<%
    Map productConfig = [
            label_for_expect_to_pay: "true",
            add_to_cart: "true",
            is_rating_active: "true",
            label_for_call_for_price: "s:call.for.price"
    ]

%>
<g:each in="${items}" status="i" var="product">
    <%
        productConfig['is_price_restricted'] = product.isPriceRestricted(AppUtil.loggedCustomer, loggedCustomerGroupIds)
        productConfig['is_purchase_restricted'] = config['is_price_restricted'] || product.isPurchaseRestricted(AppUtil.loggedCustomer, loggedCustomerGroupIds)
    %>
    <div class="slide slide-${i+1}  product-view image-view">
        <g:render template="/site/singleProductImageView" model="${[product: product, imageSize: imageSize, config: productConfig]}"/>
    </div>
</g:each>