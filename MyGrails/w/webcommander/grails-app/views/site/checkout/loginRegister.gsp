<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil; com.webcommander.constants.NamedConstants" %>
<div class="checkout-page-options">
    <h1><g:message code="checkout"/></h1>
    <div class="item-head"><g:message code="checkout.option"/></div>
    <div class="content-wrapper">
        <g:if test="${configs.allow_sign_up == "true" || configs.show_checkout_as_a_guest == "true"}">
            <div class="new-customer">
                <g:form class="new-customer-from" controller="shop" action="chooseCheckoutOption">
                    <g:if test="${configs.allow_sign_up == "true"}">
                        <h1><g:message code="new.customer"/></h1>
                        <p><site:message code="s:checkout.new.customer.registration.message"/></p>
                        <span class="option">
                            <input id="checkout-register" type="radio" name='checkout-type' value="${NamedConstants.CUSTOMER_CHECKOUT_TYPE.REGISTRATION}" checked="checked">
                            <label for="checkout-register"><g:message code="register"/></label>
                        </span>
                    </g:if>
                    <g:else>
                        <h1><g:message code="guest.customer"/></h1>
                    </g:else>
                    <g:if test="${configs.show_checkout_as_a_guest == "true" && (configs.allow_sign_up == "false" || configs.is_sign_up_required == "false")}">
                        <span class="option">
                            <input id="checkout-guest" class="et_cartp_checkout_as_guest" et-category="link" type="radio" name='checkout-type' value="${NamedConstants.CUSTOMER_CHECKOUT_TYPE.GUEST}" ${configs.allow_sign_up != "true" ? "checked" : ""}>
                            <label for="checkout-guest"><g:message code="checkout.guest"/></label>
                        </span>
                    </g:if>
                    <button type="submit" class="checkout-option"><g:message code="continue"/></button>
                </g:form>
            </div>
        </g:if>
        <g:if test="${configs.allow_sign_up == "true" || configs.show_login == "true"}">
            <div class="registered-customer">
                <h1><site:message code="s:login"/></h1>
                <p class="register"><g:message code="already.registered"/></p>
                <p class="login"><g:message code="please.login.below"/></p>
                <g:form controller="customer" action="doLogin" class="valid-verify-form">
                    <input type="hidden" name="referer" value="/shop/checkout">
                    <div class="form-row mandatory">
                        <label><g:message code="email"/>:</label>
                        <input type="text" name="userName" validation="required email rangelength[4,50]" placeholder="<g:message code="email"/>">
                    </div>
                    <div class="form-row mandatory">
                        <label><g:message code="password"/>:</label>
                        <input type="password" name="password" validation="required" placeholder="<g:message code="password"/>">
                    </div>
                    <g:set var="loginConfig" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_LOGIN_SETTINGS)}"/>
                    <g:if test="${loginConfig.reset_password_active == "activated"}">
                        <div class="form-row lost-password-row">
                            <span class="lost-password"><a href="${app.relativeBaseUrl()}customer/resetPassword">${loginConfig.reset_password_label}</a></span>
                        </div>
                    </g:if>
                    <div class="form-row button-line">
                        <button type="submit" class="login-submit et_cartp_checkout_as_customer"><g:message code="login"/></button>
                    </div>
                </g:form>
            </div>
        </g:if>
    </div>
</div>
