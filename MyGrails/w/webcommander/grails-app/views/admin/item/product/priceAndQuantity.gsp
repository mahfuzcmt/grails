<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.StringUtil; com.webcommander.webcommerce.ShippingProfile; com.webcommander.webcommerce.TaxProfile;" %>
<%@ page import="com.webcommander.webcommerce.Product; com.webcommander.constants.DomainConstants; com.webcommander.conversion.*" %>
<form action="${app.relativeBaseUrl()}productAdmin/savePriceNQuantityProperties" method="post" class="create-edit-form product-properties-and-price">
    <input type="hidden" name="id" value="${product.id}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="product.price.and.stock"/></h3>
            <div class="info-content"><g:message code="section.text.product.price.stock"/></div>
        </div>
        <div class="form-section-container">
            <plugin:hookTag hookPoint="productEditPriceAndStock">
            <g:if test="${isInventoryEnable}">
                    <div class="form-row">
                        <input class="track-inventory single" name="isInventoryEnabled" type="checkbox" toggle-target="inventory-toggle" value="true"
                            ${product.isInventoryEnabled ? "checked='checked'" : ""}>
                        <span class="inline-label"><g:message code="track.inventory"/></span>
                        <span class="inventory-toggle display-inventory tool-icon" toggle-target="adjust-inventory" row-expanded="false" style="display: none" title="<g:message code='adjust.inventory'/>"></span>
                        <span class="inventory-toggle tool-icon view-inventory-history"  product-id="${product.id}" title="<g:message code="inventory.history"/>"></span>
                    </div>
            </g:if>
                <div class="adjust-inventory" style="display: none">
                    <div class="form-row indent-row thicker-row">
                        <label><g:message code="quantity"/></label>
                        <input type="text" name="adjust.changeQuantity" restrict="signed_numeric" class="medium" validation="number maxlength[9]">
                    </div><div class="form-row indent-row thicker-row">
                        <label><g:message code="note"/></label>
                        <textarea class="medium" name="adjust.note" style="height: 60px" validation="maxlength[250]" maxlength="250"></textarea>
                    </div>
                </div>
                <div class="double-input-row">
                    <div class="form-row inventory-toggle half" style="display: none">
                        <label><g:message code="available.stock"/></label>
                        <input name="availableStock" type="text" class="medium" disabled="disabled" value="${product.availableStock}">
                    </div><div class="form-row inventory-toggle half" style="display: none">
                        <label><g:message code="low.stock.level"/></label>
                        <input name="lowStockLevel" type="text" class="medium" value="${product.lowStockLevel}" validation="number  maxlength[9]" maxlength="9" restrict="numeric">
                    </div>
                </div>
                <div class="double-input-row">
                    <div class="form-row">
                        <label><g:message code="minimum.order.quantity"/></label>
                        <g:set var="mnoqid" value="${StringUtil.uuid}"/>
                        <input id="${mnoqid}" name="minOrderQuantity" type="text" class="medium" restrict="numeric" value="${product.minOrderQuantity}" maxlength="9" validation="number maxlength[9] gt[0]">
                    </div><div class="form-row">
                        <label><g:message code="maximum.order.quantity"/></label>
                        <g:set var="mxoqid" value="${StringUtil.uuid}"/>
                        <input id="${mxoqid}" name="maxOrderQuantity" type="text" class="medium" restrict="numeric" value="${product.maxOrderQuantity}" maxlength="9" validation="maxlength[9] number compare[${mnoqid}, number, gte]"
                               depends="#${mnoqid}">
                    </div>
                </div>
                <g:if test="${showTaxProfile}">
                <div class="form-row chosen-wrapper">
                    <label><g:message code="tax.profile"/></label>
                    %{--<ui:domainSelect name="taxProfile" class="medium" domain="${TaxProfile}" prepend="${['': g.message(code: "none")]}" value="${product.taxProfile?.id}"/>--}%
                    <g:select name="taxProfile" class="medium" from="${profiles}" noSelection="['': g.message(code: 'none')]" optionValue="name" optionKey="id" value="${product.taxProfile?.id}"/>
                </div>
                </g:if>
                <plugin:hookTag hookPoint="shippingProfileBlock" attrs="[product: product]">
                    <div class="form-row chosen-wrapper">
                        <label><g:message code="shipping.profile"/></label>
                        <ui:domainSelect name="shipping-profile" class="medium" domain="${ShippingProfile}" prepend="${['': g.message(code: "none")]}" value="${product.shippingProfile?.id}"/>
                    </div>
                </plugin:hookTag>
                <div class="double-input-row">
                    <div class="form-row">
                        <input name="isMultipleOrderQuantity" class="single" type="checkbox" toggle-target="display-multiple-order" value="true" ${product.isMultipleOrderQuantity ? "checked='checked'" : ""}>
                        <span class="inline-label"><g:message code="enable.multiple.order"/></span>
                    </div><div class="form-row mandatory display-multiple-order half" style="display: none">
                        <label><g:message code="multiple.order.quantity"/></label>
                        <input name="multipleOrderQuantity" type="text" class="medium" restrict="numeric" value="${product.multipleOfOrderQuantity}" maxlength="9"
                               validation="skip@if{self::hidden} required number compare[${mxoqid}, number, lte] gt[0] maxlength[9]" depends="#${mxoqid}">
                    </div>
                </div>
                <div class="double-input-row toggle-for-expect-to-pay" do-reverse-toggle>
                    <div class="form-row">
                        <g:set var="isOnSaleId" value="${StringUtil.uuid}"/>
                        <input id="${isOnSaleId}" class="single" name="isOnSale" type="checkbox" toggle-target="display-sale-price" value="true" ${product.isOnSale ? "checked='checked'" : ""}>
                        <span class="inline-label"><g:message code="on.sale"/></span>
                    </div><div class="form-row display-sale-price" style="display: none">
                        <label><g:message code="sale.price"/></label>
                        <input name="salePrice" type="text" class="medium" restrict="decimal" value="${product.salePrice?.toAdminPrice()}" maxlength="16" validation="required@if{global:#${isOnSaleId}:checked} number maxlength[16] price">
                    </div>
                </div>
                <div class="double-input-row">
                    <div class="form-row">
                        <input  class="single" name="isExpectToPay" type="checkbox" toggle-target="toggle-for-expect-to-pay" value="true" ${product.isExpectToPay ? "checked='checked'" : ""}>
                        <span class="inline-label"><g:message code="expect.to.pay"/></span>
                    </div><div class="form-row toggle-for-expect-to-pay mandatory">
                        <label><g:message code="expect.to.pay.price"/></label>
                        <input name="expectToPayPrice" type="text" class="medium" restrict="decimal" value="${product.expectToPayPrice?.toAdminPrice()}" maxlength="16" validation="skip@if{self::hidden} required number maxlength[16] price">
                    </div>
                </div>
                <plugin:hookTag hookPoint="callForPriceBlock" attrs="[product: product]">
                    <div class="form-row"  do-reverse-toggle>
                        <input name="isCallForPriceEnabled" class="single" type="checkbox" value="true" uncheck-value="false" ${product.isCallForPriceEnabled ? "checked='checked'" : ""}>
                        <span class="inline-label"><g:message code="enable.call.for.price"/></span>
                    </div>
                </plugin:hookTag>
                <div class="form-row">
                    <input type="checkbox" class="single" name="restrictPrice" toggle-target="restrict-price-dependent" ${product.restrictPriceFor != DomainConstants.PRODUCT_RESTRICT_PRICE_FOR.NONE ? "checked" : ""}>
                    <span><g:message code="restrict.price"/></span>
                    <sapn class="help">(<g:message code="who.can.view"/>)</sapn>
                </div>
                <div class="form-row restrict-price-dependent">
                    <span class="radio-wrapper">
                        <input type="radio" name="restrictPriceFor" value="${DomainConstants.PRODUCT_RESTRICT_PRICE_FOR.EVERYONE}" ${product.restrictPriceFor == DomainConstants.PRODUCT_RESTRICT_PRICE_FOR.EVERYONE || product.restrictPriceFor == DomainConstants.PRODUCT_RESTRICT_PRICE_FOR.NONE ? "checked" : ""}>
                        <span class="value"><g:message code="nobody"/></span>
                    </span>
                    <span class="radio-wrapper">
                        <input type="radio" name="restrictPriceFor" value="${DomainConstants.PRODUCT_RESTRICT_PRICE_FOR.EXCEPT_CUSTOMER}" ${product.restrictPriceFor == DomainConstants.PRODUCT_RESTRICT_PRICE_FOR.EXCEPT_CUSTOMER ? "checked='checked'" : ""}>
                        <span class="value"><g:message code="only.customers"/></span>
                    </span>
                    <span class="radio-wrapper">
                        <input type="radio" toggle-target="restrict-price-except-select-customer" name="restrictPriceFor" value="${DomainConstants.PRODUCT_RESTRICT_PRICE_FOR.EXCEPT_SELECTED}" ${product.restrictPriceFor == DomainConstants.PRODUCT_RESTRICT_PRICE_FOR.EXCEPT_SELECTED ? "checked" : ""}>
                            <span class="value"><g:message code="selected.customers.only"/></span>
                            <span class="restrict-price-except-select-customer tool-icon choose choose-customer"></span>
                    </span>
                    <g:each in="${product.restrictPriceExceptCustomers}" var="customer">
                        <input type="hidden" name="restrictPriceExceptCustomer" value="${customer.id}">
                    </g:each>
                    <g:each in="${product.restrictPriceExceptCustomerGroups}" var="customerGroup">
                        <input type="hidden" name="restrictPriceExceptCustomerGroup" value="${customerGroup.id}">
                    </g:each>
                </div>

                <div class="form-row">
                    <input type="checkbox" class="single" name="restrictPurchase" toggle-target="restrict-purchase-dependent" ${product.restrictPurchaseFor != DomainConstants.PRODUCT_RESTRICT_PURCHASE_FOR.NONE ? "checked" : ""}>
                    <span><g:message code="restrict.purchase"/></span>
                    <sapn class="help">(<g:message code="who.can.purchase"/>)</sapn>
                </div>
                <div class="form-row restrict-purchase-dependent">
                    <span class="radio-wrapper">
                        <input type="radio" name="restrictPurchaseFor" value="${DomainConstants.PRODUCT_RESTRICT_PURCHASE_FOR.EVERYONE}" ${product.restrictPurchaseFor == DomainConstants.PRODUCT_RESTRICT_PURCHASE_FOR.EVERYONE || product.restrictPurchaseFor == DomainConstants.PRODUCT_RESTRICT_PURCHASE_FOR.NONE ? "checked" : ""}>
                        <span class="value"><g:message code="nobody"/></span>
                    </span>
                    <span class="radio-wrapper">
                        <input type="radio" name="restrictPurchaseFor" value="${DomainConstants.PRODUCT_RESTRICT_PURCHASE_FOR.EXCEPT_CUSTOMER}" ${product.restrictPurchaseFor == DomainConstants.PRODUCT_RESTRICT_PURCHASE_FOR.EXCEPT_CUSTOMER ? "checked='checked'" : ""}>
                        <span class="value"><g:message code="only.customers"/></span>
                    </span>
                    <span class="radio-wrapper">
                        <input type="radio" toggle-target="restrict-purchase-except-select-customer" name="restrictPurchaseFor" value="${DomainConstants.PRODUCT_RESTRICT_PURCHASE_FOR.EXCEPT_SELECTED}" ${product.restrictPurchaseFor == DomainConstants.PRODUCT_RESTRICT_PURCHASE_FOR.EXCEPT_SELECTED ? "checked" : ""}>
                        <span class="value"><g:message code="selected.customers.only"/></span>
                        <span class="restrict-purchase-except-select-customer tool-icon choose choose-customer"></span>
                    </span>
                    <g:each in="${product.restrictPurchaseExceptCustomers}" var="customer">
                        <input type="hidden" name="restrictPurchaseExceptCustomer" value="${customer.id}">
                    </g:each>
                    <g:each in="${product.restrictPurchaseExceptCustomerGroups}" var="customerGroup">
                        <input type="hidden" name="restrictPurchaseExceptCustomerGroup" value="${customerGroup.id}">
                    </g:each>
                </div>
            </plugin:hookTag>
        </div>
    </div>
    <plugin:hookTag hookPoint="productPropertiesBlock" attrs="[product: product]">
        <div class="form-section-separator"></div>
        <div class="form-section">
            <div class="form-section-info">
                <h3><g:message code="product.properties"/></h3>
                <div class="info-content"><g:message code="section.text.product.properties"/></div>
            </div>
            <div class="form-section-container">
                <div class="double-input-row">
                    <div class="form-row">
                        <input type="checkbox" class="single" name="isFeatured" value="true" uncheck-value="false" ${product.isFeatured ? "checked" : ""}>
                        <span class="inline-lable"><g:message code="featured.product"/></span>
                    </div><div class="form-row">
                    <input type="checkbox" class="single" name="isNew" value="true" uncheck-value="false" ${product.isNew ? "checked" :""}>
                    <span><g:message code="new.product"/></span>
                </div>
                </div>
                <div class="form-row">
                    <label><g:message code="model"/></label>
                    <input type="text" name="model" class="medium" value="${product.model.encodeAsBMHTML()}" maxlength="250" validation="maxlength[250]">
                </div>
                <g:if test="${product.productType != DomainConstants.PRODUCT_TYPE.DOWNLOADABLE}">
                    <div class="double-input-row">
                        <div class="form-row">
                            <label><g:message code="length"/></label>
                            <input type="text" name="length" class="medium" restrict="decimal" value="${LengthConversions.convertSIToLength(unitLength, product.length).toFixed(6, false)}"  validation="number max[999999999] maxprecision[9,6]">
                            <span class="unit"><g:message code="${unitLength}"/></span>
                        </div><div class="form-row">
                        <label><g:message code="width"/></label>
                        <input type="text" name="width" class="medium" restrict="decimal" value="${LengthConversions.convertSIToLength(unitLength, product.width).toFixed(6, false)}"  validation="maxprecision[9,6] number max[999999999]">
                        <span class="unit"><g:message code="${unitLength}"/></span>
                    </div>
                    </div>
                    <div class="double-input-row">
                        <div class="form-row">
                            <label><g:message code="height"/></label>
                            <input type="text" name="height" class="medium" restrict="decimal" value="${LengthConversions.convertSIToLength(unitLength, product.height).toFixed(6, false)}"  validation="number max[999999999] maxprecision[9,6]">
                            <span class="unit"><g:message code="${unitLength}"/></span>
                        </div><div class="form-row">
                        <label><g:message code="weight"/></label>
                        <input type="text" name="weight" class="medium" restrict="decimal" value="${MassConversions.convertSIToMass(unitWeight, product.weight).toFixed(6, false)}" validation="number max[999999999] maxprecision[9,6]">
                        <span class="unit"><g:message code="${unitWeight}"/></span>
                    </div>
                    </div>
                </g:if>
            </div>
        </div>
    </plugin:hookTag>
    <div class="form-section">
        <div class="form-section-container">
            <div class="form-row">
                <label>&nbsp</label>
                <button type="submit" class="submit-button"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>