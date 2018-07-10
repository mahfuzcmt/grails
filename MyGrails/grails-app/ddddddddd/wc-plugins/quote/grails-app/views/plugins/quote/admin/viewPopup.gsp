<%--
  Created by IntelliJ IDEA.
  User: sajedur
  Date: 6/15/2015
  Time: 2:19 PM
--%>
<div class="quote-view-popup">
    <button type="button" class="requote"><g:message code="requote"/></button>
    <div class="basic-info">
        <div class="info-row">
            <label><g:message code="quote.id"/>:</label>
            <span class="value">${quote.id}</span>
        </div>
        <div class="info-row">
            <label><g:message code="quote.date"/>:</label>
            <span class="value">${quote.created.toAdminFormat(true, false, session.timezone)}</span>
        </div>
        <div class="info-row">
            <label><g:message code="discount"/>:</label>
            <span class="value">${quote.totalDiscount.toPrice()}</span>
        </div>
        <div class="info-row">
            <label><g:message code="total.tax"/>:</label>
            <span class="value">${quote.totalTax.toPrice()}</span>
        </div>
        <div class="info-row">
            <label><g:message code="total.shipping"/>:</label>
            <span class="value">${quote.shippingCost?.toPrice()}</span>
        </div>
        <div class="info-row">
            <label><g:message code="total.amount"/>:</label>
            <span class="value">${quote.grandTotal.toPrice()}</span>
        </div>
    </div>
    <table class="details-table">
        <tr>
            <th class="name-column"><g:message code="product"/></th>
            <th class="price-column"><g:message code="price"/></th>
            <th class="quantity-column"><g:message code="quantity"/></th>
            <th class="discount-column"><g:message code="discount"/></th>
            <th class="tax-column"><g:message code="tax"/></th>
            <th class="total-column"><g:message code="total.amount"/></th>
        </tr>
        <g:each in="${quote.quoteItems}" var="item">
            <tr>
                <td>${item.object.name.encodeAsBMHTML()} ${item.variations ? "(" + item.variations.join(", ").encodeAsBMHTML() + ")" : ""}</td>
                <td>${item.price.toPrice()}</td>
                <td>${item.quantity}</td>
                <td>${item.discount.toPrice()}</td>
                <td>${item.tax.toPrice()}</td>
                <td>${item.totalAmount.toPrice()}</td>
            </tr>
        </g:each>
    </table>
</div>