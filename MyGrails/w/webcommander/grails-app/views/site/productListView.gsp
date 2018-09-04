<%@ page import="com.webcommander.manager.HookManager; com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants; com.webcommander.util.AppUtil" %>
<g:set var="imageSize" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE, "size")[NamedConstants.PRODUCT_IMAGE_SETTINGS.LISTVIEW]}"/>
<g:set var="imageConfig" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE)}"/>
<table cellspacing="0" cellpadding="0" border="0">
    <colgroup>
        <col class="product-image product-list-view-width">
        <col class="product-description">
        <col class="product-price">
    </colgroup>
    <g:each in="${productList}" var="product" status="i">
        <tr class="${(i + 1) % 2 ? 'odd' : 'even'} product-block" product-id="${product.id}">
            <td class="product-list-view-image product-image">
                <plugin:hookTag hookPoint="productListImageCol" attrs="${[product: product]}">
                    <div class="product-list-view-height">
                        <a class="product-image-link image-link link" href="${app.relativeBaseUrl() + "product/" + product.url}">
                            <g:set var="url" value="${appResource.getSiteProductImageURL(productData: product, imageSize: imageSize)}"/>
                            <img src="${url}" alt="${product.altText}">
                        </a>
                        <g:if test="${config.show_on_hover.toBoolean()}">
                            <g:render template="/site/addButtonView" model="${[config: config, product: product]}" />
                        </g:if>
                    </div>
                </plugin:hookTag>
            </td>
            <td class="product-description">
                <h3><a class="product-name-link title-link link" href="${app.relativeBaseUrl() + "product/" + product.url}">${product.name.encodeAsBMHTML()}</a></h3>
                <g:if test="${config.description == "true"}">
                    <div class="description">
                        ${product.description ? product.description.textify().truncate(400).encodeAsBMHTML() : product.summary ? product.summary.encodeAsBMHTML() : ""}
                    </div>
                </g:if>
                <a class="product-details-link link" href="${app.relativeBaseUrl() + "product/" + product.url}"><g:message code="view.details"/></a>
            </td>
            <td class="product-price">
                <plugin:hookTag hookPoint="productListPriceCol"  attrs="[config: config, product: product]">
                    <div class="price-n-cart">
                        <g:if test="${config["price"] == "true"}">
                            <g:if test="${!product.isCallForPriceEnabled}">
                                <g:if test="${product.isExpectToPay && config.expect_to_pay_price == "true"}">
                                    <div class="expect-to-pay-price-container">
                                        <g:set var="expectToPayPrice" value="${config.expect_to_pay_price_with_tax == 'true' ? product.expectToPayPriceWithTax : product.expectToPayPrice}"/>
                                        <label class="label-for-expect-pay-price"><site:message code="${config.label_for_expect_to_pay}"/></label>
                                        <span class="previous-price price">
                                            <span class="currency-symbol">${session.currency?.symbol?:AppUtil.baseCurrency.symbol}</span><span class="price-amount">${expectToPayPrice.toCurrency().toPrice()}</span>
                                        </span>
                                    </div>
                                </g:if>
                                <g:if test="${product.isOnSale}">
                                    <span class="previous-price price">
                                        <span class="currency-symbol">${session.currency?.symbol?:AppUtil.baseCurrency.symbol}</span><span class="price-amount">${product.previousPriceToDisplay.toCurrency().toPrice()}</span>
                                    </span>
                                </g:if>
                                <div class="price-block-container" is-on-sale="${product.isOnSale}" is-expect-to-pay="${product.isExpectToPay}">
                                    <label class="label-for-price"><site:message code="${config.label_for_price}"/></label>
                                    <span class="current-price price">
                                        <span class="currency-symbol">${session.currency?.symbol?:AppUtil.baseCurrency.symbol}</span><span class="price-amount">${product.priceToDisplay.toCurrency().toPrice()}</span>
                                        <g:if test="${product.taxMessage}">
                                            <span class="tax-message"><g:message code="${product.taxMessage.encodeAsBMHTML()}"/></span>
                                        </g:if>
                                    </span>
                                </div>
                            </g:if>
                        </g:if>
                        <g:if test="${config["add_to_cart"] == "true"}">
                            <g:if test="${product.isCallForPriceEnabled}">
                                <span class="button call-for-price"><site:message code="${config.label_for_call_for_price}"/> </span>
                            </g:if>
                            <g:else>
                                <g:set var="productQuantity" value="${product.supportedMinOrderQuantity}"/>
                                <g:if test="${config["quantity_selector"] == "true"}">
                                    <g:set var="productQuantity" value="${HookManager.hook("productListViewCustomQuantity", product.supportedMinOrderQuantity, product)}"/>
                                    <input type="text" class="product-quantity-selector text-type" value="${productQuantity}" spin-min="${product.supportedMinOrderQuantity ?: 1}"
                                           spin-max="${product.supportedMaxOrderQuantity ?: ''}" spin-step="${product.isMultipleOrderQuantity ? product.multipleOfOrderQuantity : '1'}">
                                </g:if>
                                <span class="add-to-cart-button button et_pdp_add_to_cart" et-category="button" product-id="${product.id}" title="${g.message(code: 'add.to.cart')}" cart-min-quantity="${product.supportedMinOrderQuantity}" cart-quantity="${productQuantity}">
                                    <g:message code="add.to.cart"/>
                                </span>
                            </g:else>
                        </g:if>
                    </div>
                </plugin:hookTag>
            </td>
            <td>
                <plugin:hookTag hookPoint="productListViewEnd" attrs="${[product: product]}"/>
            </td>
        </tr>
    </g:each>
</table>

