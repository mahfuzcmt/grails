<%@ page import="com.webcommander.util.AppUtil" %>
<g:applyLayout name="_widget">
    <g:if test="${config.responsive_menu == "true"}"><div class="cart-menu-button responsive-menu-btn"></div></g:if>
    <div class="content">
        <div class="cart-wrapper ${config.quick_cart == "true" ? "quick-cart" : ""}">
            <g:if test="${!cart || config.quick_cart == "false"}">
                <a href="${app.relativeBaseUrl()}cart/details" class="et_ecommerce_view_cart" et-category="button">
                </g:if>
                <span class="cart-widget-img"></span>
                <span class="cart-widget-text">${config.text.encodeAsBMHTML()}</span>
                <g:if test="${!cart || config.quick_cart == "false"}">
                </a>
            </g:if>
        </div>
        <g:if test="${cart && config.quick_cart == "true"}">
            <div class="quick-cart-content" style="display: none" tabindex="0">
                <table>
                    <colgroup>
                        <col class="product-name">
                        <col class="quantity">
                        <col class="price">
                    </colgroup>
                    <tr>
                        <th class="product-name"><g:message code="product.name"/></th>
                        <th class="quantity"><g:message code="quantity.short"/></th>
                        <th class="price"><g:message code="price"/></th>
                    </tr>
                    <g:each in="${cart.cartItemList}" var="cartItem">
                        <tr>
                            <td class="product-name">
                                ${cartItem.object.name.encodeAsBMHTML()} ${cartItem.variations ? "(" + cartItem.variations.join(", ").encodeAsBMHTML() + ")" : ""}
                            </td>
                            <td class="quantity">
                                ${cartItem.quantity}
                            </td>
                            <td class="price">
                                ${session.currency?.symbol ?: AppUtil.baseCurrency.symbol}${cartItem.cartPageDisplayTotal?.toCurrency()?.toPrice()}
                            </td>
                        </tr>
                    </g:each>
                    <tr>
                        <td class="total"><g:message code="total"/></td>
                        <td colspan="2" class="total-price">${session.currency?.symbol ?: AppUtil.baseCurrency.symbol}${cart.cartPageDisplaySubTotal?.toCurrency()?.toPrice()}</td>
                    </tr>
                </table>
                <div class="cart-details-link">
                    <plugin:hookTag hookPoint="cartNotificationMessage" attrs="${[data: cart.selectedDiscountData]}"/>
                    <a class="checkout button" href="${app.relativeBaseUrl()}shop/checkout"><g:message code="checkout"/></a>
                    <a class="view-cart button" href="${app.relativeBaseUrl()}cart/details"><g:message code="view.cart"/></a>
                </div>
            </div>
        </g:if>
    </div>
    <g:if test="${config.responsive_menu == "true"}"><style type="text/css">
        <g:if test="${hasGlobal}">#wi-${widget.uuid} .cart-menu-button {
            display: block;
        }
        #wi-${widget.uuid} .content {
            display: none;
        }
        #wi-${widget.uuid} .content.show {
            display: block;
        }</g:if>
        <g:each in="${resolutions}" var="resolution">@media ${resolution.min ? "(min-width: " + resolution.min + "px)" : ""}${resolution.max && resolution.min ? " and " : ""} ${resolution.max ? "(max-width: " + resolution.max + "px)" : ""}{
            #wi-${widget.uuid} .cart-menu-button {
                display: block;
            }
            #wi-${widget.uuid} .content {
                display: none;
            }
            #wi-${widget.uuid} .content.show {
                display: block;
            }
        }</g:each>
    </style></g:if>
</g:applyLayout>