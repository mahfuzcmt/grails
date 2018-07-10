<form class="edit-popup-form" action="${app.relativeBaseUrl()}user/updateRoles" method="post">
    <div class="form-row chosen-wrapper">
        <label><g:message code="roles"/></label>
        <g:select name="roles" class="medium" multiple="" from="${roles.name}" keys="${roles.id}" value="" validation="required"></g:select>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="update"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>