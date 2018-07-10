<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants" %>
<g:set var="imageSize" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE, "size")[NamedConstants.PRODUCT_IMAGE_SETTINGS.THUMBNAIL]}"/>
<div class='add-to-wish-list-popup'>
    <div class="header">
        <span class="close-popup close-icon"></span>
        <g:if test="${success}">
            <span class="status-bar-product-name"> ${object.name.encodeAsBMHTML()}</span>
            <span class="status-message"><g:message code="to.your.wish.list"/></span>
        </g:if>
        <g:else>
            <span class="status-message"><g:message code="add.to.wish.list.failed"/></span>
        </g:else>
    </div>
    <div class="body">
        <g:if test="${success}">
            <table cellspacing="0" cellpadding="0" border="0">
                <tr>
                    <td class="product-img product-thumb-view">
                        <g:set var="url" value="${object.getImageLink(imageSize)}"/>
                        <img src="${url.encodeAsBMHTML()}" alt="${object.altText.encodeAsBMHTML()}">
                    </td>
                    <td class="short-info">
                        <div class="prodict-name">${object.name.encodeAsBMHTML()}</div>
                        <div class="product-summery">${object.summary ? object.summary.truncate(200).encodeAsBMHTML() : (object.description ? object.description.textify().replaceAll("[\\n]+", "\\n").replaceAll("&nbsp;"," ") .truncate(200).encodeAsBMHTML(): "")}</div>
                    </td>
                </tr>
            </table>
        </g:if>
        <g:else>
            <div class="message-block cart-add-error error-message">${errorMessage ?: g.message(code: "could.not.be.added.your.wish.list")}</div>
        </g:else>
    </div>
    <div class="popup-bottom footer">
        <div class="button-item">
            <span class="continue-shopping-btn close-popup button et_pdp_continue_shopping" et-category="button"><g:message code="continue"/></span>
            <a class="cart-page-button view-wish-list button" href="${app.relativeBaseUrl() + "customer/profile?wish_list#my-list"}"><g:message code="view.wish.list"/></a>
        </div>
    </div>
</div>