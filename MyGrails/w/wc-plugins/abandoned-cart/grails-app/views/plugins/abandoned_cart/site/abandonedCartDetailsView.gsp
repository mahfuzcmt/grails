<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants;" %>
<h1><g:message code="abandoned.cart.details"/></h1>
<div id="abandoned-cart-details">
    <div class="form-row">
        <label><g:message code="cart.no"/>:</label>
        <span>${cart.id}</span>
    </div>
    <div class="form-row">
        <label><g:message code="created"/>:</label>
        <span>${cart.created.toSiteFormat(true, false, session.timezone)}</span>
    </div>
    <div class="form-row">
        <label><g:message code="discount"/>:</label>
        <span class="currency-symbol"> - ${session.currency?.symbol?:AppUtil.baseCurrency.symbol}</span>
        <span class="value">${cart.discount()?.toCurrency().toPrice()}</span>
    </div>
    <div class="form-row">
        <label><g:message code="total.tax"/>:</label>
        <span class="currency-symbol">${session.currency?.symbol?:AppUtil.baseCurrency.symbol}</span>
        <span class="value">${cart.tax().toCurrency().toPrice()}</span>
    </div>
    <div class="form-row">
        <label><g:message code="total.amount"/>:</label>
        <span class="currency-symbol">${session.currency?.symbol?:AppUtil.baseCurrency.symbol}</span>
        <span class="value">${cart.total().toCurrency().toPrice()}</span>
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
                        <td><div class="wrapper" data-label="<g:message code="product"/>:">${item.object().name.encodeAsBMHTML()} ${item.variations ? "(" + item.variations.join(", ").encodeAsBMHTML() + ")" : ""}</div></td>
                        <td><div class="wrapper" data-label="<g:message code="price"/>:">${priceObj.price.toCurrency().toPrice()}</div></td>
                        <td><div class="wrapper" data-label="<g:message code="quantity"/>:">${item.quantity}</div></td>
                        <td><div class="wrapper" data-label="<g:message code="discount"/>:">${priceObj.discount.toCurrency().toPrice()}</div></td>
                        <td><div class="wrapper" data-label="<g:message code="tax"/>:">${priceObj.tax.toCurrency().toPrice()}</div></td>
                        <td><div class="wrapper" data-label="<g:message code="total"/>:">${priceObj.total.toCurrency().toPrice()}</div></td>
                    </tr>
                </g:if>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="form-row btn-row">
        <button type="button" class="submit-button" cart-id="${cart.id}"><g:message code="add.to.cart"/></button>
        <button type="button" class="back-button"><g:message code="back"/></button>
    </div>
</div>