<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<g:set var="currencySymbol" value="${AppUtil.siteCurrency.symbol}"/>
<g:set var="currencyCode" value="${AppUtil.siteCurrency.code}"/>
<form class="confirm-step-form" action="${app.relativeBaseUrl()}shop/payment" method="post">
    <g:if test="${error}">
        <div class="message-block error">${error}</div>
    </g:if>
    <plugin:hookTag hookPoint="confirmOrderStart"/>
    <table class="cartitem-table product-thumb-view">
        <colgroup>
            <g:if test="${configs["show_product_thumbnail"] == "true"}">
                <col class="image-column">
            </g:if>
            <col class="product-name-column">
            <col class="unit-price-column">
            <col class="quantity-column">
            <g:if test="${configs["show_tax_each_item"] == "true"}">
                <col class="tax-column">
            </g:if>
            <g:if test="${configs["show_discount_each_item"] == "true"}">
                <col class="discount-column">
            </g:if>
            <col class="total-price-column">
        </colgroup>
        <thead>
        <tr>
            <g:if test="${configs["show_product_thumbnail"] == "true"}">
                <th class="image"><g:message code="image"/></th>
            </g:if>
            <th class="product-name"><g:message code="product.name"/></th>
            <th class="unit-price"><site:message code="${configs["price_column_text"]}"/></th>
            <th class="quantity"><g:message code="quantity"/></th>
            <g:if test="${configs["show_tax_each_item"] == "true"}">
                <th class="tax"><site:message code="${configs["show_tax_for_each_item_label"]}"/></th>
            </g:if>
            <g:if test="${configs["show_discount_each_item"] == "true"}">
                <th class="discount"><site:message code="${configs["show_discount_for_each_item_label"]}"/></th>
            </g:if>
            <th class="price">
                <site:message code="${configs["item_total_label"]}"/>
            </th>
        </tr>
        </thead>

        <g:each in="${cart.cartItemList}" status="i" var="cartItem">
            <g:set var="object" value="${cartItem.object}"/>
            <g:if test="${object.hasLink}">
                <g:set var="pageLink" value="${app.relativeBaseUrl() + object.link}"/>
            </g:if>
            <tr class="${(i % 2) == 0 ? 'odd' : 'even'} cart-item ${cartItem.oldQuantity ? "highlighted" : ""}" ${cartItem.oldQuantity ? "old-quantity='$cartItem.oldQuantity'" : ""} item-id="${cartItem.id}">
                <g:if test="${configs["show_product_thumbnail"] == "true"}">
                    <td class="image">
                        <div class="wrapper">
                            <g:if test="${pageLink}">
                                <a href="${pageLink}">
                            </g:if>
                            <g:if test="${object.hasImage}">
                                <img class="product-thumb-image" src="${object.getImageLink(imageSize)}" alt="${object.altText.encodeAsBMHTML()}">
                            </g:if>
                            <g:else>
                                <span class="product-thumb-image ${object.type}-image-for-cart"></span>
                            </g:else>
                            <g:if test="${pageLink}">
                                </a>
                            </g:if>
                        </div>
                    </td>
                </g:if>
                <td class="product-name">
                    <div class="wrapper" data-label="<g:message code="product.name"/>:">
                        <g:if test="${pageLink}">
                            <a href="${pageLink}">
                        </g:if>
                        ${object.name.encodeAsBMHTML()} ${cartItem.variations ? "(" + cartItem.variations.join(", ").encodeAsBMHTML() + ")" : ""}
                        <g:if test="${pageLink}">
                            </a>
                        </g:if>
                        <g:if test="${request["warning-" + cartItem.id]}">
                            <div class='warning-message in-cell-message-block message-block'>${request["warning-" + cartItem.id]}</div>
                        </g:if>
                        <g:if test="${request["error-" + cartItem.id]}">
                            <div class='error-message in-cell-message-block message-block'>${request["error-" + cartItem.id]}</div>
                        </g:if>
                    </div>
                </td>
                <td class="unit-price">
                    <div class="wrapper" data-label="<site:message code="${configs["price_column_text"]}"/>:">
                        <g:if test="${cartItem.showActualPrice}">
                            <span class="currency-symbol">${currencySymbol}</span><span class="strike-through">${cartItem.displayUnitPrice?.toCurrency()?.toPrice()}</span>
                        </g:if>
                        <span>${currencySymbol}${(configs.price_enter_with_tax == "true" ? (cartItem.unitPrice + cartItem.unitTax) : cartItem.unitPrice).toCurrency().toPrice()}</span>
                    </div>
                </td>
                <td class="quantity">
                    <div class="wrapper" data-label="<g:message code="quantity"/>:">
                        <g:if test="${cartItem.isQuantityAdjustable}">
                            <span>${cartItem.quantity}</span
                        </g:if>
                        <g:else>
                            <span>${cartItem.quantity}</span>
                        </g:else>
                    </div>
                </td>
                <g:if test="${configs["show_tax_each_item"] == "true"}">
                    <td class="tax"><div class="wrapper" data-label="<g:message code="tax"/>:"><span class="currency-symbol">${currencySymbol}</span><span class="cart-item-tax">${cartItem.tax.toCurrency().toPrice()}</span></div></td>
                </g:if>
                <g:if test="${configs["show_discount_each_item"] == "true"}">
                    <td class="discount"><div class="wrapper" data-label="<g:message code="discount"/>:"><span class="currency-symbol">${currencySymbol}</span><span class="cart-item-discount">${cartItem.discount.toCurrency().toPrice()}</span></div></td>
                </g:if>
                <td class="price">
                    <div class="wrapper" data-label="<site:message code="${configs["item_total_label"]}"/>:">
                        <span class="currency-symbol">${currencySymbol}<span class="cart-item-display-total">${cartItem.cartPageDisplayTotal?.toCurrency()?.toPrice()}</span></span>
                    </div>
                </td>
            </tr>
        </g:each>
    </table>

    <div class="panel-wrap">
        <div class="left-panel">
            <plugin:hookTag hookPoint="cartNotificationMessage" attrs="${[data: cart.selectedDiscountData]}"/>
            <div class="payment-options">
                <plugin:hookTag hookPoint="checkoutPaymentOption">
                    <g:set var="storeCreditPayment" value="${defaultPayments.find { it.identifier == "storeCredit" }}"/>
                    <g:if test="${storeCreditPayment && cart.deliveryType != DomainConstants.ORDER_DELIVERY_TYPE.OTHERS_SHIPPING}">
                        <div class="payment-option collapsible">
                            <div class="header"><g:message code="store.credit"/></div>
                            <div class="body row">
                                <div class="info"><g:message code="you.have.x.to.spend" args="[AppUtil.siteCurrency.symbol + customer.storeCredit.toPrice()]"/></div>
                                <div class="price-row">
                                    <span class="label"><g:message code="use.for.this.order"/></span>
                                    <input type="text" class="default-payment-amount" name="storeCreditPayment" value="${storeCreditPayment.amount}" restrict="decimal"/>
                                </div>
                            </div>
                        </div>
                    </g:if>
                </plugin:hookTag>
            </div>
            <g:if test="${enableComment == "true"}">
                <div class="order-comment-row">
                    <span class="label"><g:message code="comment"/>:</span>
                    <textarea class="medium" name="comment" maxlength="2000" validation="maxlength[2000]">${session.order_comment?.encodeAsBMHTML}</textarea>
                </div>
            </g:if>
            <g:if test="${cart.deliveryType != DomainConstants.ORDER_DELIVERY_TYPE.OTHERS_SHIPPING}">
                <div class="payment-gateway">
                    <span class="payment-gateway-label"><g:message code="payment.method"/></span>
                    <ui:namedSelect class="payment-method" name="paymentMethod" key="${gateways}" value="${selected}"/>
                </div>
                <g:if test="${selected == "CRD" && enableWallet && wallets.size() > 1}">
                    <div class="token-payment payment-method-CRD">
                        <span class="payment-gateway-label"><g:message code="payment.wallet"/></span>
                        <ui:namedSelect name="walletPayment" key="${wallets}"/>
                    </div>
                </g:if>
            </g:if>
        </div>

        <div class="right-panel">
            <div class="check-out-total">
                <g:if test="${configs["show_subtotal"] == "true"}">
                    <div class="price-row sub-price-row">
                        <span class="label"><site:message code="${configs["show_sub_total_label"]}"/></span>
                        <span class="price">${currencySymbol}${cart.checkoutPageDisplaySubTotal.toCurrency().toPrice()}</span>
                    </div>
                </g:if>
                <g:if test="${configs["show_total_discount"] == "true"}">
                    <div class="price-row discount-row">
                        <span class="label"><site:message code="${configs["show_total_discount_label"]}"/></span>
                        <span class="price">- ${currencySymbol}${cart.discount.toCurrency().toPrice()}</span>
                    </div>
                </g:if>
                <g:if test="${configs["show_sub_total_tax"] == "true"}">
                    <div class="price-row tax-row">
                        <span class="label"><site:message code="${configs["show_sub_total_tax_label"]}"/></span>
                        <span class="price">${currencySymbol}${cart.tax.toCurrency().toPrice()}</span>
                    </div>
                </g:if>
                <g:if test="${configs["show_shipping_cost"] == "true"}">
                    <div class="price-row shipping-cost-row">
                        <span class="label"><site:message code="${configs["show_shipping_cost_label"]}"/></span>
                        <span class="price">${currencySymbol}${(shippingMap.shipping ?: 0.0).toCurrency().toPrice()}</span>
                    </div>
                </g:if>
                <g:if test="${configs["show_shipping_discount"] == "true"}">
                    <div class="price-row shipping-discount-row">
                        <span class="label"><site:message code="${configs["show_shipping_discount_label"]}"/></span>
                        <span class="price"> - ${currencySymbol}${cart.discountOnShipping.toCurrency().toPrice()}</span>
                    </div>
                </g:if>
                <g:if test="${configs["show_shipping_tax"] == "true"}">
                    <div class="price-row shipping-tax-row">
                        <g:set var="shippingTax" value="${configs.show_handling_tax == "true" ? shippingMap.shippingTax : shippingMap.tax}"/>
                        <span class="label"><site:message code="${configs["show_shipping_tax_label"]}"/></span>
                        <span class="price">${currencySymbol}${(shippingTax ?: 0.0).toCurrency().toPrice()}</span>
                    </div>
                </g:if>
                <g:if test="${configs["show_shipping_and_handling_cost_separately"] == "true"}">
                    <div class="price-row handling-cost-row">
                        <span class="label"><site:message code="${configs["show_handling_cost_label"]}"/></span>
                        <span class="price">${currencySymbol}${(shippingMap.handling ?: 0.0).toCurrency().toPrice()}</span>
                    </div>
                </g:if>
                <g:if test="${configs.show_handling_tax == "true"}">
                    <div class="price-row handling-tax-row">
                        <span class="label"><site:message code="${configs["show_handling_tax_label"]}"/></span></span>
                        <span class="price">${currencySymbol}${(shippingMap.handlingTax ?: 0.0).toCurrency().toPrice()}</span>
                    </div>
                </g:if>
                <g:if test="${configs["show_payment_surcharge"] == "true"}">
                    <div class="price-row payment-surcharge-row">
                        <span class="label"><g:message code="payment.surcharge"/></span>
                        <span class="price">${currencySymbol}${surcharge.toCurrency().toPrice()}</span>
                    </div>
                </g:if>
                <div class="price-row total-order">
                    <span class="label"><g:message code="order.total"/></span>
                    <span class="price">
                        <span class="currency-code">${currencyCode}</span>
                        ${currencySymbol}${grandTotal.toCurrency().toPrice()}
                    </span>
                </div>
                <g:each in="${defaultPayments}" var="payment">
                    <div class="price-row default-payment ${payment.identifier}">
                        <span class="label ${payment.identifier}-label"><g:message code="${payment.name}"/></span>
                        <span class="price editable" name="${payment.identifier}"  max="${payment.max}">
                            <span class="currency-symbol">${currencySymbol}</span>
                            <span class="value">${payment.amount.toCurrency().toPrice()}</span>
                            <span class="tool-icon remove" data-name="${payment.identifier}Payment"></span>
                        </span>
                    </div>
                </g:each>

                <g:if test="${defaultPayments.size()}">
                    <div class="price-row due-order">
                        <span class="label due-label"><g:message code="due"/></span>
                        <span class="price">${currencySymbol}${due.toCurrency().toPrice()}</span>
                    </div>
                </g:if>
                <plugin:hookTag hookPoint="afterOrderTotalRow" attrs="[page: 'checkoutConfirmStep']"/>
            </div>
        </div>
    </div>
    <g:if test="${configs.terms_and_condition == 'on'}">
        <g:set var="type" value="${configs.terms_and_condition_type}"/>
        <g:if test="${type == DomainConstants.TERMS_AND_CONDITION_TYPE.SPECIFIC_TEXT}">
            <div class="terms-and-condition-text">
                <textarea readonly>${configs.terms_and_condition_ref}</textarea>
            </div>
        </g:if>
        <div class="terms-and-condition">
            <input type="checkbox" name="termsAndCondition" value="true" validation="required">
            <span>${termsMessage}</span>
        </div>
    </g:if>
    <plugin:hookTag hookPoint="confirmOrderEnd"/>
    <g:if test="${isValidAmount == true}">
        <div class="confirm-order-btn-row bottom">
            <input type="hidden" value="true" name="confirmed">
            <input type="submit" value="${site.message(code: configs.confirm_order_button_text)}" class="button confirm-order-button et_orderp_confirm_order" et-category="button">
        </div>
    </g:if>
</form>