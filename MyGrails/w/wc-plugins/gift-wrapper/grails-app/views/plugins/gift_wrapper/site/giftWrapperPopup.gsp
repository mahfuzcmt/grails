<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.models.QuantityAdjustableCartObject; com.webcommander.constants.NamedConstants; com.webcommander.util.AppUtil" %>
<div>
    <div class="header">
        <span class="close-popup close-icon"></span>
        <span class="status-bar-product-name"><g:message code="gift.wrappers"/></span>
    </div>

    <div class="body gift-wrapper-popup-body">
        <g:if test="${giftWrappers}">
            <g:each in="${giftWrappers}" var="giftWrapper">
                <table cellspacing="0" cellpadding="0" border="0">
                    <tr>
                        <td class="gift-wrapper-thumb-view">
                            <img src="${appResource.getGiftWrapperImageURL(image: giftWrapper, sizeOrPrefix: "400")}"/>
                        </td>
                        <td class="gift-wrapper-short-info">
                            <div class="gift-wrapper-name">${giftWrapper?.name?.encodeAsBMHTML()}</div>

                            <div class="current-price">
                                <span class="name"><g:message code="price"/>:</span>
                                <span class="currency-symbol">${session.currency?.symbol ?: AppUtil.baseCurrency.symbol}</span>
                                <span class="cart-item-display-unit-price price-amount">
                                    ${giftWrapperTL.getGiftWrapperPrice(price: giftWrapper.actualPrice, cartItem: cartItem)}
                                </span>
                                <span class="gift-wrapper-tax-message">
                                    ${config.price_enter_with_tax == "true" ? productData.taxMessage.encodeAsBMHTML() : ""}
                                </span>
                            </div>

                            <div class="gift-wrapper-description">
                                <span class="name"><g:message code="description"/>:</span>
                                <span class="description">${giftWrapper?.description?.encodeAsBMHTML()}</span>
                            </div>
                            <g:if test="${giftWrapper?.isAllowGiftMessage}">
                                <div class="gift-wrapper-message">
                                    <span class="gift-wrapper-message-label name"><g:message code="message"/>:</span>
                                    <textarea validation="maxlength[250]" maxlength="250"  type="text" class="small gift-wrapper-message-input" name="gift-wrapper-message" />
                                </div>
                            </g:if>
                        </td>
                        <td class="gift-wrapper-add-button">
                            <button class="add-gift-wrapper-to-cart" gift-wrapper-id="${giftWrapper.id}"
                                    product-id="${productId}" item-id="${cartItemId}">ADD</button>
                        </td>
                    </tr>
                </table>
            </g:each>
        </g:if>
        <g:else>
            <div class="message-block cart-add-error error-message">No Gift Wrapper Found</div>
        </g:else>
    </div>

    <div class="popup-bottom footer">

    </div>
</div>