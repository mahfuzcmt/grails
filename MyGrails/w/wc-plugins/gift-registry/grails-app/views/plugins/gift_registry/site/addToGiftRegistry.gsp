<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants" %>
<g:set var="imageSize" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE, "size")[NamedConstants.PRODUCT_IMAGE_SETTINGS.THUMBNAIL]}"/>
<div>
    <div class="header">
        <span class="close-popup close-icon"></span>
        <span class="status-bar-product-name">${success ? g.message(code: "added") : ""} ${" " + quantity} ${giftItem?.product?.name?.encodeAsBMHTML()}</span>
        <span class="status-message"> ${success ? g.message(code: "to.your.gift.registry") : g.message(code: "could.not.be.added.gift.registry")}</span>
    </div>
    <div class="body">
        <g:if test="${success}">
            <g:if test="${warningMessage}">
                <div class="message-block cart-add-warning warning-message">${warningMessage}</div>
            </g:if>
            <table cellspacing="0" cellpadding="0" border="0">
                <tr>
                    <td class="product-img product-thumb-view">
                        <g:set var="url" value="${productData.getImageLink(imageSize)}"/>
                        <img src="${url.encodeAsBMHTML()}" alt="${productData.altText.encodeAsBMHTML()}">
                    </td>
                    <td class="short-info">
                        <div class="prodict-name">${productData.name.encodeAsBMHTML()} ${giftItem.variations ? "(" + giftItem.variations.join(", ").encodeAsBMHTML() + ")" : ""}</div>
                        <div class="item"><span><g:message code="quantity"/>: </span> ${giftItem.quantity}</div>
                    </td>
                </tr>
            </table>
        </g:if>
        <g:else>
            <div class="message-block cart-add-error error-message">${errorMessage}</div>
        </g:else>
    </div>
    <div class="popup-bottom footer">
        <div class="button-item">
            <span class="close-btn button"><g:message code="continue"/></span>
            <a class="button" href="${app.relativeBaseUrl() + "customer/profile?&gift_registry=true#my-list"}"><g:message code="view.gift.registry"/></a>
        </div>
    </div>
</div>