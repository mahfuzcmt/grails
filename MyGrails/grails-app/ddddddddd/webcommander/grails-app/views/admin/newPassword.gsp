<g:applyLayout name="login">
    <div class="admin-login reset-pass"><g:message code="reset.password"/></div>
    <form action="${app.relativeBaseUrl()}userAuthentication/changePassword" method="post" class="authentication-form new-password-form" error-position="inline">
        <input type="hidden" name="token" value="${token}">
        <div id="new-password" class="form-row input-row inline-label">
            <span class="icon-wrap">
                <i class="icon password"></i>
            </span>
            <g:passwordField id="user-reset-new-password" class="new-password" name="password" placeholder="${message(code: 'new.password')}" validation="required rangelength[6,50]"/>
        </div>
        <div id="retype-password" class="form-row input-row inline-label">
            <span class="icon-wrap">
                <i class="icon retype-password"></i>
            </span>
            <input type="password" class="match-password" validation="required compare[user-reset-new-password, string, eq]" placeholder="${message(code: 'retype.password')}" message_params="(password above)"/>
        </div>
        <div class="form-row submit-row">
            <button class="login-button"><g:message code="reset"/></button>
        </div>
    </form>
</g:applyLayout>
