<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.StringUtil" %>
<div class="toolbar-share">
    <span class="item-group entity-count title toolbar-left">
        <g:message code="product"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="product-bulk-edit-tab price-stock-table table-view">
    <div class="bulk-editor-form" action="${app.relativeBaseUrl()}productAdmin/savePriceStockBulkProperties">
        <div class="body">
            <table class="content">
                <input type="hidden" name="tax" value='${tax}'>
                <input type="hidden" name="shipping" value='${shipping}'>
                <colgroup>
                    <col class="name-column">
                    <col class="heading-column">
                    <col class="quantity-column">
                    <col class="tax-column">
                    <col class="shipping-column">
                    <col class="price-column">
                    <col class="restricted-price-column">
                    <col class="restricted-purchase-column">
                </colgroup>
                <tr>
                    <th><g:message code="product.name"/></th>
                    <th><g:message code="heading"/></th>
                    <th><g:message code="minimum.order.quantity"/></th>
                    <th><g:message code="tax.profile"/></th>
                    <th><g:message code="shipping.profile"/></th>
                    <th><g:message code="on.sale"/></th>
                    <th><g:message code="restrict.price"/></th>
                    <th><g:message code="restrict.purchase"/></th>
                </tr>
                <tr class="data-row">
                    <td></td>
                    <td></td>
                    <td class="editable custom-edit min-order-quantity" restrict="numeric" validation="number maxlength[9]"></td>
                    <td class="selectable custom-select tax-profile hidden-overflow-selectable"><span class="value"></span></td>
                    <td class="selectable custom-select shipping-profile hidden-overflow-selectable"><span class="value"></span></td>
                    <td class="change-all on-sale"><span class="fake-link"><g:message code="change.all"/></span></td>
                    <td class="selectable custom-select restricted-price hidden-overflow-selectable"><span class="value"></span><span class="restrict-price-except-select-customer tool-icon choose choose-customer" style="display: none;"></span></td>
                    <td class="selectable custom-select restricted-purchase hidden-overflow-selectable"><span class="value"></span><span class="restrict-purchase-except-select-customer tool-icon choose choose-customer" style="display: none;"></span></td>
                </tr>
                <g:each in="${products}" var="product" status="i">
                    <tr class="data-row">
                        <g:set var="id" value="${product.id}"/>
                        <input type="hidden" name="id" value="${id}">
                        <td class="editable name" validation="required rangelength[2, 100]">
                            <input type="hidden" name="${id}.name" value="${product.name}"><span class="value">${product.name.encodeAsBMHTML()}</span>
                        </td>
                        <td class="editable heading">
                            <input type="hidden" name="${id}.heading" value="${product.heading}"><span class="value">${product.heading.encodeAsBMHTML()}</span>
                        </td>
                        <td class="editable min-order-quantity" restrict="numeric" validation="number maxlength[9]">
                            <input type="hidden" name="${id}.minOrderQuantity" value="${product.minOrderQuantity}"><span class="value">${product.minOrderQuantity}</span>
                        </td>
                        <td class="tax-profile selectable hidden-overflow-selectable">
                            <input type="hidden" name="${id}.taxProfile" value="${product.taxProfile?.id}">
                            <span class="disp-value">${product.taxProfile?.name.encodeAsBMHTML()}</span>
                            <span class="value">${product.taxProfile?.id}</span>
                        </td>
                        <td class="${product.productType != DomainConstants.PRODUCT_TYPE.DOWNLOADABLE ? "shipping-profile selectable hidden-overflow-selectable" : ""}">
                            <input type="hidden" name="${id}.shippingProfile" value="${product.shippingProfile?.id}">
                            <span class="disp-value">${product.shippingProfile?.name.encodeAsBMHTML()}</span>
                            <span class="value">${product.shippingProfile?.id}</span>
                        </td>
                        <td class="selectable on-sale hidden-overflow-selectable">
                            <input type="hidden" class="is-on-sale" name="${id}.isOnSale" value="${product.isOnSale}">
                            <input type="text" class="sale-price td-full-width" name="${id}.salePrice" value="${product.salePrice}" restrict="decimal" style="display: none">
                            <span class="value">${product.isOnSale}</span>
                            <span class="disp-value">${product.isOnSale ? product.salePrice.toAdminPrice() : g.message(code: "no")}</span>
                        </td>
                        <td class="restricted-price selectable hidden-overflow-selectable">
                            <input type="hidden" name="${id}.restrictPriceFor" value="${product.restrictPriceFor}">
                            <span class="disp-value">${g.message(code: product.restrictPriceFor == 'none' ? 'nobody' : product.restrictPriceFor)}</span>
                            <span class="value">${product.restrictPriceFor}</span>
                            <g:each in="${product.restrictPriceExceptCustomers}" var="customer">
                                <input type="hidden" name="${id}.restrictPriceExceptCustomer" value="${customer.id}">
                            </g:each>
                            <g:each in="${product.restrictPriceExceptCustomerGroups}" var="customerGroup">
                                <input type="hidden" name="${id}.restrictPriceExceptCustomerGroup" value="${customerGroup.id}">
                            </g:each>
                        </td>
                        <td class="restricted-purchase selectable hidden-overflow-selectable">
                            <input type="hidden" name="${id}.restrictPurchaseFor" value="${product.restrictPurchaseFor}">
                            <span class="disp-value">${g.message(code: product.restrictPurchaseFor == 'none' ? 'nobody' : product.restrictPurchaseFor)}</span>
                            <span class="value">${product.restrictPurchaseFor}</span>
                            <g:each in="${product.restrictPurchaseExceptCustomers}" var="customer">
                                <input type="hidden" name="${id}.restrictPurchaseExceptCustomer" value="${customer.id}">
                            </g:each>
                            <g:each in="${product.restrictPurchaseExceptCustomerGroups}" var="customerGroup">
                                <input type="hidden" name="${id}.restrictPurchaseExceptCustomerGroup" value="${customerGroup.id}">
                            </g:each>
                        </td>
                    </tr>
                </g:each>
            </table>
        </div>
        <div class="form-row">
            <label>&nbsp;</label>
            <button type="button" class="submit-button"><g:message code="update"/></button>
        </div>
    </div>
</div>