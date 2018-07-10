<%@ page import="com.webcommander.util.AppUtil" %>
<!-- START Google Trusted Stores Order -->
<div id="gts-order" style="display:none;" translate="no">

    <!-- start order and merchant information -->
    <span id="gts-o-id">${order.id}</span>
    <span id="gts-o-domain">${domain}</span>
    <span id="gts-o-email">${order.billing.email}</span>
    <span id="gts-o-country">${order.billing.country.code}</span>
    <span id="gts-o-currency">${AppUtil.baseCurrency.code}</span>
    <span id="gts-o-total">${order.grandTotal}</span>
    <span id="gts-o-discounts">${order.totalDiscount}</span>
    <span id="gts-o-shipping-total">${order.shippingCost}</span>
    <span id="gts-o-tax-total">${order.totalTax}</span>
    <span id="gts-o-est-ship-date">${(order.updated.toZone(session.timezone) + config.ship_date_after.toInteger()).toFormattedString("YYYY-MM-dd", false, null, null, null)}</span>
    <span id="gts-o-est-delivery-date">${(order.updated.toZone(session.timezone) + config.deliver_date_after.toInteger()).toFormattedString("YYYY-MM-dd", false, null, null, null)}</span>
    <span id="gts-o-has-preorder">N</span>
    <span id="gts-o-has-digital">${hasDigitalGood ? "Y": "N"}</span>
    <!-- end order and merchant information -->

    <!-- start repeated item specific information -->
    <!-- item example: this area repeated for each item in the order -->
    <g:each in="${order.items}" var="item">
        <span class="gts-item">
            <span class="gts-i-name">${item.productName}</span>
            <span class="gts-i-price">${item.price}</span>
            <span class="gts-i-quantity">${item.quantity}</span>
        </span>
    </g:each>

    <!-- end item 1 example -->
    <!-- end repeated item specific information -->

</div>
<!-- END Google Trusted Stores Order -->