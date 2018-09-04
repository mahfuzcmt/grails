<%@ page import="com.webcommander.util.AppUtil" %>
<input type="hidden" name="mode" value="edit">
<g:if test="${isClassEnabled}">
    <table class="cart-items">
        <tr>
            <th class="name-column"><g:message code="product.name"/> </th>
            <th class="method-column"><g:message code="shipping.method"/> </th>
        </tr>
        <g:each in="${cart.cartItemList}" var="cartItem">
            <g:if test="${cartItem.isShippable}">
                <g:set var="isNotShippable"  value="${cartItem.shippingCostMaps.isEmpty()}"/>
                <tr class="cart-item${isNotShippable ? " not-shippable" : ""}">
                    <td>
                        <span class="name">${cartItem.object.name.encodeAsBMHTML()}${cartItem.variations ? "(" + cartItem.variations.join(", ").encodeAsBMHTML() + ")" : ""}</span>
                        <g:if test="${isNotShippable}"><span class="not-shippable-tag"><g:message code="not.shippable"/></span></g:if>
                    </td>
                    <td>
                        <g:if test="${isNotShippable}">
                            <span class="action-icon remove" title="<g:message code="remove"/>" data-id="${cartItem.id}"></span>
                        </g:if>
                        <g:else>
                            <select class="method-selector" name="shippingMethod.${cartItem.id}">
                                <g:each in="${shippingClasses}" var="clazz">
                                    <g:if test="${cartItem.shippingCostMaps[clazz.id]?.shipping != null}">
                                        <option value="${clazz.id}" ${cartItem.selectedShippingMethod == clazz.id ? "checked" : ""}>
                                            ${clazz.name} - ${AppUtil.siteCurrency.symbol}${cartItem.shippingCostMaps[clazz.id].shipping.toCurrency().toPrice()}
                                        </option>
                                    </g:if>
                                </g:each>
                            </select>
                        </g:else>
                    </td>
                </tr>
            </g:if>

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
<g:if test="${cart.shippingCost.shipping != null}">
    <input type="button" value="${g.message(code: 'continue')}" class="button step-continue-button">
</g:if>