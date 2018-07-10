<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants" %>
<g:set var="imageSize" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE, "size")[NamedConstants.PRODUCT_IMAGE_SETTINGS.THUMBNAIL]}"/>
<g:set var="cartList" value="${cart?.cartItemList}"/>
<div class="shopping-cartitem ${cartList?.size() ? "" : "empty-cart"}">
    <div class="header-wrapper">
        <h1 class="page-heading"><site:message code="${config['page_title']}"/></h1>
    </div>
    <g:set var="currencySymbol" value="${session.currency?.symbol ?: AppUtil.baseCurrency.symbol}"/>
    <g:if test="${cartList?.size()}">
        <div class="cartitem-btn-wrapper top">
            <a class="empty-cartitem-btn cartitem-btn button et_cartp_empty_cart" et-category="button" href="${app.relativeBaseUrl() + 'cart/empty'}"><g:message code="empty.cart"/></a>
            <plugin:hookTag hookPoint="cartDetailsButton" attrs="${[:]}"/>
        </div>
        <table class="cartitem-table product-thumb-view">
            <colgroup>
                <g:set var="totalColumn" value="${5}"/>
                <col class="action-column">
                <g:if test="${config["show_product_thumbnail"] == "true"}">
                    <col class="image-column">
                    <g:set var="totalColumn" value="${++totalColumn}"/>
                </g:if>
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
            <thead>
                <tr>
                    <th></th>
                    <g:if test="${config["show_product_thumbnail"] == "true"}">
                        <th class="image"><g:message code="image"/></th>
                    </g:if>
                    <th class="product-name"><g:message code="product.name"/></th>
                    <th class="unit-price"><site:message code="${config["price_column_text"]}"/></th>
                    <th class="quantity"><g:message code="quantity"/></th>
                    <g:if test="${config["show_tax_each_item"] == "true"}">
                        <th class="tax"><site:message code="${config["show_tax_for_each_item_label"]}"/></th>
                    </g:if>
                    <g:if test="${config["show_discount_each_item"] == "true"}">
                        <th class="discount"><site:message code="${config["show_discount_for_each_item_label"]}"/></th>
                    </g:if>
                    <th class="price">
                        <site:message code="${config["item_total_label"]}"/>
                    </th>
                </tr>
            </thead>

            <g:each in="${cartList}" status="i" var="cartItem">
                <g:set var="object" value="${cartItem.object}"/>
                <g:if test="${object.hasLink}">
                    <g:set var="pageLink" value="${app.relativeBaseUrl() + object.link}"/>
                </g:if>
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'} cart-item ${cartItem.oldQuantity ? "highlighted" : ""}" ${cartItem.oldQuantity ? "old-quantity='$cartItem.oldQuantity'" : ""} item-id="${cartItem.id}">
                    <td class="remove">
                        <div class="wrapper">
                            <a class="remove-cartitem tool-icon et_cartp_remove_product" et-category="button" href="${app.relativeBaseUrl()}cart/remove?itemId=${cartItem.id}"></a>
                        </div>
                    </td>
                    <g:if test="${config["show_product_thumbnail"] == "true"}">
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
                            <g:if test="${cartItem.discountData}">
                                <div class="warning-message in-cell-message-block message-block">${cartItem.discountData.discountedMessage}</div>
                            </g:if>
                        </div>
                    </td>
                    <td class="unit-price">
                        <div class="wrapper" data-label="<site:message code="${config["price_column_text"]}"/>:">
                            <g:if test="${cartItem.showActualPrice}">
                                <span class="currency-symbol">${currencySymbol}</span><span class="strike-through">${cartItem.displayUnitPrice?.toCurrency()?.toPrice()}</span>
                            </g:if>
                            <span>${currencySymbol}${(config.price_enter_with_tax == "true" ? (cartItem.unitPrice + cartItem.unitTax) : cartItem.unitPrice).toCurrency().toPrice()}</span>
                        </div>
                    </td>
                    <td class="quantity">
                        <div class="wrapper" data-label="<g:message code="quantity"/>:">
                            <g:if test="${cartItem.isQuantityAdjustable}">
                                <input type="text" class="product-quantity-selector text-type" value="${cartItem.quantity}" spin-min="${object.supportedMinOrderQuantity ?: 1}" spin-max="${object.supportedMaxOrderQuantity ?: ''}" spin-step="${object.isMultipleOrderQuantity ? object.multipleOfOrderQuantity : '1'}" item-id="${cartItem.id}">
                            </g:if>
                            <g:else>
                                <span>${cartItem.quantity}</span>
                            </g:else>
                        </div>
                    </td>
                    <g:if test="${config["show_tax_each_item"] == "true"}">
                        <td class="tax"><div class="wrapper" data-label="<g:message code="tax"/>:"><span class="currency-symbol">${currencySymbol}</span><span class="cart-item-tax">${cartItem.tax.toCurrency().toPrice()}</span></div></td>
                    </g:if>
                    <g:if test="${config["show_discount_each_item"] == "true"}">
                        <td class="discount"><div class="wrapper" data-label="<g:message code="discount"/>:"><span class="currency-symbol">${currencySymbol}</span><span class="cart-item-discount">${cartItem.discount.toCurrency().toPrice()}</span></div></td>
                    </g:if>
                    <td class="price">
                        <div class="wrapper" data-label="<site:message code="${config["item_total_label"]}"/>:">
                            <span class="currency-symbol">${currencySymbol}<span class="cart-item-display-total">${cartItem.cartPageDisplayTotal?.toCurrency()?.toPrice()}</span></span>
                        </div>
                    </td>
                </tr>
            </g:each>
        </table>
        <div class="shopping-cart-total">
            <div class="left-column">
                <plugin:hookTag hookPoint="cartNotificationMessage" attrs="${[data: cart.selectedDiscountData]}"/>
                <plugin:hookTag hookPoint="subTotalLeftPanelCartDetails"/>
            </div>
            <div class="right-column">
                <table>
                    <tbody>
                        <g:set var="rowSpanCount" value="${(config["show_subtotal"] == "true" ? 1 : 0) + (config["show_total_discount"] == "true" ? 1 : 0) + (config["show_sub_total_tax"] == "true" ? 1 : 0) + 1}"/>
                        <plugin:hookTag hookPoint="changeRowSpanCount" attrs="[page: 'cartDetails']"/>
                        <g:set var="rowSpanSet" value="${true}"/>
                        <g:if test="${config["show_subtotal"] == "true"}">
                            <tr class="sub-total-row">
                                <td class="total-label"><div class="wrapper"><span><site:message code="${config["show_sub_total_label"]}"/></span></div></td>
                                <td class="price"><div class="wrapper"><span class="currency-symbol">${currencySymbol}</span><span class="cart-display-sub-total">${cart.cartPageDisplaySubTotal?.toCurrency()?.toPrice()}</span></div></td>
                            </tr>
                        </g:if>
                        <g:if test="${config["show_total_discount"] == "true"}">
                            <tr class="discount-row">
                                <g:if test="${rowSpanSet}">
                                    <g:set var="rowSpanSet" value="${false}"/>
                                </g:if>
                                <td class="total-label"><div class="wrapper"><span><site:message code="${config["show_total_discount_label"]}"/></span></div></td>
                                <td class="price"><div class="wrapper"><span class="currency-symbol"> - ${currencySymbol}</span><span class="cart-discount">${cart.discount.toCurrency().toPrice()}</span></div></td>
                            </tr>
                        </g:if>
                        <plugin:hookTag hookPoint="afterTotalDiscountRow" attrs="[page: 'cartDetails', rowSpanSet: rowSpanSet, rowSpanCount: rowSpanCount, totalColumn: totalColumn]"/>
                        <g:if test="${config["show_sub_total_tax"] == "true"}">
                            <tr class="tax-row">
                                <g:if test="${rowSpanSet}">
                                    <g:set var="rowSpanSet" value="${false}"/>
                                </g:if>
                                <td class="total-label"><div class="wrapper"><span><site:message code="${config["show_sub_total_tax_label"]}"/></span></div></td>
                                <td class="price"><div class="wrapper"><span class="currency-symbol">${currencySymbol}</span><span class="cart-tax">${cart.tax.toCurrency().toPrice()}</span></div></td>
                            </tr>
                        </g:if>
                        <tr class="total-order">
                            <g:if test="${rowSpanSet}">
                                <td class="empty-left-footer-block"></td>
                            </g:if>
                            <td class="total-label"><div class="wrapper"><span><g:message code="order.total"/></span></div></td>
                            <td class="price"><div class="wrapper"><span class="currency-symbol">${currencySymbol}</span><span class="cart-grand-total">${grandTotal.toCurrency().toPrice()}</span></div></td>
                        </tr>
                    </tbody>
                </table>
            </div>
            <div style="clear: both;"></div>
        </div>
        <plugin:hookTag hookPoint="cartDetailsAfterTable" attrs="${[cartList: cartList]}"/>
        <div class="cartitem-btn-wrapper">
            <g:if test="${eCommerceConfig.enable_continue_shopping == "true"}">
                <a href="${continueShoppingUrl}" class="continue-shopping-btn cartitem-btn button et_cartp_continue_shopping" et-category="button"><site:message code="${eCommerceConfig.continue_shopping_label}"/></a>
            </g:if>
            <a class="checkout-btn cartitem-btn button et_cartp_continue_shopping" et-category="button" href= "${app.relativeBaseUrl()}shop/checkout"><site:message code="${config["checkout_button_text"]}"/></a>
        </div>
    </g:if>
    <g:else>
        <div class='empty-cartitem-text'><site:message code="${config['cart_empty_message']}"/></div>
        <a href="${app.relativeBaseUrl()}" class="continue-shopping-btn cartitem-btn button et_cartp_continue_shopping" et-category="button"><g:message code="continue.shopping"/></a>
    </g:else>
</div>