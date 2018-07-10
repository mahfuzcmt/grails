<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.StringUtil; com.webcommander.webcommerce.ShippingProfile; com.webcommander.webcommerce.TaxProfile;" %>
<%@ page import="com.webcommander.webcommerce.Product; com.webcommander.constants.DomainConstants; com.webcommander.conversion.*" %>
<div class="toolbar-share">
    <span class="header-title"><g:message code="web.commerce"/> > <g:message code="product"/> > ${product.name.encodeAsBMHTML()} > <g:message code="variation"/> > ${details.name ?: product.name.encodeAsBMHTML() + "_" + details.id} > <g:message code="price.stock"/> </span>
</div>
<form action="${app.relativeBaseUrl()}enterpriseVariation/saveProperties" method="post" class="create-edit-form product-properties-and-price">
    <input type="hidden" name="id" value="${details.id}">
    <input type="hidden" name="type" value="priceStock">
    <input type="hidden" name="productId" value="${product.id}">
    <input type="hidden" name="variationId" value="${variationId}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="product.price.and.stock"/></h3>
            <div class="info-content"><g:message code="section.text.product.price.stock"/></div>
        </div>
        <div class="form-section-container">
            <plugin:hookTag hookPoint="productEditPriceAndStock" attrs="${[entityType: "variation"]}">
                <div class="form-row">
                    <input class="track-inventory single" name="isInventoryEnabled" type="checkbox" toggle-target="inventory-toggle" value="true"
                        ${details.isInventoryEnabled ? "checked='checked'" : ""}>
                    <span class="inline-label"><g:message code="track.inventory"/></span>
                    <span class="inventory-toggle display-inventory tool-icon" toggle-target="adjust-inventory" row-expanded="false" style="display: none"
                          title="<g:message code='adjust.inventory'/>"></span>
                    <span class="inventory-toggle tool-icon view-inventory-history"  variation-id="${details.id}" title="<g:message code="inventory.history"/>"></span>
                </div>
                <div class="adjust-inventory" style="display: none">
                    <div class="form-row indent-row thicker-row">
                        <label><g:message code="quantity"/></label>
                        <input type="text" name="changeQuantity" restrict="signed_numeric" class="medium" validation="number maxlength[9]">
                    </div><div class="form-row indent-row thicker-row">
                        <label><g:message code="note"/></label>
                        <textarea class="medium" validation="maxlength[250]" maxlength="250" name="note" style="height: 60px"></textarea>
                    </div>
                </div>
                <div class="double-input-row">
                    <div class="form-row inventory-toggle" style="display: none">
                        <label><g:message code="available.stock"/></label>
                        <input name="availableStock" type="text" class="medium" disabled="disabled" value="${details.availableStock ?: 0}">
                    </div><div class="form-row inventory-toggle" style="display: none">
                        <label><g:message code="low.stock.level"/></label>
                        <input name="lowStockLevel" type="text" class="medium" value="${details.lowStockLevel ?: 0}" validation="number" restrict="numeric">
                    </div>
                </div>
                <div class="double-input-row">
                    <div class="form-row">
                        <label><g:message code="minimum.order.quantity"/></label>
                        <g:set var="mnoqid" value="${StringUtil.uuid}"/>
                        <input id="${mnoqid}" name="priceStock.minOrderQuantity" type="text" class="medium min-order-quantity" ${detailsMap.containsKey("minOrderQuantity") ? '' : 'disabled'}
                               restrict="numeric" value="${detailsMap.minOrderQuantity != null ? detailsMap.minOrderQuantity : product.minOrderQuantity}" maxlength="9" validation="number maxlength[9] gt[0]">
                    </div><div class="form-row with-check-box">
                        <label><g:message code="maximum.order.quantity"/></label>
                        <g:set var="mxoqid" value="${StringUtil.uuid}"/>
                        <input id="${mxoqid}" name="priceStock.maxOrderQuantity" type="text" class="medium" ${detailsMap.containsKey("minOrderQuantity") ? '' : 'disabled'}
                               restrict="numeric" value="${detailsMap.maxOrderQuantity != null ? detailsMap.maxOrderQuantity : product.maxOrderQuantity}" maxlength="9"
                               validation="maxlength[9] number compare[${mnoqid}, number, gte]" depends="#${mnoqid}">
                        <input type="checkbox" class="multiple active-check" disable-also="min-order-quantity" value="true" ${detailsMap.containsKey("minOrderQuantity") ? 'checked' : ''}>
                    </div>
                </div>
                <div class="double-input-row">
                    <div class="form-row">
                        <input name="priceStock.isMultipleOrderQuantity" class="single" type="checkbox" toggle-target="display-multiple-order" value="true"
                            ${detailsMap.isMultipleOrderQuantity.toBoolean() ? "checked='checked'" : ""}>
                        <span class="inline-label"><g:message code="enable.multiple.order"/></span>
                    </div><div class="form-row mandatory display-multiple-order" style="display: none">
                        <label><g:message code="multiple.order.quantity"/></label>
                        <input name="priceStock.multipleOrderQuantity" type="text" class="medium" restrict="numeric" value="${detailsMap.multipleOrderQuantity ?: 1}" maxlength="9"
                               validation="skip@if{self::hidden} required number compare[${mxoqid}, number, lt] maxlength[9] gt[0]" depends="#${mxoqid}">
                    </div>
                </div>
                <div class="double-input-row toggle-for-expect-to-pay" do-reverse-toggle>
                    <div class="form-row">
                        <g:set var="isOnSaleId" value="${StringUtil.uuid}"/>
                        <input id="${isOnSaleId}" class="single" name="priceStock.isOnSale" type="checkbox" toggle-target="display-sale-price" value="true"
                        ${detailsMap.isOnSale.toBoolean() ? "checked='checked'" : ""}>
                        <span class="inline-label"><g:message code="on.sale"/></span>
                    </div><div class="form-row display-sale-price" style="display: none">
                        <label><g:message code="sale.price"/></label>
                        <input name="priceStock.salePrice" type="text" class="medium" restrict="decimal"
                               value="${detailsMap.salePrice ? detailsMap.salePrice.toDouble().toAdminPrice() : product.salePrice.toAdminPrice()}" maxlength="16"
                               validation="required@if{global:#${isOnSaleId}:checked} number maxlength[16] price">
                    </div>
                </div>
                <div class="double-input-row">
                    <div class="form-row">
                        <input  class="single" name="priceStock.isExpectToPay" type="checkbox" toggle-target="toggle-for-expect-to-pay" value="true"
                            ${detailsMap.isExpectToPay.toBoolean() ? "checked='checked'" : ""}>
                        <span class="inline-label"><g:message code="expect.to.pay"/></span>
                    </div><div class="form-row toggle-for-expect-to-pay">
                        <label><g:message code="expect.to.pay.price"/></label>
                        <input name="priceStock.expectToPayPrice" type="text" class="medium" restrict="decimal"
                               value="${detailsMap.expectToPayPrice ? detailsMap.expectToPayPrice.toDouble().toAdminPrice() : product.expectToPayPrice.toAdminPrice()}"
                               maxlength="16" validation="skip@if{self::hidden} required number maxlength[16] price">
                    </div>
                </div>
                <plugin:hookTag hookPoint="callForPriceBlock" attrs="[product: product]">
                    <div class="form-row">
                        <input name="priceStock.isCallForPriceEnabled" class="single" type="checkbox" value="true" uncheck-value="false" ${detailsMap.isCallForPriceEnabled.toBoolean() ? "checked='checked'" : ""}>
                        <span class="inline-label"><g:message code="enable.call.for.price"/></span>
                    </div>
                </plugin:hookTag>
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
                        <input type="checkbox" class="single" name="priceStock.isFeatured" value="true" uncheck-value="false" ${detailsMap.isFeatured.toBoolean() ? "checked" : ""}>
                        <span class="inline-lable"><g:message code="featured.product"/></span>
                    </div><div class="form-row">
                    <input type="checkbox" class="single" name="priceStock.isNew" value="true" uncheck-value="false" ${detailsMap.isNew.toBoolean() ? "checked" :""}>
                    <span><g:message code="new.product"/></span>
                </div>
                </div>
                <div class="form-row with-check-box">
                    <label><g:message code="model"/></label>
                    <input type="text" name="priceStock.model" class="medium" ${detailsMap.containsKey("model") ? '' : 'disabled'} value="${detailsMap.model ?: product.model.encodeAsBMHTML()}">
                    <input type="checkbox" class="multiple active-check" value="true" ${detailsMap.containsKey("model") ? 'checked' : ''}>
                </div>
                <div class="double-input-row">
                    <div class="form-row with-check-box">
                        <label><g:message code="length"/></label>
                        <input type="text" name="priceStock.length" class="medium" restrict="decimal" ${detailsMap.containsKey("length") ? '' : 'disabled'}
                               value="${detailsMap.length ? LengthConversions.convertSIToLength(unitLength, detailsMap.length.toDouble()).toLength() : LengthConversions.convertSIToLength(unitLength, product.length).toLength()}"  validation="number max[999999999]">
                        <span class="unit"><g:message code="${unitLength}"/></span>
                        <input type="checkbox" class="multiple active-check" value="true" ${detailsMap.containsKey("length") ? 'checked' : ''}>
                    </div><div class="form-row with-check-box">
                    <label><g:message code="width"/></label>
                    <input type="text" name="priceStock.width" class="medium" ${detailsMap.containsKey("width") ? '' : 'disabled'} restrict="decimal"
                           value="${detailsMap.width ? LengthConversions.convertSIToLength(unitLength, detailsMap.width.toDouble()).toLength() : LengthConversions.convertSIToLength(unitLength, product.width).toLength()}"  validation="number max[999999999]">
                    <span class="unit"><g:message code="${unitLength}"/></span>
                    <input type="checkbox" class="multiple active-check" value="true" ${detailsMap.containsKey("width") ? 'checked' : ''}>
                </div>
                </div>
                <div class="double-input-row">
                    <div class="form-row with-check-box">
                        <label><g:message code="height"/></label>
                        <input type="text" name="priceStock.height" class="medium" restrict="decimal" ${detailsMap.containsKey("height") ? '' : 'disabled'}
                               value="${detailsMap.height ? LengthConversions.convertSIToLength(unitLength, detailsMap.height.toDouble()).toLength() : LengthConversions.convertSIToLength(unitLength, product.height).toLength()}"  validation="number max[999999999]">
                        <span class="unit"><g:message code="${unitLength}"/></span>
                        <input type="checkbox" class="multiple active-check" value="true" ${detailsMap.containsKey("height") ? 'checked' : ''}>
                    </div><div class="form-row with-check-box">
                    <label><g:message code="weight"/></label>
                    <input type="text" name="priceStock.weight" class="medium" restrict="decimal" ${detailsMap.containsKey("weight") ? '' : 'disabled'}
                           value="${detailsMap.weight ? MassConversions.convertSIToMass(unitWeight, detailsMap.weight.toDouble()).toWeight() : MassConversions.convertSIToMass(unitWeight, product.weight).toWeight()}" validation="number max[999999999]" max="9">
                    <span class="unit"><g:message code="${unitWeight}"/></span>
                    <input type="checkbox" class="multiple active-check" value="true" ${detailsMap.containsKey("weight") ? 'checked' : ''}>
                </div>
                </div>
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