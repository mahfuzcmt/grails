<%@ page import="com.webcommander.webcommerce.Product; com.webcommander.constants.NamedConstants" %>
<g:each in="${latestSolds}" var="product">
    <div class="report-block-item">
        <g:if test="${product.productType == NamedConstants.CART_OBJECT_TYPES.PRODUCT}">
            <g:set var="prdt" value="${Product.read(product.productId)}"/>
            <div class="image-container">
                <img src="${product.image}">
            </div>
            <div class="title-n-summary">
                <div class="title">
                    ${product.productName.encodeAsBMHTML() + (product.variation ? " (" + product.variation + ")" : "")}
                </div>
                <div class="timestamp">
                    <reporting:timestamp time="${product.created}" state="${product.state}" country="${product.country}"/>
                </div>
            </div>
        </g:if>
    </div>
</g:each>