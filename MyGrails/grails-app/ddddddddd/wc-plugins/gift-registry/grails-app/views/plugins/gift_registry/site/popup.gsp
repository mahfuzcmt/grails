<%@ page import="com.webcommander.plugin.gift_registry.GiftRegistry; com.webcommander.constants.NamedConstants; com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants"%>
<g:set var="imageSize" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE, "size")[NamedConstants.PRODUCT_IMAGE_SETTINGS.THUMBNAIL]}"/>
<div>
    <input type="hidden" name="productId" id="product-id-placeholder" value="${product.id}">
    <div class="header">
        <span class="close-popup close-icon"></span>
        <span class="title"><g:message code="${popupTitle.encodeAsBMHTML()}"/></span>
    </div>
    <div class="body">
        <g:if test="${count > 0}">
            <table>
                <tr>
                    <td class="product-img product-thumb-view">
                        <g:set var="url" value="${productData.getImageLink(imageSize)}"/>
                        <img src="${url.encodeAsBMHTML()}" alt="${productData.altText.encodeAsBMHTML()}" configured-size="${imageSize}">
                    </td>
                    <td class="short-info">
                        <div class="name">${productData.name.encodeAsBMHTML()}</div>
                        <div class="quantity"><span><g:message code="quantity"/>: </span> ${quantity}</div>
                        <g:if test="${!productData.isPriceRestricted(AppUtil.loggedCustomer, AppUtil.loggedCustomerGroupIds)}">
                            <div class="current-price"><span><g:message code="price"/>: </span>
                                <span class="currency-symbol">${session.currency?.symbol?:AppUtil.baseCurrency.symbol}</span><span class="price-amount">${productData.priceToDisplay.toCurrency().toPrice()}</span>
                                X
                                <span class="quantity">${quantity}</span>
                                =
                                <span class="currency-symbol">${session.currency?.symbol?:AppUtil.baseCurrency.symbol}</span><span class="total-amount">${(productData.priceToDisplay * quantity).toCurrency().toPrice()}</span>
                            </div>
                        </g:if>
                    </td>
                </tr>
            </table>
            <g:if test="${requiresGiftRegistry}">
                <div class="form-row">
                    <label><g:message code="select.gift.registry"/>:</label>
                    <ui:domainSelect filter="${{eq("customer", customer)}}" name="giftRegistry" domain="${GiftRegistry}" text="eventName"/>
                </div>
            </g:if>
            <div style="display: none" class="param-dump">
                <util:paramDump/>
            </div>
        </g:if>
        <g:else>
            <div class="no-wish-list-message"><g:message code="no.gift.registry.found"/> </div>
        </g:else>

    </div>
    <div class="popup-bottom footer">
        <g:if test="${count <= 0}">
            <button class="submit-button create-gift-registry"><g:message code="create"/></button>
        </g:if>
        <g:else>
            <button class="submit-button add-to-gift-registry" product-id="${product.id}" quantity="${quantity}"><g:message code="add"/></button>
        </g:else>
        <button class="form-reset close-popup" onclick="return false"><g:message code="cancel"/></button>
    </div>
</div>
