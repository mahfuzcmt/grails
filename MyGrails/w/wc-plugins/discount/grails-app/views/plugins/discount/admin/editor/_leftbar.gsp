<%@ page import="com.webcommander.util.StringUtil" %>

<div class="form-row-group wc-toggle-group">
    <div class="form-row">
        <label><g:message code="discount.name"/></label>
        <input type="text" class="small unique" name="name" value="${discount.name.encodeAsBMHTML()}" validation="required maxlength[100]" maxlength="100">
    </div>

    <div class="form-row">
        <input type="checkbox" class="single" name="isActive" ${discount.isActive ? "checked" : ""} value="true">
        <label><g:message code="active"/></label>
    </div>

    <div class="form-row datefield-between">
        <label><g:message code="start.date.time"/></label>
        <g:set var="startFromId" value="${StringUtil.uuid}"/>
        <input type="text" id="${startFromId}" class="timefield large" name="startFrom" validate-on="change" value="${discount.startFrom?.toDatePickerFormat(true, session.timezone)}">
    </div>

    <div class="form-row">
        <input type="checkbox" class="single" name="isSpecifyEndDate" value="true" ${discount.isSpecifyEndDate ? "checked": ""} toggle-target="specify-end-date">
        <label><g:message code="specify.end.date.time"/></label>
    </div>
    <div class="specify-end-date">
        <div class="form-row no-label datefield-between">
            <input type="text" class="timefield large" validate-on="change" name="startTo" value="${discount.startTo?.toDatePickerFormat(true, session.timezone)}" validation="skip@if{self::hidden} compare[${startFromId}, date, gt]" depends="#${startFromId}">
        </div>
    </div>

</div>

<div class="form-row-group wc-toggle-group">

    <div class="form-row">
        <input type="checkbox" class="single" name="isExcludeProductsOnSale" value="true" ${discount.isExcludeProductsOnSale ? "checked": ""} toggle-target="exclude-product">
        <label><g:message code="exclude.product.on.sale"/></label>
    </div>
    <div class="exclude-product">
        <div class="form-row">
            <span class="title"><g:message code="select.product"/></span>
            <span class="tool-icon choose choose-exclude-product"></span>
            <div class="exclude-product-preview-table">
                <g:if test="${discount?.excludeProducts}">
                    <discount:selectionTablePreview data="${['excludeProducts': discount.excludeProducts]}"/>
                </g:if>
            </div>
        </div>
    </div>

    <div class="form-row">
        <input type="checkbox" class="single" name="isDiscountUsedWithOtherDiscount" value="true" ${discount.isDiscountUsedWithOtherDiscount ? "checked": ""}>
        <label><g:message code="discount.use.with.other.discount"/></label>
    </div>

    <div class="form-row">
        <input type="checkbox" class="single" name="isMaximumUseTotal" value="true" ${discount.isMaximumUseTotal ? "checked": ""} toggle-target="maximum-use-total">
        <label><g:message code="maximum.use.total"/></label>
    </div>
    <div class="maximum-use-total">
        <div class="form-row no-label">
            <input type="text" class="small" name="maximumUseCount" value="${discount.maximumUseCount}" validation="required@if{self::visible} digits gt[0]" restrict="decimal" maxlength="9" placeholder="<g:message code="unlimited"/>">
        </div>
    </div>

    <div class="form-row">
        <input type="checkbox" class="single" name="isMaximumUseCustomer" value="true" ${discount.isMaximumUseCustomer ? "checked": ""} toggle-target="maximum-use-customer">
        <label><g:message code="maximum.use.customer"/></label>
    </div>
    <div class="maximum-use-customer">
        <div class="form-row no-label">
            <input type="text" class="small" name="maximumUseCustomerCount" value="${discount.maximumUseCustomerCount}" validation="required@if{self::visible} digits gt[0]" restrict="decimal" maxlength="9" placeholder="<g:message code="unlimited"/>">
        </div>
    </div>

    <div class="form-row">
        <input type="checkbox" class="single" name="isMaximumDiscountAllowed" value="true" ${discount.isMaximumDiscountAllowed ? "checked": ""} toggle-target="maximum-allowed-amount">
        <label><g:message code="maximum.allowed.amount"/></label>
    </div>
    <div class="maximum-allowed-amount">
        <div class="form-row no-label">
            <input type="text" class="small" name="maximumDiscountAllowedAmount" value="${discount.maximumDiscountAllowedAmount}" validation="required@if{self::visible} price" restrict="decimal" maxlength="12" placeholder="<g:message code="amount"/>">
        </div>
    </div>

    <div class="form-row">
        <input type="checkbox" class="single" name="isDisplayDiscountInformationProdDetail" value="true" ${discount.isDisplayDiscountInformationProdDetail ? "checked": ""}>
        <label><g:message code="discount.information.prod.detail"/></label>
    </div>

    <div class="form-row">
        <input type="checkbox" class="single" name="isDisplayTextCart" value="true" ${discount.isDisplayTextCart ? "checked": ""} toggle-target="display-additional-text-cart">
        <label><g:message code="display.additional.text.cart"/></label>
    </div>
    <div class="display-additional-text-cart">
        <div class="form-row no-label">
            <textarea class="small" validation="required@if{self::visible} maxlength[200]" maxlength="1000" name="displayTextCart">${discount.displayTextCart.encodeAsBMHTML()}</textarea>
        </div>
    </div>

    <div class="form-row">
        <input type="checkbox" class="single" name="isDisplayTextPartialDiscountCondition" value="true" ${discount.isDisplayTextPartialDiscountCondition ? "checked": ""} toggle-target="display-additional-text-condition">
        <label><g:message code="display.additional.text.condition"/></label>
    </div>
    <div class="display-additional-text-condition">
        <div class="form-row no-label">
            <textarea class="small" validation="required@if{self::visible} maxlength[200]" maxlength="1000" name="displayTextPartialDiscountCondition">${discount.displayTextPartialDiscountCondition.encodeAsBMHTML()}</textarea>
        </div>
    </div>

