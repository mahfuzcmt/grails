<g:applyLayout name="login">
    <img src="${app.systemResourceBaseUrl()}images/admin/login-user.png" class="login-user-icon">
    <div class="admin-login"><g:message code="admin.login"/></div>
    <form action="${app.relativeBaseUrl()}userAuthentication/login" method="post" class="authentication-form login-form" error-position="inline">
        <input name="redirectUrl" type="hidden" value="${params.redirectUrl}">
        <div class="form-row" id="login-username">
            <span class="icon-wrap">
                <i class="icon user-name"></i>
            </span>
            <g:textField id="user-name" name="email" maxlength="50" validation="required email" placeholder="${message (code :'operator.email')}" autofocus="true"/>
        </div>
        <div class="form-row" id="login-password">
            <span class="icon-wrap">
                <i class="icon password"></i>
            </span>
            <input type="password" id="password" name="password" maxlength="50" placeholder="<g:message code="password"/>" validation="required rangelength[4,50]">
        </div>
        <div class="form-row remember-forget-pass">
            <span class="remember-label"><g:message code="remember.me"/></span> <input type="checkbox" name="remember" class="multiple">
            <span class="lost-password"><a href="${app.relativeBaseUrl()}userAuthentication/resetPassword"><g:message code="lost.your.password"/></a></span>
        </div>
        <div class="form-row submit-row">
            <button class="login-button"><g:message code="login"/></button>
        </div>
    </form>
</g:applyLayout>