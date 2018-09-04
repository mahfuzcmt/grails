<g:if test="${product.isTaxCodeFound}">
<div class="btn-add">
    <plugin:hookTag hookPoint="productImageViewPriceBlock" attrs="${[product: product]}">
        <g:if test="${config["add_to_cart"] == "true"}">
            <g:if test="${product.isCallForPriceEnabled}">
                <span class="button call-for-price"><site:message code="${config.label_for_call_for_price}"/></span>
            </g:if>
            <g:elseif test="${config['is_purchase_restricted'] != true}">
                <span class="add-to-cart-button button et_pdp_add_to_cart"  et-category="button" product-id="${product.id}" cart-min-quantity="${product.supportedMinOrderQuantity}" title="${g.message(code: 'add.to.cart')}">
                    <g:message code="add.to.cart"/>
                </span>
            </g:elseif>
        </g:if>
    </plugin:hookTag>
</div>
</g:if>