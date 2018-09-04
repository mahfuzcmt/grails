<%@ page import="com.webcommander.util.AppUtil" %>
<div class="order-view-button-line">
    <button class="manage-payment"><g:message code="manage.payment"/></button>
    <button class="manage-payment"><g:message code="manage.shipment"/></button>
</div>
<div class="multi-column two-column">
    <div class="columns first-column">
        <h3><g:message code="billing.address"/></h3>
        ${order.billing.firstName.encodeAsBMHTML() + (order.billing.lastName ? " " + order.billing.lastName.encodeAsBMHTML() : "")}<br/>
        ${order.billing.addressLine1.encodeAsBMHTML()}<br/>
        <g:if test="${order.billing.addressLine2}">
            ${order.billing.addressLine2.encodeAsBMHTML()}<br/>
        </g:if>
        <g:if test="${!order.billing.state}">
            ${order.billing.city ? order.billing.city.encodeAsBMHTML() + ", " : ""} ${order.billing.postCode.encodeAsBMHTML()}<br/>
        </g:if>
        <g:else>
            ${order.billing.city ? order.billing.city.encodeAsBMHTML() + ", " : ""} ${order.billing.state ? order.billing.state.code + ", "
                : ""} ${order.billing.postCode ? order.billing.postCode : ""}<br/>
        </g:else>
        ${order.billing.country?.name.encodeAsBMHTML()}<br/>
        ${order.billing.email.encodeAsBMHTML()} <br/>
        <g:if test="${order.billing.phone}">
            ${order.billing.phone.encodeAsBMHTML()}<br/>
        </g:if>
        <g:if test="${order.billing.fax}">
            ${order.billing.fax.encodeAsBMHTML()}<br/>
        </g:if>
    </div><div class="columns last-column">
        <h3><g:message code="shipping.address"/> </h3>
        ${order.shipping.firstName.encodeAsBMHTML() + (order.shipping.lastName ? " " + order.shipping.lastName.encodeAsBMHTML() : "")}<br/>
        ${order.shipping.addressLine1.encodeAsBMHTML()}<br/>
        <g:if test="${order.shipping.addressLine2}">
            ${order.shipping.addressLine2.encodeAsBMHTML()}<br/>
        </g:if>
        <g:if test="${!order.shipping.state}">
            ${order.shipping.city ? order.shipping.city.encodeAsBMHTML() + ", " : ""} ${order.shipping.postCode.encodeAsBMHTML()}<br/>
        </g:if>
        <g:else>
            ${order.shipping.city ? order.shipping.city.encodeAsBMHTML() + ", " : ""} ${order.shipping.state.code ?
                order.shipping.state.code + ", " : ""}  ${order.shipping.postCode}<br/>
        </g:else>
        ${order.shipping.country?.name.encodeAsBMHTML()}<br/>
        ${order.shipping.email.encodeAsBMHTML()} <br/>
        <g:if test="${order.shipping.phone}">
            ${order.shipping.phone.encodeAsBMHTML()}<br/>
        </g:if>
        <g:if test="${order.shipping.fax}">
            ${order.shipping.fax.encodeAsBMHTML()}<br/>
        </g:if>
    </div>
</div>
<g:set var="currencySymbol" value="${AppUtil.baseCurrency.symbol}"/>
<table class="order-details-table">
    <colgroup>
        <col class="product-name-column">
        <col class="unit-price-column">
        <col class="quantity-column">
        <col class="tax-column">
        <col class="discount-column">
        <col class="total-price-column">
    </colgroup>
    <tr class="title">
        <th><g:message code="product.name"/></th>
        <th><g:message code="unit.price"/></th>
        <th><g:message code="quantity"/></th>
        <th><g:message code="tax"/></th>
        <th><g:message code="discount"/></th>
        <th><g:message code="amount"/></th>
    </tr>
    <g:each in="${order.items}" status="i" var="item">
        <g:set var="object" value="${item}"/>
        <tr>
            <td class="product-name">
                <div class="name">${item.productName.encodeAsBMHTML()}</div>
                <div class="variations">
                    <g:each in="${item.variations}" var="variation">
                        <div>${variation}</span>
                    </g:each>
                </div>
            </td>
            <td>${currencySymbol}${item.price.toAdminPrice()}</td>
            <td>${item.quantity}</td>
            <td>${currencySymbol}${item.tax.toAdminPrice()}</td>
            <td>${currencySymbol}${item.discount.toAdminPrice()}</td>
            <td class="price">${currencySymbol}${item.totalPriceConsideringConfiguration.toAdminPrice()}</td>
        </tr>
    </g:each>
    <tfoot>
    <tr>
        <td class="empty-left-footer-block" colspan="4"></td>
        <td class="total-label"><g:message code="sub.total"/></td>
        <td class="price">${currencySymbol}${order.subTotal.toAdminPrice()}</td>
    </tr>
    <tr class="discount-row">
        <td class="empty-left-footer-block" colspan="4"></td>
        <td class="total-label"><g:message code="discount"/></td>
        <td class="price"> - ${currencySymbol}${order.totalDiscount.toAdminPrice()}</td>
    </tr>
    <plugin:hookTag hookPoint="afterTotalDiscountRow" attrs="[page: 'orderInfoView']"/>
    <tr>
        <td class="empty-left-footer-block" colspan="4"></td>
        <td class="total-label"><g:message code="total.tax"/></td>
        <td class="price">${currencySymbol}${order.totalTax.toAdminPrice()}</td>
    </tr>
    <tr>
        <td class="empty-left-footer-block" colspan="4"></td>
        <td class="total-label"><g:message code="order.total"/></td>
        <td class="price">${currencySymbol}${order.grandTotal.toAdminPrice()}</td>
    </tr>
    <plugin:hookTag hookPoint="afterOrderTotalRow" attrs="[page: 'orderInfoView']"/>
    <g:if test="${order.due}">
        <tr>
            <td class="empty-left-footer-block" colspan="4"></td>
            <td class="total-label"><g:message code="total.due"/></td>
            <td class="price">${currencySymbol}${order.due.toAdminPrice()}</td>
        </tr>
    </g:if>
    </tfoot>
</table>