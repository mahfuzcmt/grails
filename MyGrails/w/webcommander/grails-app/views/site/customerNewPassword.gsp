<form class="customer-new-password valid-verify-form" action="${app.relativeBaseUrl()}customer/changePassword" method="post">
    <span class="title"><g:message code="new.password"/></span>
    <input type="hidden" name="token" value="${token}">
    <div class="form-row mandatory">
        <label><g:message code="new.password"/>:</label>
        <input type="password" id="customer-reset-new-password" name="password" validation="required rangelength[6,50]">
    </div>
    <div class="form-row mandatory">
        <label><g:message code="retype.password"/>:</label>
        <input type="password" class="match-password" validation="required compare[customer-reset-new-password, string, eq]" message_params="(password above)"/>
    </div>
    <div class="form-row submit-row">
        <label>&nbsp;</label>
        <button type="submit"><g:message code="reset"/></button>
    </div>
</form>