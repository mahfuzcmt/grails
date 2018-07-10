<%@ page import="com.webcommander.webcommerce.Order; com.webcommander.util.AppUtil" %>
<g:set var="currencySymbol" value="${AppUtil.baseCurrency.symbol}"/>
<table style="border-collapse:collapse; width:700px;" border="0" cellspacing="0" cellpadding="0" align="left">
    <tr>
        <g:set var="totalColumn" value="${4}"/>
        <th style="padding:5px; background-color: #3997D6;  border-left: 1px solid #3997D6; border-right: 1px solid #3997D6; border-top: 1px solid #3997D6; color: #FFFFFF; text-align:center; font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">Name</th>
        <th style="padding:5px; background-color: #3997D6;  border-left: 1px solid #3997D6; border-right: 1px solid #3997D6; border-top: 1px solid #3997D6; color: #FFFFFF; text-align:center; font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">Unit Price</th>
        <th style="padding:5px; background-color: #3997D6;  border-left: 1px solid #3997D6; border-right: 1px solid #3997D6; border-top: 1px solid #3997D6; color: #FFFFFF; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">Ordered Quantity</th>
        <g:if test="${config["show_discount_each_item"] == "true"}">
            <th style="padding:5px; background-color: #3997D6;  border-left: 1px solid #3997D6; border-right: 1px solid #3997D6; border-top: 1px solid #3997D6; color: #FFFFFF; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">Discount</th>
            <g:set var="totalColumn" value="${++totalColumn}"/>
        </g:if>
        <g:if test="${config["show_tax_each_item"] == "true"}">
            <th style="padding:5px; background-color: #3997D6;  border-left: 1px solid #3997D6; border-right: 1px solid #3997D6; border-top: 1px solid #3997D6; color: #FFFFFF; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">Tax</th>
            <g:set var="totalColumn" value="${++totalColumn}"/>
        </g:if>
        <th style="padding:5px; background-color: #3997D6;  border-left: 1px solid #3997D6; border-right: 1px solid #3997D6; border-top: 1px solid #3997D6; color: #FFFFFF; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">Price</th>
    </tr>
    <g:each in="${order.items}" var="item">
        <tr>
            <td style="padding:5px; border:1px #e6e6e6 solid; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">${item.productName.encodeAsBMHTML()} ${item.variations ? "(" + item.variations.join(", ") + ")" : ""}</td>
            <td style="padding:5px; border:1px #e6e6e6 solid; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">${currencySymbol}${item.price.toAdminPrice()}</td>
            <td style="padding:5px; border:1px #e6e6e6 solid; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">${item.quantity}</td>
            <g:if test="${config["show_discount_each_item"] == "true"}">
                <td style="padding:5px; border:1px #e6e6e6 solid; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">${currencySymbol}${item.discount.toAdminPrice()}</td>
            </g:if>
            <g:if test="${config["show_tax_each_item"] == "true"}">
                <td style="padding:5px; border:1px #e6e6e6 solid; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">${currencySymbol}${item.tax.toAdminPrice()}</td>
            </g:if>
            <td style="padding:5px; border:1px #e6e6e6 solid; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">${currencySymbol}${item.totalPriceConsideringConfiguration.toAdminPrice()}</td>
        </tr>
    </g:each>
    <g:if test="${config["show_subtotal"] == "true"}">
        <tr>
            <td colspan="${totalColumn - 2}"></td>
            <td style="padding:5px; border-bottom:2px #e6e6e6 solid; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">Sub Total</td>
            <td style="padding:5px; border-bottom:2px #e6e6e6 solid; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">${currencySymbol}${order.subTotal.toAdminPrice()}</td>
        </tr>
    </g:if>
    <g:if test="${config["show_total_discount"] == "true"}">
        <tr>
            <td colspan="${totalColumn - 2}"></td>
            <td style="padding:5px; border-bottom:2px #e6e6e6 solid; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">Total Discount</td>
            <td style="padding:5px; border-bottom:2px #e6e6e6 solid; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">${currencySymbol}${order.totalDiscount.toAdminPrice()}</td>
        </tr>
    </g:if>
    <g:if test="${config["show_sub_total_tax"] == "true"}">
        <tr>
            <td colspan="${totalColumn - 2}"></td>
            <td style="padding:5px; border-bottom:2px #e6e6e6 solid; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">Tax</td>
            <td style="padding:5px; border-bottom:2px #e6e6e6 solid; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">${currencySymbol}${order.totalTax.toAdminPrice()}</td>
        </tr>
    </g:if>
    <g:if test="${config["show_shipping_cost"] == "true"}">
        <tr>
            <td colspan="${totalColumn - 2}"></td>
            <td style="padding:5px; border-bottom:2px #e6e6e6 solid; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">Shipping Cost</td>
            <td style="padding:5px; border-bottom:2px #e6e6e6 solid; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">${currencySymbol}${order.shippingCost.toAdminPrice()}</td>
        </tr>
    </g:if>
    <g:if test="${config["show_shipping_tax"] == "true"}">
        <tr>
            <td colspan="${totalColumn - 2}"></td>
            <td style="padding:5px; border-bottom:2px #e6e6e6 solid; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">Shipping Tax</td>
            <td style="padding:5px; border-bottom:2px #e6e6e6 solid; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">${currencySymbol}${order.shippingTax.toAdminPrice()}</td>
        </tr>
    </g:if>
    <g:if test="${config["show_handling_cost"] == "true"}">
        <tr>
            <td colspan="${totalColumn - 2}"></td>
            <td style="padding:5px; border-bottom:2px #e6e6e6 solid; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">Handling Cost</td>
            <td style="padding:5px; border-bottom:2px #e6e6e6 solid; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">${currencySymbol}${order.handlingCost.toAdminPrice()}</td>
        </tr>
    </g:if>
    <g:if test="${config["show_payment_surcharge"] == "true"}">
        <tr>
            <td colspan="${totalColumn - 2}"></td>
            <td style="padding:5px; border-bottom:2px #e6e6e6 solid; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">Payment Surcharge</td>
            <td style="padding:5px; border-bottom:2px #e6e6e6 solid; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">${currencySymbol}${order.totalSurcharge.toAdminPrice()}</td>
        </tr>
    </g:if>
    <tr>
        <td colspan="${totalColumn - 2}"></td>
        <td style="padding:5px; text-align:center; font-weight:bold;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">Total </td>
        <td style="padding:5px; text-align:center; font-weight:bold;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">${currencySymbol}${order.grandTotal.toAdminPrice()}</td>
    </tr>
    <tr>
        <td colspan="${totalColumn - 2}"></td>
        <td style="padding:5px; background-color: #E0F0FB; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">Paid </td>
        <td style="padding:5px; background-color: #E0F0FB; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">${currencySymbol}${order.paid.toAdminPrice()}</td>
    </tr>
    <tr>
        <td colspan="${totalColumn - 2}"></td>
        <td style="padding:5px; text-align:center; font-weight:bold;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">Due</td>
        <td style="padding:5px; text-align:center; font-weight:bold;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">${currencySymbol}${order.due.toAdminPrice()}</td>
    </tr>
</table>
