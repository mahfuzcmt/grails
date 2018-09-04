<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<g:set var="enableShipping" value="${configs.enable_shipping}"/>
<g:set var="enableStorePickUp" value="${configs.enable_store_pickup}"/>
<g:set var="enableOthersShipping" value="${configs.enable_others_shipping}"/>
<input type="hidden" name="mode" value="edit">
<div class="address billing">
    <div class="address-view">
        <div class="header">
            <span class="title"><g:message code="billing.address"/></span>
            <span class="toolbar">
                <span class="toolbar-btn edit edit-address" data-type="billing"><g:message code="edit"/></span>
                <g:if test="${customer}">
                    <span class="toolbar-btn change change-address" data-type="billing"><g:message code="change"/></span>
                </g:if>
            </span>
        </div>
        <div class="address">
            <div class="name">${effectiveBilling.fullName.encodeAsBMHTML()}</div>
            <div class="address-line-1">${effectiveBilling.addressLine.encodeAsBMHTML()}</div>
            <div class="address-line-2">${effectiveBilling.stateName}, ${effectiveBilling.city.encodeAsBMHTML()} ${effectiveBilling.postCode}, ${effectiveBilling.countryName}</div>
        </div>
    </div>
</div>
<g:if test="${shouldHaveShipping}">
    <div class="section delivery-types">
        <g:if test="${enableShipping == "true" && (enableStorePickUp == "true" || enableOthersShipping == "true")}">
            <div class="form-row">
                <input type="radio" class="single" name="delivery_type" value="${DomainConstants.ORDER_DELIVERY_TYPE.SHIPPING}" ${cart.deliveryType == DomainConstants.ORDER_DELIVERY_TYPE.SHIPPING ? "checked" : ""} toggle-target="shipping-config"/>
                <span class="label-text"><site:message code="${configs.shipping_display_text}"/></span>
                <g:if test="${configs.shipping_display_subtext}">
                    <span class="label-text">(<site:message code="${configs.shipping_display_subtext}"/>)</span>
                </g:if>
            </div>
        </g:if>
        <g:if test="${enableShipping == "true"}">
            <div class="form-row different-shipping shipping-config">
                <input type="checkbox" name="is_different_shipping" value="true" ${session.is_different_shipping ? "checked" : ""} toggle-target="shipping-address-wrap"/>
                <span><g:message code="ship.to.different.address"/></span>
            </div>
        </g:if>
        <g:if test="${enableStorePickUp == "true"}">
            <div class="form-row">
                <input type="radio" name="delivery_type" value="${DomainConstants.ORDER_DELIVERY_TYPE.STORE_PICKUP}" ${cart.deliveryType == DomainConstants.ORDER_DELIVERY_TYPE.STORE_PICKUP ? "checked" : ""} />
                <span class="label-text"><site:message code="${configs.store_pickup_display_text}"/></span>
                <g:if test="${configs.store_pickup_display_subtext}">
                    <span class="label-text">(<site:message code="${configs.store_pickup_display_subtext}"/>)</span>
                </g:if>
            </div>
        </g:if>
        <g:if test="${enableOthersShipping == "true"}">
            <div class="form-row">
                <input type="radio" name="delivery_type" value="${DomainConstants.ORDER_DELIVERY_TYPE.OTHERS_SHIPPING}" ${cart.deliveryType == DomainConstants.ORDER_DELIVERY_TYPE.OTHERS_SHIPPING ? "checked" : ""}/>
                <span class="label-text"><site:message code="${configs.others_shipping_display_text}"/></span>
                <g:if test="${configs.others_shipping_display_subtext}">
                    <span class="label-text">(<site:message code="${configs.others_shipping_display_subtext}"/>)</span>
                </g:if>
            </div>
        </g:if>
    </div>
    <g:if test="${enableShipping == "true"}">
        <div class="address shipping shipping-address-wrap shipping-config">
            <div class="address-view shipping">
                <div class="header">
                    <span class="title"><g:message code="shipping.address"/></span>
                    <span class="toolbar">
                        <span class="toolbar-btn edit edit-address" data-type="shipping"><g:message code="edit"/></span>
                        <g:if test="${customer}">
                            <span class="toolbar-btn change change-address" data-type="shipping"><g:message code="change"/></span>
                        </g:if>
                    </span>
                </div>
                <div class="address">
                    <div class="name">${effectiveShipping.fullName.encodeAsBMHTML()}</div>
                    <div class="address-line-1">${effectiveShipping.addressLine.encodeAsBMHTML()}</div>
                    <div class="address-line-2">${effectiveShipping.stateName}, ${effectiveShipping.city.encodeAsBMHTML()} ${effectiveShipping.postCode}, ${effectiveShipping.countryName}</div>
                </div>
            </div>
        </div>
    </g:if>

</g:if>
<g:if test="${isTaxAvailable}">
    <button class="step-continue-button"><g:message code="continue"/></button>
</g:if>
<g:else>
   <span class="tax-rate-note-available"><g:message code="tax.rate.not.match"/></span>
</g:else>
