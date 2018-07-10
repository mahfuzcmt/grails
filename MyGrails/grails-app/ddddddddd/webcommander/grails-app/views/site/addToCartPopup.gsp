<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.models.QuantityAdjustableCartObject; com.webcommander.constants.NamedConstants; com.webcommander.util.AppUtil" %>
<g:set var="imageSize" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE, "size")[NamedConstants.PRODUCT_IMAGE_SETTINGS.THUMBNAIL]}"/>
<div>
    <g:set var="cartObject" value="${cartdata.object}"/>
    <div class="header">
        <span class="close-popup close-icon"></span>
        <span class="status-bar-product-name">${success ? g.message(code: "added") : ""} ${" " + quantity} ${cartObject.name.encodeAsBMHTML()}</span>
        <span class="status-message"> ${success ? g.message(code: "to.your.shopping.cart") : g.message(code: "could.not.be.added.your.cart")}</span>
    </div>
    <div class="body">
        <input type="hidden" name="itemId" value="${cartdata.id}">
        <g:if test="${success}">
            <g:if test="${warningMessage}">
                <div class="message-block cart-add-warning warning-message">${warningMessage}</div>
            </g:if>
            <table cellspacing="0" cellpadding="0" border="0">
                <tr>
                    <td class="product-img product-thumb-view">
                        <g:set var="url" value="${cartObject.getImageLink(imageSize)}"/>
                        <img src="${url.encodeAsBMHTML()}" alt="${cartObject.altText.encodeAsBMHTML()}">
                    </td>
                    <td class="short-info">
                        <div class="prodict-name">${cartObject.name.encodeAsBMHTML()} ${cartdata.variations ? "(" + cartdata.variations.join(", ").encodeAsBMHTML() + ")" : ""}</div>
                        <div class="item">
                            <span><g:message code="quantity"/>:</span>
                            <g:if test="${cartObject instanceof QuantityAdjustableCartObject}">
                                <input type="text" class="product-quantity-selector text-type" value="${cartdata.quantity}" spin-min="${cartObject.product.supportedMinOrderQuantity ?: 1}" spin-max="${cartObject.product.supportedMaxOrderQuantity ?: ''}" spin-step="${cartObject.product.isMultipleOrderQuantity ? cartObject.product.multipleOfOrderQuantity : '1'}">
                            </g:if>
                            <g:else>
                                <span class="cart-item-quantity">${cartdata.quantity}</span>
                            </g:else>
                        </div>
                        <div class="current-price">
                            <span><g:message code="price"/>: </span>
                            <g:if test="${cartdata.discount > 0.001}">
                                <span class="currency-symbol">${session.currency?.symbol?:AppUtil.baseCurrency.symbol}</span><span class="cart-item-display-unit-price price-amount strike-through">${cartdata.displayUnitPrice?.toCurrency()?.toPrice()}</span>
                                <span class="currency-symbol">${session.currency?.symbol?:AppUtil.baseCurrency.symbol}</span><span class="cart-item-discounted-unit-price price-amount">${cartdata.displayDiscountedUnitPrice?.toCurrency()?.toPrice()}</span>
                            </g:if>
                            <g:else>
                                <span class="currency-symbol">${session.currency?.symbol?:AppUtil.baseCurrency.symbol}</span><span class="cart-item-display-unit-price price-amount">${cartdata.displayUnitPrice?.toCurrency()?.toPrice()}</span>
                            </g:else>
                            X
                            <span class="cart-item-quantity">${cartdata.quantity}</span>
                            =
                            <span class="currency-symbol">${session.currency?.symbol?:AppUtil.baseCurrency.symbol}</span><span class="cart-item-display-total total-amount">${cartdata.cartPageDisplayTotal?.toCurrency()?.toPrice()}</span>
                        </div>
                        <plugin:hookTag hookPoint="cartNotificationMessage" attrs="${[data: cart.selectedDiscountData]}"/>
                    </td>
                </tr>
            </table>
        </g:if>
        <g:else>
            <div class="message-block cart-add-error error-message">${errorMessage}</div>
        </g:else>
    </div>
    <div class="popup-bottom footer">
        <div class="cart-summary-header">
            <span><g:message code="your.cart.summary"/></span>
        </div>
        <div class="cart-summary">
            <span>${totalItem + " " + (totalItem > 1 ? g.message(code: "items") : g.message(code: "item"))}</span> |
            <span><g:message code="sub.total"/> <span class="currency-symbol">${session.currency?.symbol?:AppUtil.baseCurrency.symbol}</span><span class="cart-display-sub-total">${cart?.cartPageDisplaySubTotal?.toCurrency()?.toPrice() ?: 0}</span></span>
        </div>
        <div class="button-item">
            <span class="continue-shopping-btn close-btn button et_pdp_continue_shopping" et-category="button"><g:message code="continue.shopping"/></span>
            <a class="cart-page-button button et_ecommerce_view_cart" et-category="button" href="${app.relativeBaseUrl() + "cart/details"}"><g:message code="view.cart"/></a>
        </div>
    </div>
</div>