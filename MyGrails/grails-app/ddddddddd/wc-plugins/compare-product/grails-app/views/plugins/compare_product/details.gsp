<%@ page import="com.webcommander.webcommerce.Product; com.webcommander.util.AppUtil; com.webcommander.plugin.compare_product.Constants.NamedConstants; com.webcommander.constants.DomainConstants" %>
<g:set var="imageSize" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE, "size")[com.webcommander.constants.NamedConstants.PRODUCT_IMAGE_SETTINGS.GRIDVIEW]}"/>
<%
    app.enqueueSiteJs(src: "plugins/compare-product/js/compare-product-details.js", scriptId: "compare-product-details")
    Long loggedCustomer = AppUtil.loggedCustomer
    List loggedCustomerGroupIds = AppUtil.loggedCustomerGroupIds
%>
<div class="compare-details">
    <div class="heading"><h1 class="title page-heading"><g:message code="compare.products"/></h1></div>
    <table border="0" cellspacing="0" cellpadding="0" class="product-view">
        <colgroup>
            <col class="left-bar">
            <g:each in="${productList}">
                <col class="product">
            </g:each>
        </colgroup>
        <g:if test="${productList.size() > 1}">
            <tr class="action-row">
                <td></td>
                <g:each in="${productList}" var="product">
                    <td><span product-id="${product.id}" class="remove"><g:message code="remove"/></span></td>
                </g:each>
            </tr>
        </g:if>
        <plugin:hookTag hookPoint="compareProductDetailsTable">
            <g:each in="${NamedConstants.COMPARE_FIELD}" var="field">
                <tr class="${field.key}-row">
                    <g:if test="${config[field.key + "_active"].toBoolean(false)}">
                        <th><g:message code="${field.value}"/></th>
                        <g:each in="${productList}" var="product">
                            <td>
                                <g:if test="${field.key == 'image'}">
                                    <div class="image product-image-view-height product-image product-view image-view">
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
                                        <a class="product-image-link image-link link" href="${app.relativeBaseUrl() + "product/" + product.url}">
                                            <g:set var="url" value="${appResource.getSiteProductImageURL(productData: product, imageSize:imageSize)}"/>
                                            <img src="${url}" alt="${product.altText}">
                                        </a>
                                    </div>
                                </g:if>
                                <g:elseif test="${field.key == "description"}">
                                    ${product.description}
                                </g:elseif>
                                <g:elseif test="${field.key == "price"}">
                                    <g:if test="${product.isPriceRestricted(loggedCustomer, loggedCustomerGroupIds) != true}">
                                        <div class="price">${session.currency?.symbol?:AppUtil.baseCurrency.symbol}${product["priceToDisplay"].toCurrency().toPrice()}</div>
                                    </g:if>
                                </g:elseif>
                                <g:else>
                                    ${(product[field.key] != null ? product[field.key] : "" + "").encodeAsBMHTML()}
                                </g:else>
                            </td>
                        </g:each>
                    </g:if>
                </tr>
            </g:each>
            <g:each in="${customCompareDatas}" var="cpdata">
                <tr class="${cpdata.label.sanitize()}-row">
                    <th>${cpdata.label.encodeAsBMHTML()}</th>
                    <g:each in="${cpdata.description}" var="description">
                        <td>
                            ${description.encodeAsBMHTML()}
                        </td>
                    </g:each>
                </tr>
            </g:each>
        </plugin:hookTag>
        <g:if test="${config.add_to_cart_active == "true"}">
            <tr class="add-to-cart-row">
                <th><g:message code="add.to.cart"/></th>
                <g:each in="${productList}" var="product">
                    <td>
                        <g:if test="${product.isCallForPriceEnabled}">
                            <span class="button call-for-price"><site:message code="${config.label_for_call_for_price}"/></span>
                        </g:if>
                        <g:elseif test="${product.isPriceOrPurchaseRestricted(loggedCustomer, loggedCustomerGroupIds) != true}">
                            <span class="add-to-cart-button button et_pdp_add_to_cart" et-category="button" product-id="${product.id}"
                                  cart-min-quantity="${product.supportedMinOrderQuantity}">
                                <g:message code="add.to.cart"/>
                            </span>
                        </g:elseif>
                    </td>
                </g:each>
            </tr>
        </g:if>
    </table>
</div>


