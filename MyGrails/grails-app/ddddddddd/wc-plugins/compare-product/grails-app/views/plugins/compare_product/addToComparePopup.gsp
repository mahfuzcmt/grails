<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants" %>
<g:set var="imageSize" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE, "size")[NamedConstants.PRODUCT_IMAGE_SETTINGS.THUMBNAIL]}"/>
<g:set var="taxConfigs" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.TAX)}"/>
<div>
    <div class="header">
        <span class="close-popup close-icon"></span>
        <span class="status-message"> ${success ? g.message(code: "you.have.added.to.comparison", args: [data.name.encodeAsBMHTML()]) : g.message(code: "could.not.be.added.comparison")}</span>
    </div>
    <div class="body">
        <g:if test="${success}">
            <table cellspacing="0" cellpadding="0" border="0">
                <tr>
                    <td class="product-img product-thumb-view">
                        <div class="wrapper">
                            <g:set var="url" value="${data.getImageLink(imageSize)}"/>
                            <img src="${url.encodeAsBMHTML()}" alt="${data.altText.encodeAsBMHTML()}">
                        </div>
                    </td>
                    <td class="short-info">
                        <div class="wrapper">
                            <div class="product-name">${data.name.encodeAsBMHTML()}</div>
                            <g:if test="${data.description}">
                                <div class="product-description">${data.description.textify().truncate(150)}</div>
                            </g:if>
                            <g:if test="${!data.isPriceRestricted(AppUtil.loggedCustomer, AppUtil.loggedCustomerGroupIds)}">
                                <div class="product-price">${session.currency?.symbol?:AppUtil.baseCurrency.symbol}${(taxConfigs["show_price_with_tax"] == "true" ? (data.priceToDisplay) : data.effectivePrice).toCurrency().toPrice()}</div>
                            </g:if>
                        </div>
                    </td>
                </tr>
            </table>
        </g:if>
        <g:else>
            <div class="message-block cart-add-error error-message"><g:message code="${error}"/></div>
        </g:else>
    </div>
    <div class="popup-bottom footer">
        <div class="button-item">
            <span class="continue-btn close-popup button"><g:message code="continue"/></span>
            <g:if test="${session.compare.size() > 1}">
                <a class="compare-page-button button" href="${app.relativeBaseUrl() + "compareProduct/details"}"><g:message code="compare"/></a>

            </g:if>
        </div>
    </div>
</div>