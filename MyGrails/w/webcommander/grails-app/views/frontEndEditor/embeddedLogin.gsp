<div class="fee-login-panel">
    <img src="${app.systemResourceBaseUrl()}images/admin/login-user.png" class="login-user-icon">
    <div class="fee-admin-login"><g:message code="admin.login"/></div>
    <form action="${app.relativeBaseUrl()}userAuthentication/login_embed" method="post" error-position="inline">
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
        <div class="form-row remember-forget-pass">
            <label class="remember-label">
                <input type="checkbox" name="remember" class="multiple"><g:message code="remember.me"/>
            </label>
            <span class="lost-password"><a href="${app.relativeBaseUrl()}userAuthentication/resetPassword"><g:message code="lost.your.password"/></a></span>
        </div>
        <div class="form-row submit-row">
            <button class="login-button"><g:message code="login"/></button>
        </div>
    </form>
</div>