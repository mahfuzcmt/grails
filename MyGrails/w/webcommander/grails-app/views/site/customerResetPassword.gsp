<form class="password-reset-form valid-verify-form" action="${app.relativeBaseUrl()}customer/passwordResetLink" method="post">
    <span class="title"><g:message code="reset.password.link"/></span>
    <div class="lost-password-text">
        <g:message code="password.reset.link.send.email"/>
    </div>
    <div class="form-row mandatory">
        <label><g:message code="customer.email"/>:</label>
        <input type="text" name="userName" maxlength="50" validation="required email">
    </div>
    <div class="form-row submit-row">
        <button class="submit-button"><g:message code="send.now"/></button>
    </div>
</form>