<%@ page import="com.webcommander.constants.DomainConstants" %>
<form action="${app.relativeBaseUrl()}setting/saveConfigurations" method="POST" class="create-edit-form">
    <input type="hidden" name="type" value="${DomainConstants.SITE_CONFIG_TYPES.DISCOUNT}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="discount.coupon"/></h3>
            <div class="info-content"><g:message code="section.text.discount.coupon.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <input type="checkbox" class="single" name="${DomainConstants.SITE_CONFIG_TYPES.DISCOUNT}.show_coupon_in_cart_page" value="true" uncheck-value="false" ${config["show_coupon_in_cart_page"] == "true" ? "checked": ""} >
                <span><g:message code="show.in.cart.page"/></span>
            </div>
            <div class="form-row">
                <input type="checkbox" class="single" name="${DomainConstants.SITE_CONFIG_TYPES.DISCOUNT}.show_coupon_in_checkout_page" value="true" uncheck-value="false" ${config["show_coupon_in_checkout_page"] == "true" ? "checked": ""} >
                <span><g:message code="show.in.checkout.page"/></span>
            </div>
            <div class="form-row mandatory">
                <label><g:message code="discount.coupon.code.prefix"/><span class="suggestion">DISP-</span></label>
                <input type="text" class="single" name="${DomainConstants.SITE_CONFIG_TYPES.DISCOUNT}.coupon_code_prefix" validation="required rangelength[2,50] function[discountCouponPrefixValidation]" value="${config.coupon_code_prefix}">
            </div>
            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>