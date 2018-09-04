<form action="${app.relativeBaseUrl()}customerAdmin/changePass" method="post" class="edit-popup-form">
    <input type="hidden" name="id" value="${customer.id}">
    <div class="form-row mandatory">
        <label><g:message code="new.password"/>:</label>
        <input type="password" id="customer-reset-new-password" name="password" validation="required rangelength[6,50]">
    </div>
    <div class="form-row mandatory">
        <label><g:message code="retype.password"/>:</label>
        <input type="password" class="match-password" validation="required compare[customer-reset-new-password, string, eq]" message_params="(password above)"/>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="reset"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>