<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<form class="login-form valid-verify-form" action="${app.relativeBaseUrl()}customer/doLogin" method="post">
    <g:if test="${referer}">
        <input type="hidden" name="referer" value="${referer}">

        <script type="text/javascript">
            $(function () {
                var referer = $('input[name="referer"]');
                referer.val(referer.val() + window.location.hash);
            });
        </script>
    </g:if>
    <g:set var="enableFbLogin" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_REGISTRATION_SETTINGS, "enable_fb_login") == "true"}"/>
    <g:if test="${enableFbLogin && request.fb_login_script_loaded != true}">
        <app:enqueueSiteJs src="//connect.facebook.net/en_US/sdk.js" scriptId="facebook"/>
        <script type="text/javascript">
            app.fb_app_id = '${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_REGISTRATION_SETTINGS, "fb_app_id").encodeAsJavaScript()}'
        </script>
    </g:if>
    <g:set var="enableGoogleLogin" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_REGISTRATION_SETTINGS, "enable_google_login") == "true"}"/>
    <g:if test="${enableGoogleLogin && request.google_login_script_loaded != true}">
        <app:enqueueSiteJs src="https://apis.google.com/js/api:client.js" scriptId="google-login"/>
        <script type="text/javascript">
            app.google_client_id = '${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_REGISTRATION_SETTINGS, "google_client_id").encodeAsJavaScript()}'
            googleAyncInit();
        </script>
    </g:if>
    <span class="title"><g:message code="login"/></span>
    <div class="form-row mandatory">
        <label>${loginConfig.name_label}:</label>
        <input type="text" name="userName" validation="required email rangelength[4,50]" placeholder="<g:message code="email"/>">
    </div>
    <div class="form-row mandatory">
        <label>${loginConfig.password_label}:</label>
        <input type="password" name="password" validation="required" placeholder="<g:message code="password"/>">
    </div>
    <g:if test="${useCaptcha}">
        <ui:captcha/>
    </g:if>
    <div class="form-row submit-row">
        <button class="login-button"><g:message code="login"/></button>
    </div>
    <g:if test="${enableFbLogin}">
        <div class="form-row">
            <button type="button" class="login-with-fb-btn"><g:message code="login.with.facebook"/> </button>
        </div>
    </g:if>
    <g:if test="${enableGoogleLogin}">
        <div class="form-row">
            <button type="button" class="login-with-google-btn"><g:message code="login.with.google"/> </button>
        </div>
    </g:if>
    <div class="form-row remember">
        <label>&nbsp</label>
        <input type="checkbox" name="remember"> <g:message code="remember.me"/>
    </div>
    <g:if test="${loginConfig.reset_password_active == "activated"}">
        <div class="form-row">
            <span class="lost-password"><a href="${app.relativeBaseUrl()}customer/resetPassword">${loginConfig.reset_password_label}</a></span>
        </div>
    </g:if>
    <g:if test="${regStatus == "open" || regStatus == "awaiting_approval"}">
        <g:if test="${loginConfig.reg_link_active == "activated"}">
            <div class="form-row">
                <span class="no-account-label"><g:message code="dont.have.account"/></span>
                <span class="account-register"><a href="${app.relativeBaseUrl()}customer/register">${loginConfig.reg_link_label}</a></span>
            </div>
        </g:if>
    </g:if>
</form>