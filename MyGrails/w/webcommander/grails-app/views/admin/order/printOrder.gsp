<%@ page import="com.webcommander.webcommerce.Product; com.webcommander.constants.NamedConstants; com.webcommander.util.AppUtil; com.webcommander.webcommerce.PaymentGateway; com.webcommander.util.StringUtil" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
        <link href='http://fonts.googleapis.com/css?family=Oswald|Open+Sans' rel='stylesheet' type='text/css'>
        <title><g:message code="order"/> </title>
        <style type="text/css">
            .print-order-form * {
                border-collapse : collapse;
                border-radius   : 0 0 0 0;
                box-shadow      : none;
                font-family     : 'Open Sans', Trebuchet MS, Arial, sans-serif;
                font-size       : 12px;
                font-style      : normal;
                font-variant    : normal;
                margin          : 0;
                padding         : 0;
                text-decoration : none;
                border          : none;
            }

            body {
                font-size        : 12px;
                font-family      : 'Open Sans', sans-serif;
                color            : #444444;
                background-color : white;
            }

            p {
                line-height : 20px;
            }

            #order_table {
                margin        : 35px auto 25px;
                border-bottom : 3px solid #E2E2E2;
            }

            #order_table h4 {
                font-size : 14px;
            }

            #order_table h3 {
                font-size  : 17px;
                color      : #1F77B6;
                text-align : right;
            }

            .company_info {
                font-size : 12px;
            }

            .company_info h4 {
                font-family    : 'Oswald', sans-serif;
                font-size      : 16px;
                padding-bottom : 5px;
            }

            p.order_number {
                background  : none repeat scroll 0 0 #1F77B6;
                color       : #FFFFFF;
                display     : block;
                font-weight : bold;
                padding     : 5px 10px;
                width       : 100px;
            }

            p.order_number span {
                padding-left : 15px;
            }

            tr.order_info_title td {
                background : none repeat scroll 0 0 #777777 !important;
                color      : #FFFFFF;
            }

            tr.order_info_details td {
                background : none repeat scroll 0 0 #F1F1F1;
            }

            table#order_title {
                border-top : 3px solid #E2E2E2;
                margin     : 20px 0;
            }

            .order_info_title td {
                font-weight : bold;
            }

            #order_info td {
                border-right  : 1px solid #FFFFFF;
                border-bottom : 1px solid #E2E2E2;
                padding       : 5px 0 5px 10px;
            }

            #order_checkout_box {
                border : 1px solid #E2E2E2;
                margin : 20px 0 30px;
            }

            #order_checkout_box td {
                padding     : 10px;
                text-align  : center;
                border      : none;
                font-weight : bold;
            }

            #order_checkout_box td.paid {
                background : none repeat scroll 0 0 #1F77B6;
                color      : #FFFFFF;
            }

            .common_table td {
                background   : none repeat scroll 0 0 #F1F1F1;
                padding      : 8px 0 8px 10px;
                border-right : 1px solid #FFFFFF;
            }

            .common_table tr.tr_bg td {
                background : none repeat scroll 0 0 #FFFFFF;
            }

            .common_table tr.title td {
                background  : #DEEFFC;
                font-weight : bold;
                color       : #333333;
            }

            .common_table {
                border-bottom : 1px solid #E2E2E2;
            }

            #order-total-details td {
                border-bottom : 1px solid #FFFFFF;
                color         : #333333;
                font-size     : 15px;
                padding       : 5px;
            }

            #order-total-details td.total {
                color       : #FFFFFF;
                font-weight : bold;
            }

            #order-total-details {
                margin-top : 5px;
            }

            #aditional_info td {
                padding : 10px 15px;
            }

            #aditional_info td ol {
                padding-left : 15px;
            }

            .table_heading {
                padding : 45px 0 10px;
            }

            .product_name {
                color : #111111;
            }

            #payment_details_table {
                margin-bottom : 60px;
            }

            .print-order-form {
                overflow : auto;
            }

            .total {
                background : #1F77B6;
            }

            .bgcolor1 {
                background : #F1F1F1;
            }
            .print-order-form #store-image-preview {
                max-height: 150px;
                max-width: 150px;
            }
        </style>
        <script type='text/javascript'>function printMe() {window.print();}</script>
    </head>
    <body id="print-order">
    <g:set var="currencySymbol" value="${AppUtil.baseCurrency.symbol}"/>
    <g:set var="currencyCode" value="${AppUtil.siteCurrency.code}"/>
        <div class="print-order-form">
            <table id="order_table" border="0" cellspacing="0" cellpadding="0" style="table-layout: fixed; margin: 0; width: 665px;">
                <tr>
                    <td>
                        <img id="store-image-preview" src='${appResource.getStoreLogoURL(storeDetails: storeDetail)}'
                            ${storeDetail.image ? "" : "style='display: none'"}>
                    </td>
                    <td class="company_info" style="padding-left:10px; width: 257px; border-left: 1px solid #e2e2e2; text-align: right;">
                        <h3>${storeDetail.name.encodeAsBMHTML()}</h3>
                        <p>${storeDetail.address.addressLine1.encodeAsBMHTML()}<br/>
                            <g:if test="${storeDetail.address.addressLine2}">
                                ${storeDetail.address.addressLine2.encodeAsBMHTML()}<br/>
                            </g:if>
                            ${storeDetail.address.city ? storeDetail.address.city.encodeAsBMHTML()  + (storeDetail.address.state || storeDetail.address.postCode ? ", " : "") : ""}
                            ${storeDetail.address.state ? storeDetail.address.state.code + (storeDetail.address.postCode ? ", " : "") : ""}
                            ${storeDetail.address.postCode.encodeAsBMHTML()}<br/>
                            ${storeDetail.address.country.name.encodeAsBMHTML()}<br/>
                            <g:message code="email"/> ${storeDetail.address.email.encodeAsBMHTML()} <br/>
                            <g:if test="${storeDetail.address.phone}">
                                <g:message code="phone"/> ${storeDetail.address.phone.encodeAsBMHTML()}<br/>
                            </g:if>
                            <g:if test="${storeDetail.address.fax}">
                                <g:message code="fax"/> ${storeDetail.address.fax.encodeAsBMHTML()}<br/>
                            </g:if>
                        </p>
                    </td>
                </tr>
                <tr>
                    <td colspan="2">
                        <table id="order_title" width="100%" border="0" cellspacing="0" cellpadding="0">
                            <tr>
                                <td>&nbsp;</td>
                                <td>&nbsp;</td>
                            </tr>
                            <tr>
                                <td><p class="order_number"><g:message code="order" />#<span> ${order.id} </span></p></td>
                                <td ><h3 style="text-align: right;"><g:message code="order.details"/></h3></td>
                            </tr>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td colspan="2">
                        <table id="order_info" width="100%" border="0" cellspacing="0" cellpadding="0">
                            <colgroup>
                                <col>
                                <col style="width: 33%">
                                <col style="width: 33%">
                            </colgroup>
                            <tr class="order_info_title">
                                <td><g:message code="order.information"/></td>
                                <td><g:message code="billing.address"/></td>
                                <td><g:message code="shipping.address"/></td>
                            </tr>
                            <tr class="order_info_details">
                                <td valign="top">
                                    <p>
                                        <g:message code="customer"/> :
                                        <strong>
                                            <g:if test="${order.customerId == null}">
                                                <g:message code="guest.customer" />
                                            </g:if>
                                            <g:else>
                                                ${order.customerName.encodeAsBMHTML()}
                                            </g:else>
                                        </strong>
                                        <br/>
                                        <g:message code="order.date" /> : <strong>${order.created.toAdminFormat(true, false, session.timezone)} </strong>
                                        <br/>
                                        <g:message code="order.status" /> : <strong><g:message code="${order.orderStatus}"/></strong>
                                        <br/>
                                        <g:message code="payment.status" /> : <strong><g:message code="${order.paymentStatus}"/> </strong><br/>
                                        <g:message code="shipping.status" /> : <strong> <g:message code="${order.shippingStatus}"/> </strong>
                                    </p>
                                </td>
                                <td valign="top">
                                    <p><strong>
                                        ${order.billing.firstName.encodeAsBMHTML() + (order.billing.lastName ? " " + order.billing.lastName.encodeAsBMHTML() : "")}</strong><br/>
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
                                        <g:if test="${order.billing.mobile}">
                                            ${order.billing.mobile.encodeAsBMHTML()}<br/>
                                        </g:if>
                                        <g:if test="${order.billing.fax}">
                                            ${order.billing.fax.encodeAsBMHTML()}<br/>
                                        </g:if>
                                    </p>
                                </td>
                                <td valign="top">
                                    <p>
                                        <g:if test="${order.shipping}">
                                            <strong>${order.shipping.firstName.encodeAsBMHTML() + (order.shipping.lastName ? " " + order.shipping.lastName.encodeAsBMHTML() : "")}</strong><br/>
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
                                            <g:if test="${order.shipping.mobile}">
                                                ${order.shipping.mobile.encodeAsBMHTML()}<br/>
                                            </g:if>
                                            <g:if test="${order.shipping.fax}">
                                                ${order.shipping.fax.encodeAsBMHTML()}<br/>
                                            </g:if>
                                        </g:if>
                                    </p>
                                </td>
                            </tr>
                            <tr>
                                <td style="padding:0 0 10px 0; border: 0 none; vertical-align: bottom;" >
                                    <h4><g:message code="order.items" /> </h4>
                                </td>
                                <td style=" border: 0 none;"></td>
                                <td style=" border: 0 none;">
                                    <table id="order_checkout_box" width="100%" border="0" cellspacing="0" cellpadding="0">
                                        <tr>
                                            <td width="33%"><g:message code="order.total" />
                                                <span style="font-size:20px; color:#1F77B6;">
                                                   <span class="currency-code">${currencyCode}</span>
                                                   ${currencySymbol}${order.grandTotal.toAdminPrice()}
                                                </span>
                                            </td>
                                            <td width="33%" class="paid"><g:message code="paid" /> <br /> ${currencySymbol}${order.paid.toAdminPrice()}</td>
                                            <td width="33%"><g:message code="due" /> <br /> ${currencySymbol}${order.due.toAdminPrice()}</td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td colspan="2">
                        <table id="order_items_table" class="common_table" width="100%" border="0" cellspacing="0" cellpadding="0">
                            <tr class="title">
                                <td><g:message code="product" /> </td>
                                <td><g:message code="sku" /> </td>
                                <td><g:message code="quantity" /> </td>
                                <td><g:message code="unit.price" /> </td>
                                <g:if test="${config["show_discount_each_item"] == "true"}">
                                    <td><g:message code="discount" /> </td>
                                </g:if>
                                 <g:if test="${config["show_tax_each_item"] == "true"}">
                                     <td><g:message code="tax"/> </td>
                                 </g:if>
                                <td><g:message code="amount" /> </td>
                            </tr>
                            <g:each in="${order.items}" status="i" var="item">
                                <tr class="${i % 2 == 0 ? '' : 'tr_bg'}">
                                    <td>
                                        ${item.productName.encodeAsBMHTML()} ${item.variations ? "(" + item.variations.join(", ").encodeAsBMHTML() + ")" : ""}
                                        <plugin:hookTag hookPoint="printOrderProductColumn" attrs="${[orderItem: item]}"/>
                                    </td>
                                    <td>${item.itemNumber?.encodeAsBMHTML()}</td>
                                    <td>${item.quantity}</td>
                                    <td>${item.displayPrice?.toAdminPrice()}</td>
                                    <g:if test="${config["show_discount_each_item"] == "true"}">
                                        <td>${currencySymbol}${item.discount.toAdminPrice()}</td>
                                    </g:if>
                                    <g:if test="${config["show_tax_each_item"] == "true"}">
                                        <td>${currencySymbol}${item.tax.toAdminPrice()}</td>
                                    </g:if>
                                    <td>${currencySymbol}${item.totalPriceConsideringConfiguration?.toAdminPrice()}</td>
                                </tr>
                            </g:each>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td colspan="2">
                        <table width="100%" border="0" cellspacing="0" cellpadding="0">
                            <tr>
                                <td valign="bottom" colspan="2">
                                </td>
                                <td width="40%">
                                    <table id="order-total-details" width="100%" border="0" cellspacing="0" cellpadding="0">
                                        <g:if test="${config["show_subtotal"] == "true"}">
                                            <tr>
                                                <td align="right" style="padding-right:15px; color:#333;"><g:message code="sub.total"/> </td>
                                                <td class="total"  align="center" width="50%">${currencySymbol}${order.subTotal.toAdminPrice()}</td>
                                            </tr>
                                        </g:if>
                                        <g:if test="${config["show_total_discount"] == "true"}">
                                            <tr>
                                                <td align="right" style="padding-right:15px; color:#333;"><g:message code="discount"/> </td>
                                                <td class="bgcolor1" align="center">${currencySymbol}${order.totalDiscount.toAdminPrice()}</td>
                                            </tr>
                                        </g:if>
                                        <plugin:hookTag hookPoint="afterTotalDiscountRow" attrs="[page: 'printOrder']"/>
                                        <g:if test="${config["show_sub_total_tax"] == "true"}">
                                            <tr>
                                                <td align="right" style="padding-right:15px; color:#333;"><g:message code="tax"/> </td>
                                                <td class="bgcolor1" align="center">${currencySymbol}${order.totalTax.toAdminPrice()}</td>
                                            </tr>
                                        </g:if>
                                        <g:if test="${config["show_shipping_cost"] == "true"}">
                                            <tr>
                                                <td align="right" style="padding-right:15px; color:#333;"><g:message code="shipping"/> </td>
                                                <td class="bgcolor1" align="center">${currencySymbol}${ order.shippingCost.toAdminPrice()}</td>
                                            </tr>
                                        </g:if>
                                        <g:if test="${config["show_shipping_tax"] == "true"}">
                                            <tr>
                                                <td align="right" style="padding-right:15px; color:#333;"><g:message code="shipping.tax"/> </td>
                                                <td class="bgcolor1" align="center">${currencySymbol}${order.shippingTax.toAdminPrice()}</td>
                                            </tr>
                                        </g:if>
                                        <g:if test="${config["show_handling_cost"] == "true"}">
                                            <tr>
                                                <td align="right" style="padding-right:15px; color:#333;"><g:message code="handling.cost"/> </td>
                                                <td class="bgcolor1" align="center">${currencySymbol}${order.handlingCost.toAdminPrice()}</td>
                                            </tr>
                                        </g:if>
                                        <g:if test="${config["show_payment_surcharge"] == "true"}">
                                            <tr>
                                                <td align="right" style="padding-right:15px; color:#333;"><g:message code="surcharge"/></td>
                                                <td class="bgcolor1" align="center">${currencySymbol}${order.totalSurcharge.toAdminPrice()}</td>
                                            </tr>
                                        </g:if>
                                        <tr>
                                            <td align="right" style="padding-right:15px; color:#333;"><g:message code="order.total"/> </td>
                                            <td class="total" bgcolor="#1F77B6" align="center">
                                                <span class="currency-code">${currencyCode}</span>
                                                ${currencySymbol}${order.grandTotal?.toAdminPrice()}</td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td colspan="2">
                        <h4 class="table_heading"><g:message code="shipping.details" /> </h4>
                        <table id="shipping_details_table" class="common_table" width="100%" border="0" cellspacing="0" cellpadding="0">
                            <tr class="title">
                                <td><g:message code="product.name" /> </td>
                                <td><g:message code="ordered.quantity" /> </td>
                                <td><g:message code="shipment.quantity" /> </td>
                            </tr>
                            <g:each in="${order.items}" status="i" var="item">
                                <tr class="${i % 2 == 0 ? '' : 'tr_bg'}">
                                    <td class="product_name">${item.productName.encodeAsBMHTML()}</td>
                                    <td>${item.quantity}</td>
                                    <g:set var="delivered" value="${shippedQuantity[i] == null ? 0 : shippedQuantity[i].deliveredQuantity }"/>
                                    <td>${delivered}</td>
                                </tr>
                            </g:each>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td colspan="2">
                        <h4 class="table_heading"><g:message code="payment.details" /> </h4>
                        <table id="payment_details_table" class="common_table" width="100%" border="0" cellspacing="0" cellpadding="0">
                            <tr class="title">
                                <td><g:message code="payment.date"/> </td>
                                <td><g:message code="payment.method"/></td>
                                <td><g:message code="track.info"/></td>
                                <td><g:message code="status"/></td>
                                <td><g:message code="amount"/></td>
                            </tr>
                            <g:each in="${order.payments}" status="i" var="payment">
                                <tr class="${i % 2 == 0 ? '' : 'tr_bg'}">
                                    <td>
                                        <div style="word-wrap: break-word; ">${payment.payingDate.toAdminFormat(true, false, session.timezone)}</div>
                                    </td>
                                    <td style="word-wrap: break-word; "><g:message code="${PaymentGateway.findByCode(payment.gatewayCode).name}"/></td>
                                    <td style="word-wrap: break-word; ">${payment.trackInfo}</td>
                                    <td style="word-wrap: break-word; "><g:message code="${payment.status}"/></td>
                                    <td style="word-wrap: break-word; ">${currencySymbol}${payment.amount.toAdminPrice()}</td>
                                </tr>
                            </g:each>
                        </table>
                    </td>
                </tr>
            </table>
        </div>
    </body>
</html>


