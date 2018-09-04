<%@ page import="com.webcommander.constants.NamedConstants; com.webcommander.webcommerce.Product; com.webcommander.util.AppUtil" %>
<div class="embedded-order-details">
    <div class="order-details-container">
        <div class="order-information">
            <span class="header"><g:message code="order.information"/></span>
            <div class="order-row-detail-content">
                <div class="details-row">
                    <label><g:message code="order.id"/></label>
                    <span>${order.id}</span>
                </div>
                <div class="details-row">
                    <label><g:message code="customer"/></label>
                    <span>
                        <g:if test="${order.customerId == null}">
                            <g:message code="guest.customer" />
                        </g:if>
                        <g:else>
                            ${order.customerName.encodeAsBMHTML()}
                        </g:else>
                    </span>
                </div>
                <div class="details-row">
                    <label><g:message code="order.total"/></label>
                    <span>${order.grandTotal.toAdminPrice()}</span>
                </div>
                <div class="details-row">
                    <label><g:message code="order.date"/></label>
                    <span>${order.created.toAdminFormat(true, false, session.timezone)}</span>
                </div>
                <div class="details-row">
                    <label><g:message code="order.status"/></label>
                    <span><g:message code="${order.orderStatus}"/></span>
                </div>
                <g:if test="${order.ipAddress}">
                    <div class="details-row">
                        <label><g:message code="ip"/></label>
                        <span>${order.ipAddress}</span>
                    </div>
                </g:if>
            </div>
        </div>
        <g:if test="${order.items.find { it.isShippable}}">
            <div class="shipping-details">
                <div class="header-wrapper">
                    <span class="header"><g:message code="shipping.details"/></span>
                    <g:if test="${order.shipping}">
                        <button class="edit-shipping-address btn" id="${order.id}">Edit</button>
                    </g:if>
                </div>
                <div class="order-row-detail-content">
                    <g:if test="${order.shipping}">
                        <div class="address-details">
                            <g:set var="shippingAddress" value="${order.shipping}"/>
                            <span class="name">${shippingAddress.firstName.encodeAsBMHTML() + " " + (shippingAddress.lastName ?: '')}</span>
                            <span class="address-line">${shippingAddress.addressLine1.encodeAsBMHTML() + (shippingAddress.addressLine2 ? (", " + shippingAddress.addressLine2.encodeAsBMHTML()) : "")}</span>
                            <span class="city">${shippingAddress.city? shippingAddress.city.encodeAsBMHTML() : ""}</span>
                            <span class="state">${shippingAddress.state?.code? ", " + shippingAddress.state.code.encodeAsBMHTML() : ""}</span>
                            <span class="post-code">${shippingAddress.postCode? ", " + shippingAddress.postCode : ""}</span>
                            <span class="country">${shippingAddress.country?.name.encodeAsBMHTML()}</span>
                            <span class="email">${shippingAddress.email}</span>
                            <g:if test="${shippingAddress.phone}">
                                <div class="details-row">
                                    <label><g:message code="phone"/></label>
                                    <span>${shippingAddress.phone}</span>
                                </div>
                            </g:if>
                            <g:if test="${shippingAddress.mobile}">
                                <div class="details-row">
                                    <label><g:message code="mobile"/></label>
                                    <span>${shippingAddress.mobile}</span>
                                </div>
                            </g:if>
                            <g:if test="${shippingAddress.fax}">
                                <div class="details-row">
                                    <label><g:message code="fax"/></label>
                                    <span>${shippingAddress.fax}</span>
                                </div>
                            </g:if>
                        </div>
                    </g:if>
                    <div class="details-row">
                        <label><g:message code="delivery.type"/></label>
                        <span><g:message code="${NamedConstants.ORDER_DELIVERY_TYPE[order.deliveryType]}"/> </span>
                    </div>
                    <div class="details-row">
                        <label><g:message code="shipping.cost"/></label>
                        <span>${order.shippingCost.toAdminPrice()}</span>
                    </div>
                    <div class="details-row">
                        <label><g:message code="shipping.status"/></label>
                        <span>${g.message(code: order.shippingStatus)}</span>
                    </div>
                </div>
            </div>
        </g:if>

        <div class="billing-details">
            <div class="header-wrapper">
                <span class="header"><g:message code="billing.details"/></span>
                <button class="edit-billing-address btn" id="${order.id}">Edit</button>
            </div>
            <div class="order-row-detail-content">
                <div class="address-details">
                    <g:set var="billingAddress" value="${order.billing}"/>
                    <span class="name">${billingAddress.firstName + " " + (billingAddress.lastName ?: "")}</span>
                    <span class="address-line">${billingAddress.addressLine1 + (billingAddress.addressLine2 ? (", " + billingAddress.addressLine2) : "")}</span>
                    <span class="city">${billingAddress.city? billingAddress.city : ""}</span>
                    <span class="state">${billingAddress.state?.code? ", " + billingAddress.state.code : ""}</span>
                    <span class="post-code">${billingAddress.postCode? ", " + billingAddress.postCode : ""}</span>
                    <span class="country">${billingAddress.country?.name.encodeAsBMHTML()}</span>
                    <span class="email">${billingAddress.email}</span>
                    <g:if test="${billingAddress.phone}">
                        <div class="details-row">
                            <label><g:message code="phone"/></label>
                            <span>${billingAddress.phone}</span>
                        </div>
                    </g:if>
                    <g:if test="${billingAddress.mobile}">
                        <div class="details-row">
                            <label><g:message code="mobile"/></label>
                            <span>${billingAddress.mobile}</span>
                        </div>
                    </g:if>
                    <g:if test="${billingAddress.fax}">
                        <div class="details-row">
                            <label><g:message code="fax"/></label>
                            <span>${billingAddress.fax}</span>
                        </div>
                    </g:if>
                    <div class="details-row">
                        <label><g:message code="payment.status"/></label>
                        <span>${g.message(code:  order.paymentStatus)}</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="order-details-container">
        <div class="product-details">
            <span class="header"><g:message code="order.details"/></span>
            <div class="order-row-detail-content">
                <table>
                    <g:each in="${order.items}" var="orderItem">
                        <tr>
                            <td class="detail-label-cell">
                                <span class="product-sku">${orderItem.itemNumber} - </span>
                                <span class="product-name">${orderItem.productName.encodeAsBMHTML()} <plugin:hookTag hookPoint="orderShortDetailsVariation">${orderItem.variations ? "(" + orderItem.variations.join(", ").encodeAsBMHTML() + ")" : ""}</plugin:hookTag></span> X
                                <span class="quantity">${orderItem.quantity}</span>
                                <plugin:hookTag hookPoint="orderShortDetailsProductNameRow" attrs="[orderItem: orderItem]"/>
                            </td>
                            <td class="price-value">
                                <span class="currency-symbol">${AppUtil.baseCurrency.symbol}</span>
                                <span class="value">${orderItem.totalPriceConsideringConfiguration.toAdminPrice()}</span>
                            </td>
                        </tr>
                    </g:each>
                    <tfoot>
                        <tr>
                            <td class="detail-label-cell"><g:message code="sub.total"/></td>
                            <td class="price-value">
                                <span class="currency-symbol">${AppUtil.baseCurrency.symbol}</span>
                                <span class="value">${order.subTotal.toAdminPrice()}</span>
                            </td>
                        </tr>
                        <tr>
                            <td class="detail-label-cell"><g:message code="total.tax"/></td>
                            <td class="price-value">
                                <span class="currency-symbol">${AppUtil.baseCurrency.symbol}</span>
                                <span class="value">${order.totalTax.toAdminPrice()}</span>
                            </td>
                        </tr>
                        <tr>
                            <td class="detail-label-cell"><g:message code="shipping.cost"/></td>
                            <td class="price-value">
                                <span class="currency-symbol">${AppUtil.baseCurrency.symbol}</span>
                                <span class="value">${order.shippingCost?.toAdminPrice()}</span>
                            </td>
                        </tr>
                        <tr>
                            <td class="detail-label-cell"><g:message code="shipping.tax"/></td>
                            <td class="price-value">
                                <span class="currency-symbol">${AppUtil.baseCurrency.symbol}</span>
                                <span class="value">${order.shippingTax?.toAdminPrice()}</span>
                            </td>
                        </tr>
                        <tr>
                            <td class="detail-label-cell"><g:message code="handling.cost"/></td>
                            <td class="price-value">
                                <span class="currency-symbol">${AppUtil.baseCurrency.symbol}</span>
                                <span class="value">${order.handlingCost.toAdminPrice()}</span>
                            </td>
                        </tr>
                        <tr>
                            <td class="detail-label-cell"><g:message code="discount"/></td>
                            <td class="price-value">
                                <span class="currency-symbol"> - ${AppUtil.baseCurrency.symbol}</span>
                                <span class="value">${order.totalDiscount.toAdminPrice()}</span>
                            </td>
                        </tr>
                        <tr>
                            <td class="detail-label-cell"><g:message code="surcharge"/></td>
                            <td class="price-value">
                                <span class="currency-symbol">${AppUtil.baseCurrency.symbol}</span>
                                <span class="value">${order.totalSurcharge.toAdminPrice()}</span>
                            </td>
                        </tr>
                        <plugin:hookTag hookPoint="afterTotalDiscountRow" attrs="[page: 'orderDetailsRow']"/>
                        <tr>
                            <td class="detail-label-cell"><g:message code="grand.total"/></td>
                            <td class="price-value">
                                <span class="currency-symbol">${AppUtil.baseCurrency.symbol}</span>
                                <span class="value">${order.grandTotal.toAdminPrice()}</span>
                            </td>
                        </tr>
                    </tfoot>
                </table>
            </div>
        </div>
        <plugin:hookTag hookPoint="orderDetailsRowEnd"/>
    </div>
</div>