<%@ page import="com.webcommander.plugin.discount.NameConstants; com.webcommander.plugin.discount.Constants" %>
<div class="product-discount-details" validation="skip@if{self::hidden} skip@if{this::input[name='productDetails.type']:checked} fail" message_template="<g:message code="choose.an.option"/>" validate-on="call-only">
    <div class="product-discount-details" data-type="${Constants.PRODUCT_DETAILS_TYPE.FREE_PRODUCT}">
        <div class="form-row">
            <input type="radio" name="productDetails.type" value="${Constants.PRODUCT_DETAILS_TYPE.FREE_PRODUCT}" ${discountDetails.type == Constants.PRODUCT_DETAILS_TYPE.FREE_PRODUCT ? "checked" : ""} toggle-target="free-product-dependents">
            <label><g:message code="free.product"/></label>
        </div>
        <div class="dependents free-product-dependents">
            <div class="form-row">
                <label><g:message code="maximum.quantity"/></label>
                <input type="text" name="productDetails.freeProductMaxQty" value="${discountDetails.freeProductMaxQty}" validation="skip@if{self::hidden} required digits" restrict="numeric" maxlength="9">
            </div>
        </div>
    </div>
    <div class="product-discount-details" data-type="${Constants.PRODUCT_DETAILS_TYPE.DISCOUNT_AMOUNT}">
        <div class="form-row">
            <input type="radio" name="productDetails.type" value="${Constants.PRODUCT_DETAILS_TYPE.DISCOUNT_AMOUNT}" ${discountDetails.type == Constants.PRODUCT_DETAILS_TYPE.DISCOUNT_AMOUNT ? "checked" : ""} toggle-target="product-discount-amount-dependents">
            <label><g:message code="discount.amount"/></label>
        </div>
        <div class="dependents product-discount-amount-dependents"  validation="skip@if{self::hidden} skip@if{this::input[name='productDetails.amountType']:checked} fail" message_template="<g:message code="choose.an.option"/>" validate-on="call-only">
            <div class="form-row">
                <input type="radio" name="productDetails.amountType" value="${Constants.PRODUCT_DETAILS_AMOUNT_TYPE.SINGLE}" ${discountDetails.amountType == Constants.PRODUCT_DETAILS_AMOUNT_TYPE.SINGLE ? "checked" : ""} toggle-target="product-single-amount-dependents">
                <label><g:message code="single.amount"/></label>
            </div>
            <div class="dependents product-single-amount-dependents">
                <div class="form-row">
                    <label><g:message code="discount.amount"/></label>
                    <div class="field-group">
                        <div class="field">
                            <input type="text" name="productDetails.singleAmount" value="${discountDetails.singleAmount?.toAdminPrice()}" restrict="decimal" validation="required@if{self::visible} price gt[0]" maxlength="9">
                        </div>
                        <div class="field">
                            <ui:namedSelect class="addon" name="productDetails.singleAmountType" key="${NameConstants.DISCOUNT_AMOUNT_TYPE}" value="${discountDetails.singleAmountType}"/>
                        </div>
                    </div>
                </div>
            </div>
            <div class="form-row">
                <input type="radio" name="productDetails.amountType" value="${Constants.PRODUCT_DETAILS_AMOUNT_TYPE.TIERED}" ${discountDetails.amountType == Constants.PRODUCT_DETAILS_AMOUNT_TYPE.TIERED ? "checked" : ""} toggle-target="product-tier-quantity-dependents">
                <label><g:message code="tiered.discount"/></label>
            </div>
            <div class="dependents product-tier-quantity-dependents">
                <table class="tier-table">
                    <tr>
                        <th><g:message code="minimum.quantity" /></th>
                        <th><g:message code="discount.value" /></th>
                        <th></th>
                    </tr>
                    <g:each in="${discountDetails?.tiers}" var="tier" status="i">
                        <tr rowid="${i}" class="tier-details-row">
                            <td><div class="field">
                                <input type="text" name="productDetails.quantityTier.${i}.minimumQty" validation="required@if{self::visible} number gt[0]" restrict="decimal" maxlength="9" value="${tier.minimumQty}">
                            </div></td>
                            <td>
                                <div class="field-group">
                                    <div class="field">
                                        <input type="text" name="productDetails.quantityTier.${i}.amount"  validation="required@if{self::visible} price gt[0]" restrict="decimal" maxlength="9" value="${tier.amount?.toAdminPrice()}">
                                    </div>
                                    <div class="field">
                                        <ui:namedSelect class="addon raw" name="productDetails.quantityTier.${i}.amountType" key="${NameConstants.DISCOUNT_AMOUNT_TYPE}" value="${tier.amountType}"/>
                                    </div>
                                </div>
                            </td>
                            <td>
                                <span class="tool-icon remove"></span>
                            </td>
                        </tr>
                    </g:each>
                    <tr class="last-row"><td colspan="3">
                        <span class="link-btn add-row">+<g:message code="add.new.tier"/></span>
                    </td></tr>
                    <tr class="template hidden">
                        <td><div class="field"><input type="text" data-name="minimumQty" data-validation="required@if{self::visible} number gt[0]" restrict="numeric" maxlength="9"></div></td>
                        <td><div class="field-group">
                            <div class="field">
                                <input type="text" data-name="amount"  data-validation="required@if{self::visible} price gt[0]" restrict="decimal" maxlength="9">
                            </div>
                            <div class="field">
                                <ui:namedSelect class="addon raw" data-name="amountType" key="${NameConstants.DISCOUNT_AMOUNT_TYPE}" />
                            </div>
                        </div></td>
                        <td>
                            <span class="tool-icon remove"></span>
                        </td>
                    </tr>
                </table>
                <div class="form-row">
                    <label><g:message code="minimum.quantity.on"/></label>
                    <ui:namedSelect name="productDetails.minimumQtyOn" key="${NameConstants.MINIMUM_QTY_ON}" value="${discountDetails.minimumQtyOn}"/>
                </div>
            </div>
        </div>
    </div>
    <div class="product-discount-details" data-type="${Constants.PRODUCT_DETAILS_TYPE.PRICE_CAP}">
        <div class="form-row">
            <input type="radio" name="productDetails.type" value="${Constants.PRODUCT_DETAILS_TYPE.PRICE_CAP}" ${discountDetails.type == Constants.PRODUCT_DETAILS_TYPE.PRICE_CAP ? "checked" : ""} toggle-target="price-cap-dependents">
            <label><g:message code="price.cap"/></label>
        </div>
        <div class="dependents price-cap-dependents double-input-row">
            <div class="form-row">
                <label><g:message code="quantity"/></label>
                <input type="text" name="productDetails.capPriceMaxQty" value="${discountDetails.capPriceMaxQty}" validation="skip@if{self::hidden} required digits" restrict="numeric" maxlength="9">
            </div>
            <div class="form-row">
                <label><g:message code="cap.price"/></label>
                <input type="text" name="productDetails.capPrice" value="${discountDetails.capPrice?.toAdminPrice()}" validation="skip@if{self::hidden} required price" restrict="decimal" maxlength="9">
            </div>
        </div>
    </div>
    <div class="form-row">
        <span class="title"><g:message code="select.product"/></span>
        <span class="tool-icon choose choose-product"></span>
        <div class="preview-table">
            <g:if test="${discountDetails?.products}">
                <discount:selectionTablePreview data="${['discountProducts': discountDetails.products]}"/>
            </g:if>
        </div>
    </div>
</div>