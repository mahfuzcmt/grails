<%@ page import="com.webcommander.constants.NamedConstants; com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants"%>
<g:set var="imageSize" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE, "size")[NamedConstants.PRODUCT_IMAGE_SETTINGS.THUMBNAIL]}"/>
<div>
    <input type="hidden" name="productId" id="product-id-placeholder" value="${product.id}">
    <div class="header">
        <span class="close-popup close-icon"></span>
        <span class="title"><g:message code="${popupTitle.encodeAsBMHTML()}"/></span>
    </div>
    <div class="body">
        <table>
            <tr>
                <td class="product-img product-thumb-view">
                    <g:set var="url" value="${appResource.getSiteProductImageURL(productData: productData, imageSize: imageSize)}"/>
                    <img src="${url.encodeAsBMHTML()}" alt="${productData.altText.encodeAsBMHTML()}" configured-size="${imageSize}">
                </td>
                <td class="short-info">
                    <div class="name">${productData.name.encodeAsBMHTML()}</div>
                    <div class="quantity"><span><g:message code="quantity"/>: </span> ${quantity}</div>
                    <div class="current-price"><span><g:message code="price"/>: </span>
                        <g:if test="${productData.isAvailable}">
                            <plugin:hookTag hookPoint="incompleteDataCartPopup" attrs="[productData: productData, quantity: quantity]">
                                <span class="currency-symbol">${session.currency?.symbol ?: AppUtil.baseCurrency.symbol}</span><span class="price-amount">${productData.priceToDisplay.toCurrency().toPrice()}</span>
                                X
                                <span class="quantity">${quantity}</span>
                                =
                                <span class="currency-symbol">${session.currency?.symbol ?: AppUtil.baseCurrency.symbol}</span><span class="total-amount">${(productData.priceToDisplay * quantity).toCurrency().toPrice()}</span>
                            </plugin:hookTag>
                        </g:if>
                        <g:else>
                            <span class="product-status message-block error"><g:message code="product.not.available"/></span>
                        </g:else>
                    </div>
                </td>
            </tr>
        </table>
        <g:if test="${requiresCombination}">
            <g:include view="site/product/includedProducts.gsp"/>
        </g:if>
        <plugin:hookTag hookPoint="addCartPopup" attrs="[product: product]"/>
        <div style="display: none" class="param-dump">
            <util:paramDump/>
        </div>
    </div>
    <div class="popup-bottom footer">
         <span class="status-message"></span>
        <div class="button-item">
            <span class="add-to-cart-button button et_pdp_add_to_cart ${productData.isAvailable ? "" : "disabled"}" et-category="button" product-id="${product.id}" cart-quantity="${quantity}"><g:message code="add.to.cart"/></span>
            <span class="close-btn button"><g:message code="cancel"/></span>
        </div>
    </div>
</div>
