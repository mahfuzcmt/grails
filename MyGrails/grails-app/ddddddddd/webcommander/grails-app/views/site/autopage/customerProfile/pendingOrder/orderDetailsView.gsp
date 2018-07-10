<%@ page import="com.webcommander.webcommerce.Product; com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants;" %>
<g:set var="configs" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CHECKOUT_PAGE)}"/>
<g:set var="taxConfigs" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.TAX)}"/>
<g:set var="imageSize" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE, "size")[NamedConstants.PRODUCT_IMAGE_SETTINGS.THUMBNAIL]}"/>
<g:set var="currencyCode" value="${AppUtil.siteCurrency.code}"/>
<g:set var="currencySymbol" value="${session.currency?.symbol ?: AppUtil.baseCurrency.symbol}"/>
<div class="panel-header">
    <div class="header-left">
        <h1><g:message code="order.details"/> #<span class="order-id">${order.id}</span</h1> <span class="order-created">(${order.created.toSiteFormat(true, false, session.timezone)})</span>
    </div>
    <div class="header-right">
        <span class="link-btn order-comment" data-id="${order.id}"><g:message code="comments"/></span>
        <span class="link-btn back-button"><g:message code="back.to.list"/></span>
    </div>
</div>
<div id="order-details">
    <div class="order-items-wrap">
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
                <th><g:message code="amount"/></th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${order.items}" var="item">
                <g:set var="productData" value="${item.productData}"/>
                <g:if test="${productData.url}">
                    <g:set var="pageLink" value="${app.relativeBaseUrl() + "product/" + productData.url}"/>
                </g:if>
                <tr>
                    <td>
                        <div class="wrapper" data-label="<g:message code="product"/>:">
                            <g:if test="${pageLink}">
                                <a href="${pageLink}">
                            </g:if>
                            <g:if test="${productData.images}">
                                <img class="product-thumb-image" src="${productData.getImageLink(imageSize)}" alt="${productData.altText.encodeAsBMHTML()}">
                            </g:if>
                            <g:else>
                                <span class="product-thumb-image ${item.itemType}-image-for-cart"></span>
                            </g:else>
                            <g:if test="${pageLink}">
                                </a>
                            </g:if>
                            <div class="details">
                                <div class="id">#${item.itemId}</div>
                                <div class="name">${item.itemName} ${item.variations ? "(" + item.variations.join(", ").encodeAsBMHTML() + ")" : ""}</div>
                                <div class="refer-this  product-share" data-product-id="${item.itemId}"><g:message code="refer.this.product"/></div>
                            </div>
                        </div>
                    </td>
                    <td><div class="wrapper" data-label="<g:message code="price"/>:"><span class="currency-symbol">${currencySymbol}</span>${(taxConfigs["show_price_with_tax"] == "true" ? (item.displayPrice) : item.price).toCurrency().toPrice()}</div></td>
                    <td><div class="wrapper" data-label="<g:message code="quantity"/>:">${item.quantity}</div></td>
                    <td><div class="wrapper" data-label="<g:message code="discount"/>:"><span class="currency-symbol">${currencySymbol}</span>${item.discount.toCurrency().toPrice()}</div></td>
                    <td><div class="wrapper" data-label="<g:message code="tax"/>:"><span class="currency-symbol">${currencySymbol}</span>${item.tax.toCurrency().toPrice()}</div></td>
                    <td><div class="wrapper" data-label="<g:message code="amount"/>:"><span class="currency-symbol">${currencySymbol}</span>${itemTotals[item].toCurrency().toPrice()}</div></td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="order-info-wrap">
        <g:if test="${configs["show_subtotal"] == "true"}">
            <div class="info-row">
                <label><g:message code="sub.total"/>:</label>
                <span class="value"><span class="currency-symbol">${session.currency?.symbol?:AppUtil.baseCurrency.symbol}</span>${order.subTotal.toCurrency().toPrice()}</span>
            </div>
        </g:if>
        <g:if test="${configs["show_total_discount"] == "true"}">
            <div class="info-row">
                <label><g:message code="discount"/>:</label>
                <span class="value"><span class="currency-symbol">${session.currency?.symbol?:AppUtil.baseCurrency.symbol}</span>${order.totalDiscount.toCurrency().toPrice()}</span>
            </div>
        </g:if>
        <plugin:hookTag hookPoint="afterTotalDiscountRow" attrs="${[page: "orderDetails"]}"/>
        <g:if test="${configs["show_sub_total_tax"] == "true"}">
            <div class="info-row">
                <label><g:message code="total.tax"/>:</label>
                <span class="value"><span class="currency-symbol">${session.currency?.symbol?:AppUtil.baseCurrency.symbol}</span>${order.items.sum {it.tax}.toCurrency().toPrice()}</span>
            </div>
        </g:if>
        <g:if test="${configs["show_shipping_cost"] == "true"}">
            <div class="info-row">
                <label><g:message code="shipping.cost"/>:</label>
                <span class="value"><span class="currency-symbol">${session.currency?.symbol?:AppUtil.baseCurrency.symbol}</span>${order.shippingCost?.toCurrency().toPrice()}</span>
            </div>
        </g:if>
        <g:if test="${configs["show_shipping_tax"] == "true"}">
            <div class="info-row">
                <label><g:message code="shipping.tax"/>:</label>
                <span class="value"><span class="currency-symbol">${session.currency?.symbol?:AppUtil.baseCurrency.symbol}</span>${order.shippingTax?.toCurrency().toPrice()}</span>
            </div>
        </g:if>
        <g:if test="${configs["show_handling_cost"] == "true"}">
            <div class="info-row">
                <label><g:message code="handling.cost"/>:</label>
                <span class="value"><span class="currency-symbol">${session.currency?.symbol?:AppUtil.baseCurrency.symbol}</span>${order.handlingCost.toCurrency().toPrice()}</span>
            </div>
        </g:if>
        <g:if test="${configs["show_payment_surcharge"] == "true"}">
            <div class="info-row">
                <label><g:message code="payment.surcharge"/>:</label>
                <span class="value"><span class="currency-symbol">${session.currency?.symbol?:AppUtil.baseCurrency.symbol}</span>${order.totalSurcharge.toCurrency().toPrice()}</span>
            </div>
        </g:if>
        <div class="info-row">
            <label><g:message code="total.amount"/>:</label>
            <span class="value"><span class="currency-code">${currencyCode}</span><span class="currency-symbol">${session.currency?.symbol?:AppUtil.baseCurrency.symbol}</span>${order.getGrandTotal().toCurrency().toPrice()}</span>
        </div>
    </div>
</div>