<%@ page import="com.webcommander.util.AppUtil" %>
<g:applyLayout name="_productwidget">
    <g:if test="${productData.isCallForPriceEnabled}">
        <span class="button call-for-price"><site:message code="${config.label_for_call_for_price}"/> </span>
    </g:if>
    <g:else>
        <g:set var="available" value="${productData.isAvailable}"/>
        <plugin:hookTag hookPoint="productCartBlock" attrs="${[productId: product.id, available: available, product: productData]}">
            <g:if test="${config.add_to_cart.toBoolean() && productData.isTaxCodeFound}">
                <input type="text" class="product-quantity-selector text-type" value="${productData.supportedMinOrderQuantity}" spin-min="${productData.supportedMinOrderQuantity ?: 1}" spin-max="${productData.supportedMaxOrderQuantity ?: ''}" spin-step="${productData.isMultipleOrderQuantity ? productData.multipleOfOrderQuantity : '1'}">
                <span class="add-to-cart-button button et_pdp_add_to_cart ${available && !isPurchaseRestricted ? "": "disabled"}" title="${g.message(code: 'add.to.cart')}" et-category="button" product-id="${product.id}" cart-quantity="${productData.supportedMinOrderQuantity}" combined-isCombined="${product.isCombined}" combined-isFixed="${product.isCombinationPriceFixed}" ${(product.isCombined && !product.isCombinationPriceFixed) ? includedProducts.each {
                    out << "included-" + it.id + "='" + it.quantity + "'";
                } : ""} product-isDetailsPage = "true"><g:message code="add.to.cart"/></span>
            </g:if>
        </plugin:hookTag>
    </g:else>
</g:applyLayout>