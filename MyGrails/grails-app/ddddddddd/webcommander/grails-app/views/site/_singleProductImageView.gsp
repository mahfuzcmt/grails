<%@ page import="com.webcommander.constants.NamedConstants; com.webcommander.util.AppUtil" %>
<div class="product-block product-view-height-width" product-id="${product.id}">
    <plugin:hookTag hookPoint="imageBlockInProductImageView" attrs="[config: config, product: product]">
        <div class="image product-image">
            <a class="product-image-link image-link link" href="${app.relativeBaseUrl() + "product/" + product.url}">
                <g:if test="${product.isOnSale}">
                    <span class="sale tag-mark"></span>
                </g:if>
                <g:if test="${product.isNew}">
                    <span class="new tag-mark"></span>
                </g:if>
                <g:if test="${product.isFeatured}">
                    <span class="featured tag-mark"></span>
                </g:if>
                <g:if test="${product.isCallForPriceEnabled}">
                    <span class="call-for-price tag-mark"></span>
                </g:if>
                <g:set var="url" value="${appResource.getSiteProductImageURL(productData: product, imageSize: imageSize)}"/>
                <img src="${url}" alt="${product.altText}">
            </a>
            <g:if test="${config.thumbnail_view != "true"}">
                <span class="btn-add-placeholder"></span>
                <g:if test="${config.show_on_hover.toBoolean() && (config["display-type"] == NamedConstants.PRODUCT_WIDGET_VIEW.IMAGE || config["display-type"] == NamedConstants.PRODUCT_WIDGET_VIEW.SCROLLABLE)}">
                    <g:render template="/site/addButtonView" model="${[config: config, product: product]}" />
                </g:if>
            </g:if>
        </div>
    </plugin:hookTag>
    <g:if test="${config.thumbnail_view != "true"}">
        <div class="block-content-wrap">
            <div class="product-name">
                <a class="product-name-link title-link link" href="${app.relativeBaseUrl() + "product/" + product.url}">${product.name.encodeAsBMHTML()}</a>
            </div>
            <g:if test="${config["description"] == "true"}">
                <div class="summary">
                    ${product.summary ? product.summary.encodeAsBMHTML() : (product.description ? product.description.textify().replaceAll("[\\n]+", "\\n").replaceAll("&nbsp;"," ").truncate(500).encodeAsBMHTML() : "")}
                </div>
            </g:if>
            <div class="price-waper">
                <g:if test="${config["isCallForPriceEnabled"]}">
                    <span class="button call-for-price"><site:message code="${config.label_for_call_for_price}"/> </span>
                </g:if>
                <g:elseif test="${config["price"] == "true" && config['is_price_restricted'] != true && !product.isCallForPriceEnabled}">
                    <plugin:hookTag hookPoint="priceBlockContainer" attrs="[productData: product, config: config]">
                        <g:if test="${product.isExpectToPay && config.expect_to_pay_price == "true"}">
                            <div class="expect-to-pay-price-container">
                                <g:set var="expectToPayPrice" value="${config.expect_to_pay_price_with_tax == 'true' ? product.expectToPayPriceWithTax : product.expectToPayPrice}"/>
                                <label class="label-for-expect-pay-price"><site:message code="${config.label_for_expect_to_pay}"/></label>
                                <span class="previous-price price">
                                    <span class="currency-symbol">${session.currency?.symbol ?: AppUtil.baseCurrency.symbol}</span><span class="price-amount">${expectToPayPrice.toCurrency().toPrice()}</span>
                                </span>
                            </div>
                        </g:if>
                        <g:if test="${product.isOnSale}">
                            <span class="previous-price price">
                                <span class="currency-symbol">${session.currency?.symbol ?: AppUtil.baseCurrency.symbol}</span><span class="price-amount">${product.previousPriceToDisplay.toCurrency().toPrice()}</span>
                            </span>
                        </g:if>
                        <div class="price-block-container" is-on-sale="${product.isOnSale}" is-expect-to-pay="${product.isExpectToPay}">
                            <label class="label-for-price"><site:message code="${config.label_for_price}"/></label>
                            <span class="current-price price">
                                <span class="currency-symbol">${session.currency?.symbol ?: AppUtil.baseCurrency.symbol}</span><span class="price-amount">${product.priceToDisplay.toCurrency().toPrice()}</span>
                                <g:if test="${product.taxMessage}">
                                    <span class="tax-message"><g:message code="${product.taxMessage.encodeAsBMHTML()}"/></span>
                                </g:if>
                            </span>
                        </div>
                    </plugin:hookTag>
                </g:elseif>
            </div>
            <g:if test="${!config.show_on_hover.toBoolean() || config["display-type"] == NamedConstants.PRODUCT_WIDGET_VIEW.LIST}">
                <g:render template="/site/addButtonView" model="${[config: config, product: product]}" />
            </g:if>
        </div>
    </g:if>
</div>