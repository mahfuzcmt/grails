<%@ page import="com.webcommander.util.AppUtil" %>
<input type="hidden" name="mode" value="edit">
<g:if test="${isClassEnabled}">
    <span class="share-toolbar toolbar hidden">
        <span class="toolbar-btn edit edit-section"><g:message code="edit"/></span>
    </span>
    <table class="cart-items">
        <tr>
            <th class="name-column"><g:message code="product.name"/> </th>
            <th class="method-column"><g:message code="shipping.method"/> </th>
        </tr>
        <g:each in="${cart.cartItemList}" var="cartItem">
            <g:set var="clazz" value="${shippingClasses.find { it.id == cartItem.selectedShippingMethod}}"/>
            <tr class="cart-item">
                <td>${cartItem.object.name.encodeAsBMHTML()} ${cartItem.variations ? "(" + cartItem.variations.join(", ").encodeAsBMHTML() + ")" : ""}</td>
                <td>
                    <g:if test="${clazz && cartItem.shippingCostMaps[clazz.id]}">
                        ${clazz.name} - ${AppUtil.siteCurrency.symbol}${cartItem.shippingCostMaps[clazz.id].shipping.toCurrency().toPrice()}
                    </g:if>
                    <g:else>
                        <span class="tool-icon remove" data-id="${cartItem.id}"></span>
                    </g:else>
                </td>
            </tr>
        </g:each>
    </table>
</g:if>
<g:elseif test="${cart.shippingCost.shipping != null}">
    <div class="info-row">
        <label><g:message code="shipping.cost"/>:</label>
        <span>${AppUtil.siteCurrency.symbol}${cart.shippingCost.shipping.toCurrency().toPrice()}</span>
    </div>
</g:elseif>
<g:else>
    <div class="not-supported-shipping-message message-block error-message">
        <g:message code="not.support.shipping.choose.address"/>
    </div>
</g:else>