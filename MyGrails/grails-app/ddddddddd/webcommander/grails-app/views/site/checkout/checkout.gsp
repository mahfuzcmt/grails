<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants" %>
<div class="checkout-page">
    <div class="header-wrapper">
        <g:set var="title" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CHECKOUT_PAGE, "page_title")}"/>
        <h1 class="page-heading"><site:message code="${title}"/></h1>
    </div>
    <g:set var="stepCount" value="${1}"/>
    <div class="content-wrapper">
        <div class="section address loaded step-${stepCount}" data-name="address" data-loaded="true" data-step="${stepCount++}" data-url="<app:relativeBaseUrl/>shop/loadAddressStep">
            <div class="header"><span class="icon"></span><span class="title"><g:message code="billing.shipping.info"/></span></div>
            <div class="body">
                <g:include action="loadAddressStep" controller="shop"/>
            </div>
        </div>
        <g:if test="${shouldHaveShipping}">
            <div class="section shipping step-${stepCount}${cart.deliveryType != DomainConstants.ORDER_DELIVERY_TYPE.SHIPPING ? " disabled" : ""}" data-name="shipping" data-step="${stepCount++}"  data-url="<app:relativeBaseUrl/>shop/loadShippingStep">
                <div class="header"><span class="icon"></span><span class="title"><g:message code="shipping.handling"/></span></div>
                <div class="body"></div>
            </div>
        </g:if>
        <plugin:hookTag hookPoint="beforeCheckoutConfirmStep"/>
        <div class="section confirm step-${stepCount}" data-name="confirm" data-step="${stepCount++}"  data-url="<app:relativeBaseUrl/>shop/loadConfirmStep">
            <div class="header"><span class="icon"></span><span class="title"><g:message code="order.confirmation"/></span></div>
            <div class="body"></div>
        </div>
    </div>
</div>