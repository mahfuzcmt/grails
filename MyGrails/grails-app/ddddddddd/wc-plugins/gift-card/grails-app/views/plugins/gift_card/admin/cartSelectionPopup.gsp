<%@ page import="com.webcommander.converter.json.JSON; com.webcommander.constants.NamedConstants; com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants" %>
<g:set var="imageSize" value="150"/>
<g:set var="quantity" value="${1}"/>
<form action="#" class="edit-popup-form" method="post">
    <table>
        <tr>
            <td class="product-img product-thumb-view">
                <g:set var="url" value="${productData.getImageLink(imageSize)}"/>
                <img src="${url.encodeAsBMHTML()}" alt="${productData.altText.encodeAsBMHTML()}" configured-size="${imageSize}">
            </td>
            <td class="short-info">
                <div class="name">${productData.name.encodeAsBMHTML()}</div>
                <div class="quantity"><span><g:message code="quantity"/>: </span> ${quantity}</div>
                <div class="current-price"><span><g:message code="price"/>: </span>
                    <g:if test="${productData.isAvailable}">
                        <g:set var="price" value="${productData.isOnSale ? productData.salePrice : productData.basePrice}"/>
                        <span class="currency-symbol">${session.currency?.symbol ?: AppUtil.baseCurrency.symbol}</span><span class="price-amount">${price.toCurrency().toPrice()}</span>
                        X
                        <span class="quantity">${quantity}</span>
                        =
                        <span class="currency-symbol">${session.currency?.symbol ?: AppUtil.baseCurrency.symbol}</span><span class="total-amount">${(price * quantity).toCurrency().toPrice()}</span>
                    </g:if>
                    <g:else>
                        <span class="product-status message-block error"><g:message code="product.not.available"/></span>
                    </g:else>
                </div>
            </td>
        </tr>
    </table>
    <g:include view="plugins/gift_card/giftCardExtraFields.gsp" model="[cardData: [:]]"/>
    <div class="button-line">
        <span class="status-message"></span>
        <div class="button-item">
            <button type="submit" class="add-to-cart-button submit-button" ${productData.isAvailable ? "" : "disabled"} sku="${productData.sku}" stock="${productData.availableStock}"
                ${productData.isInventoryEnabled ? "spin-step='1' spin-min='${productData.minOrderQuantity ?: 1}' spin-max='${productData.maxOrderQuantity ?: productData.availableStock}'" : ""}><g:message code="add.to.cart"/></button>
            <button type="button" class="cancel-button"><g:message code="cancel"/></button>
        </div>
    </div>
</form>