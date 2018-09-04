<g:applyLayout name="login">
    <div class="admin-login reset-pass"><g:message code="reset.password"/></div>
    <form action="${app.relativeBaseUrl()}userAuthentication/passwordResetLink" method="post" class="authentication-form reset-password-form" error-position="inline">
        <div class="lost-password-text">
            <g:message code="password.reset.link.send.email"/>
        </div>
        <div class="form-row" id="login-email">
            <span class="icon-wrap">
                <i class="icon user-email"></i>
            </span>
            <g:textField name="userEmail" maxlength="50" placeholder="${message(code: 'email')}" validation="required email"/>
        </div>
        <div class="form-row submit-row">
            <button class="login-button"><g:message code="get.password.now"/></button>
        </div>
    </form>
</g:applyLayout>