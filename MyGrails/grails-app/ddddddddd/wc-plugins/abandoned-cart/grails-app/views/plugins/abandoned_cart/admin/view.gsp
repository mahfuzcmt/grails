<%@ page import="com.webcommander.util.AppUtil" %>
<g:set var="currencySymbol" value="${session.currency?.symbol ?: AppUtil.baseCurrency.symbol}"/>
<h3><g:message code="abandoned.cart.details"/></h3>
<div class="info-row">
    <label><g:message code="cart.no"/>:</label>
    <span class="value">${cart.id}</span>
</div>
<div class="info-row">
    <label><g:message code="created"/>:</label>
    <span class="value">${cart.created.toAdminFormat(true, false, session.timezone)}</span>
</div>
<div class="info-row">
    <label><g:message code="discount"/>:</label>
    <span class="value">
        <span class="currency-symbol"> - ${currencySymbol}</span>
        <span class="value">${cart.discount()?.toCurrency().toPrice()}</span>
    </span>
</div>
<div class="info-row">
    <label><g:message code="total.tax"/>:</label>
    <span class="value">
        <span class="currency-symbol">${currencySymbol}</span>
        <span class="value">${cart.tax().toCurrency().toPrice()}</span>
    </span>
</div>
<div class="info-row">
    <label><g:message code="total.amount"/>:</label>
    <span class="value">
        <span class="currency-symbol">${currencySymbol}</span>
        <span class="value">${cart.total().toCurrency().toPrice()}</span>
    </span>
</div>
<div class="abandoned-cart-items-wrap">
    <label><g:message code="order.items"/></label>
    <table>
        <colgroup>
            <col class="product-col">
            <col class="price-col">
            <col class="quantity-col">
            <col class="discount-col">
            <col class="tax-col">
            <col class="total-col">
        </colgroup>
        <thead>
        <tr>
            <th><g:message code="product"/></th>
            <th><g:message code="price"/></th>
            <th><g:message code="quantity"/></th>
            <th><g:message code="discount"/></th>
            <th><g:message code="tax"/></th>
            <th><g:message code="total"/></th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${cart.cartItems}" var="item">
            <g:set var="priceObj" value="${item.priceObject()}"/>
            <g:if test="${priceObj}">
                <tr>
                    <td>${item.object().name.encodeAsBMHTML()} ${item.variations ? "(" + item.variations.join(", ").encodeAsBMHTML() + ")" : ""}</td>
                    <td>
                        <span class="currency-symbol">${currencySymbol}</span>
                        <span>${priceObj.price.toCurrency().toPrice()}</span>
                    </td>
                    <td>${item.quantity}</td>
                    <td>
                        <span class="currency-symbol">${currencySymbol}</span>
                        <span>${priceObj.discount.toCurrency().toPrice()}</span>
                    </td>
                    <td>
                        <span class="currency-symbol">${currencySymbol}</span>
                        <span>${priceObj.tax.toCurrency().toPrice()}</span>
                    </td>
                    <td>
                        <span class="currency-symbol">${currencySymbol}</span>
                        <span>${priceObj.total.toCurrency().toPrice()}</span>
                    </td>
                </tr>
            </g:if>
        </g:each>
        </tbody>
    </table>
</div>