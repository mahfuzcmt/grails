<%@ page import="com.webcommander.webcommerce.Product; com.webcommander.webcommerce.Order; com.webcommander.constants.NamedConstants; com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants" %>
<g:set var="productService" bean="productService"/>
<g:set var="currencyCode" value="${AppUtil.siteCurrency.code}"/>
<div class="message-details">
    <span class="accepted payment-success-sign ${payment.status}"></span>
    <g:if test="${payment.status ==  DomainConstants.PAYMENT_STATUS.SUCCESS}">
        <g:if test="${gateway && gateway.successMessage}">
            <span class="custom-message success-message">${gateway.successMessage}</span>
        </g:if>
    </g:if>
    <g:elseif test="${gateway && gateway.pendingMessage}">
        <span class="custom-message pending-message">${gateway.pendingMessage}</span>
    </g:elseif>
    <p class="post-order-contact-us">
        <g:message code="question.order.free.contact.us"/> <a href='mailto:${storeDetail.address.email}'>${storeDetail.address.email}</a>
    </p>
    <p class="order-info"> Your Order ID # <span class='order-id'>${payment.order.id}</span></p>
</div>
<div class="order-details">
    <g:set var="currencySymbol" value="${session.currency?.symbol ?: AppUtil.baseCurrency.symbol}"/>
    <g:set var="order" value="${payment.order}"/>
    <h4 class="table_heading"><g:message code="order.details" /> </h4>
    <table class="cartitem-table order_details_table">
        <colgroup>
            <g:set var="totalColumn" value="${4}"/>
            <col class="product-name-column">
            <col class="unit-price-column">
            <col class="quantity-column">
            <g:if test="${config["show_tax_each_item"] == "true"}">
                <col class="tax-column">
                <g:set var="totalColumn" value="${++totalColumn}"/>
            </g:if>
            <g:if test="${config["show_discount_each_item"] == "true"}">
                <col class="discount-column">
                <g:set var="totalColumn" value="${++totalColumn}"/>
            </g:if>
            <col class="total-price-column">
        </colgroup>
        <tr class="title">
            <th class="product-name"><g:message code="product.name"/></th>
            <th class="unit-price"><g:message code="unit.price"/></th>
            <th class="quantity"><g:message code="quantity"/></th>
            <g:if test="${config["show_tax_each_item"] == "true"}">
                <th class="tax"><g:message code="tax"/></th>
            </g:if>
            <g:if test="${config["show_discount_each_item"] == "true"}">
                <th class="discount"><g:message code="discount"/></th>
            </g:if>
            <th class="price">
                <g:message code="amount"/>
            </th>
        </tr>
        <g:each in="${order.items}" status="i" var="item">
            <g:set var="object" value="${item}"/>
            <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                <td class="product-name">
                    <div class="wrapper" data-label="<g:message code="product.name"/>:">
                        <g:set var="needEndingTag" value="${false}"/>
                        <g:if test="${payment.status ==  DomainConstants.PAYMENT_STATUS.SUCCESS && item.productType == NamedConstants.CART_OBJECT_TYPES.PRODUCT && Product.get(item.productId).productType == DomainConstants.PRODUCT_TYPE.DOWNLOADABLE}">
                            <a target="_blank" href="${app.relativeBaseUrl()}page/downloadProduct?token=${productService.getProductFileDownloadToken(item)}">
                            <g:set var="needEndingTag" value="${true}"/>
                        </g:if>
                        ${item.productName.encodeAsBMHTML()} ${item.variations ? ("(" + item.variations.join(", ").encodeAsBMHTML() + ")") : ""}
                        <g:if test="${needEndingTag}"></a></g:if>
                    </div>
                </td>
                <td class="unit-price">
                    <div class="wrapper" data-label="<g:message code="unit.price"/>:">
                        <g:if test="${item.showActualPrice}">
                            <span class="currency-symbol">${currencySymbol}</span><span class="strike-through">${item.actualPrice?.toCurrency()?.toPrice()}</span>
                        </g:if>
                        <span>${currencySymbol}${item.displayPrice.toCurrency().toPrice()}</span>
                    </div>
                </td>
                <td class="quantity">
                    <div class="wrapper" data-label="<g:message code="quantity"/>:">
                        <span> ${item.quantity}</span>
                    </div>
                </td>
                <g:if test="${config["show_tax_each_item"] == "true"}">
                    <td class="tax">
                        <div class="wrapper" data-label="<g:message code="tax"/>:">
                            <span>${currencySymbol}${item.tax.toCurrency().toPrice()}</span>
                        </div>
                    </td>
                </g:if>
                <g:if test="${config["show_discount_each_item"] == "true"}">
                    <td class="discount">
                        <div class="wrapper" data-label="<g:message code="discount"/>:">
                            <span>${currencySymbol}${item.discount.toCurrency().toPrice()}</span>
                        </div>
                    </td>
                </g:if>
                <td class="price">
                    <div class="wrapper" data-label="<g:message code="amount"/>:">
                        <span>${currencySymbol}${item.totalPriceConsideringConfiguration.toCurrency().toPrice()}</span>
                    </div>
                </td>
            </tr>
        </g:each>
    </table>
    <div class="payment-success-total">
        <table>
            <g:set var="rowSpanCount" value="${(config["show_subtotal"] == "true" ? 1 : 0) + (config["show_total_discount"] == "true" ? 1 : 0) + (config["show_sub_total_tax"] == "true" ? 1 : 0) + (config["show_shipping_cost"] == "true" ? 1 : 0) + (config["show_handling_cost"] == "true" ? 1 : 0) + (config["show_shipping_tax"] == "true" ? 1 : 0) + (config["show_payment_surcharge"] == "true" ? 1 : 0) + (order.due ? 1 : 0) + 1}"/>
            <plugin:hookTag hookPoint="changeRowSpanCount" attrs="[page: 'paymentSuccess']"/>
            <g:set var="rowSpanSet" value="${true}"/>
            <g:if test="${config["show_subtotal"] == "true"}">
                <tr class="sub-total-row">
                    <td class="total-label"><div class="wrapper"><span><g:message code="sub.total"/></span></div></td>
                    <td class="price"><div class="wrapper"><span>${currencySymbol}${order.subTotal.toCurrency().toPrice()}</span></div></td>
                </tr>
            </g:if>
            <g:if test="${config["show_total_discount"] == "true"}">
                <tr class="discount-row">
                    <td class="total-label"><div class="wrapper"><span><g:message code="discount"/></span></div></td>
                    <td class="price"> <div class="wrapper"><span>- ${currencySymbol}${order.itemsDiscount.toCurrency().toPrice()}</span></div></td>
                </tr>
            </g:if>
            <plugin:hookTag hookPoint="afterTotalDiscountRow" attrs="[page: 'paymentSuccess', rowSpanSet: rowSpanSet, rowSpanCount: rowSpanCount, totalColumn: totalColumn]"/>
            <g:if test="${config["show_sub_total_tax"] == "true"}">
                <tr class="tax-row">
                    <td class="total-label"><div class="wrapper"><span><g:message code="total.tax"/></span></div></td>
                    <td class="price"><div class="wrapper"><span>${currencySymbol}${order.totalTax.toCurrency().toPrice()}</span></div></td>
                </tr>
            </g:if>
            <g:if test="${config["show_shipping_cost"] == "true"}">
                <tr class="shipping-cost-row">
                    <td class="total-label"><div class="wrapper"><span><g:message code="shipping.cost"/></span></div></td>
                    <td class="price"><div class="wrapper"><span>${currencySymbol}${order.shippingCost.toCurrency().toPrice()}</span></div></td>
                </tr>
            </g:if>
            <g:if test="${config["show_total_discount"] == "true"}">
                <tr class="shipping-discount-row">
                    <td class="total-label"><div class="wrapper"><span><g:message code="shipping.discount"/></span></div></td>
                    <td class="price"><div class="wrapper"><span> - ${currencySymbol}${order.discountOnShipping.toCurrency().toPrice()}</span></div></td>
                </tr>
                <tr class="order-discount-row">
                    <td class="total-label"><div class="wrapper"><span><g:message code="order.discount"/></span></div></td>
                    <td class="price"><div class="wrapper"><span> - ${currencySymbol}${order.discountOnOrder.toCurrency().toPrice()}</span></div></td>
                </tr>
            </g:if>
            <g:if test="${config["show_shipping_tax"] == "true"}">
                <tr class="shipping-tax-row">
                    <td class="total-label"><div class="wrapper"><span><g:message code="shipping.tax"/></span></div></td>
                    <td class="price"><div class="wrapper"><span>${currencySymbol}${order.shippingTax.toCurrency().toPrice()}</span></div></td>
                </tr>
            </g:if>
            <g:if test="${config["show_handling_cost"] == "true"}">
                <tr class="handling-cost-row">
                    <td class="total-label"><div class="wrapper"><span><g:message code="handling.cost"/></span></div></td>
                    <td class="price"><div class="wrapper"><span>${currencySymbol}${order.handlingCost.toCurrency().toPrice()}</span></div></td>
                </tr>
            </g:if>
            <g:if test="${config["show_payment_surcharge"] == "true"}">
                <tr class="payment-surcharge-row">
                    <td class="total-label"><div class="wrapper"><span><g:message code="payment.surcharge"/></span></div></td>
                    <td class="price"><div class="wrapper"><span>${currencySymbol}${order.totalSurcharge.toCurrency().toPrice()}</span></div></td>
                </tr>
            </g:if>
            <tr class="total-order">
                <td class="total-label"><div class="wrapper"><span><g:message code="order.total"/></span></div></td>
                <td class="price">
                    <div class="wrapper">
                        <span>
                            <span class="currency-code">${currencyCode}</span>
                            ${currencySymbol}${order.grandTotal.toCurrency().toPrice()}
                       </span>
                    </div>
                </td>
            </tr>
            <plugin:hookTag hookPoint="afterOrderTotalRow" attrs="[page: 'paymentSuccess', rowSpanSet: rowSpanSet, rowSpanCount: rowSpanCount, totalColumn: totalColumn]"/>
            <g:if test="${order.due}">
                <tr class="total-order">
                    <td class="total-label"><g:message code="total.due"/></td>
                    <td class="price">
                        <div class="wrapper">
                            <span>
                                ${currencySymbol}${order.due.toCurrency().toPrice()}
                           </span>
                        </div>
                    </td>
                </tr>
            </g:if>
        </table>
    </div>

    <plugin:hookTag hookPoint="paymentSuccessAfterTable" attrs="${[order: order]}"/>
</div>
<g:set var="seoConfig" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.WEBTOOL)}"/>
<g:if test="${seoConfig?.tracking_enabled?.toBoolean() && seoConfig?.google_e_commerce_tracking?.toBoolean()}">
    <script type="text/javascript">
        ga('require', 'ecommerce', 'ecommerce.js');
        ga('ecommerce:addTransaction', {
            'id': '${order.id}',
            'affiliation': '${storeDetail.name.encodeAsJavaScript()}',
            'revenue': '${order.grandTotal}',
            'shipping': '${order.shippingCost}',
            'tax': '${order.totalTax}'
        });
        <g:each in="${order.items}" var="item">
            <g:if test="${item.productType == NamedConstants.CART_OBJECT_TYPES.PRODUCT}">
                <g:set var="product" value="${Product.findById(item.productId)}"/>
                ga('ecommerce:addItem', {
                    'id': '${order.id}',
                    'name': '${item.productName.encodeAsJavaScript()}',
                    'sku': '${product.sku}',
                    'category': '${product.parent ? product.parent.name.encodeAsJavaScript() : ""}',
                    'price': '${item.price}',
                    'quantity': '${item.quantity}'
                });
            </g:if>
        </g:each>
        ga('ecommerce:send');
    </script>
</g:if>