</div>

<div class="form-row-group wc-toggle-group">

    <div class="form-row">
        <input type="checkbox" class="single" name="isApplyCouponCode" value="true" ${discount.isApplyCouponCode ? "checked": ""} toggle-target="apply-coupon-code" ${isProfileUsed ? 'disabled' : ''}>
        <label><g:message code="apply.coupon.code"/></label>
    </div>

    <div class="apply-coupon-code">

        <div class="default-coupon-code-section" ${discount.isCreateUniqueCouponEachCustomer ? "style='display: none'" : '' }>
        <div class="double-input-row">
            <div class="form-row no-label">
                <label><g:message code="coupon.code"/></label>
            </div>
            <div class="form-row">
                <input type="checkbox" name="isCouponCodeAutoGenerate" value="true" ${discount.isCouponCodeAutoGenerate ? "checked": ""} ${isProfileUsed ? 'disabled' : ''}>
                <label><g:message code="auto.generate"/></label>
            </div>
        </div>
        <div class="form-row no-label">
            <input type="text" class="small ${!discount.id ? "unique": ""} " unique-action="isCouponUnique" name="defaultCouponCode" value="${discount.defaultCouponCode.encodeAsBMHTML()}" validation="required@if{self::visible} maxlength[200]" maxlength="200" ${discount.isCouponCodeAutoGenerate || isProfileUsed ? 'readonly' : ''} >
            <input type="hidden" name="generatedCouponCode" value="${discount.defaultCouponCode.encodeAsBMHTML()}">
        </div>
        </div>

        <div class="form-row">
            <input type="checkbox" class="single" name="isCreateUniqueCouponEachCustomer" value="true" ${discount.isCreateUniqueCouponEachCustomer ? "checked": ""} toggle-target="apply-coupon-code-unique-customer" ${isProfileUsed ? 'disabled' : ''} >
            <label><g:message code="apply.coupon.code.unique.customer"/></label>
        </div>
        <div class="apply-coupon-code-unique-customer">
            <div class="form-row no-label view-coupon-code" data-coupon-id="${discount?.coupon?.id}">
                <label style="cursor: pointer">(<g:message code="view.coupon.codes"/>)</label>
            </div>
            <div class="form-row no-label export-coupon-code" data-coupon-id="${discount?.coupon?.id}">
                <label style="cursor: pointer">(<g:message code="export.coupon.codes"/>)</label>
            </div>
        </div>

        <div class="form-row">
            <input type="checkbox" class="single" name="isDisplayTextCoupon" value="true" ${discount.isDisplayTextCoupon ? "checked": ""} toggle-target="apply-coupon-code-additional-info">
            <label><g:message code="apply.coupon.code.additional.info"/></label>
        </div>
        <div class="apply-coupon-code-additional-info">
            <div class="form-row no-label">
                <textarea class="small" validation="required@if{self::visible} maxlength[200]" maxlength="1000" name="displayTextCoupon">${discount.displayTextCoupon.encodeAsBMHTML()}</textarea>
            </div>
        </div>

    </div>

</div>