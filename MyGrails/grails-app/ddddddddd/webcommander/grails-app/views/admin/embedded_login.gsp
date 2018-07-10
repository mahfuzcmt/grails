<form action="${app.relativeBaseUrl()}userAuthentication/login_embed" method="post" class="authentication-form login-form" error-position="inline">
    <div class="form-row" id="login-username">
        <span class="icon-wrap">
            <i class="icon user-name"></i>
        </span>
        <g:textField name="email" maxlength="50" validation="required" placeholder="${message(code: 'operator.email')}"/>
    </div>
    <div class="form-row" id="login-password">
        <span class="icon-wrap">
            <i class="icon password"></i>
        </span>
        <input type="password" id="password" name="password" maxlength="50" placeholder="<g:message code="password"/>" validation="required rangelength[4,50]">
    </div>
    <div class="form-row change-login">
        <span class="not-admin"></span>
        <a class="link" href="${app.baseUrl()}userAuthentication/logout"><g:message code="login.as.different.operator"/></a>
    </div>
    <div class="form-row submit-row">
        <button class="login-button"><g:message code="login"/></button>
    </div>
</form>