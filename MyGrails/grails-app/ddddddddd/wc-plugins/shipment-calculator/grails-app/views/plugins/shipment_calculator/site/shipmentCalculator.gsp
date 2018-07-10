<%@ page import="com.webcommander.util.AppUtil; com.webcommander.webcommerce.ShippingClass" %>
<h4><g:message code="shipping.cost"/></h4>
<g:if test="${config['enable_shipping_class'] == "true" && !shippingCostMaps.isEmpty()}">
    <table>
        <tr>
            <th class="class-column"><g:message code="select.shipping.class"/></th>
            <th class="shiping-column affected-row"><g:message code="shipping.cost"/></th>
            <th class="handling-column affected-row"><g:message code="handling.cost"/></th>
        </tr>
        <tr>
            <td class="class-column">
                <select name="shippingMethod" class="shipping-class-selector">
                    <g:each in="${shippingClasses}" var="clazz">
                        <g:if test="${shippingCostMaps[clazz.id]}">
                            <option value="${clazz.id}" data-invalid-shipping="${shippingCostMaps[clazz.id].shipping == null}"
                                    data-shipping="${AppUtil.siteCurrency.symbol}${shippingCostMaps[clazz.id].shipping?.toCurrency()?.toPrice()}"
                                    data-handling="${AppUtil.siteCurrency.symbol}${shippingCostMaps[clazz.id].handling.toCurrency().toPrice()}"
                            >${clazz.name}</option>
                        </g:if>
                    </g:each>
                </select>
            </td>
            <td class="shipping-class-shipping affected-row"></td>
            <td class="shipping-class-handling affected-row"></td>
        </tr>
    </table>
    <span style="display: none" class="error"><g:message code="${error ?: "not.support.shipping.choose.address"}"/></span>
</g:if>
<g:elseif test="${shippingCostMaps?.shipping != null}">
    <div class="form-row">
        <label><g:message code="shipping.cost"/>:</label>
        <span>${AppUtil.baseCurrency.symbol + shippingCostMaps.shipping.toPrice()}</span>
    </div>
    <div class="form-row">
        <label><g:message code="handling.cost"/>:</label>
        <span>${AppUtil.baseCurrency.symbol + shippingCostMaps.handling.toPrice()}</span>
    </div>
</g:elseif>
<g:else>
    <span class="error"><g:message code="${error ?: "not.support.shipping.choose.address"}"/></span>
</g:else>