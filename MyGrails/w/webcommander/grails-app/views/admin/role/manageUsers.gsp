<form class="edit-popup-form" action="${app.relativeBaseUrl()}role/updateUsers" method="post">
    <input type="hidden" name="id" value="${role.id}">
    <div class="form-row chosen-wrapper">
        <label><g:message code="operators"/></label>
        <g:select name="users" class="medium" multiple="" from="${users.fullName}" keys="${users.id}" value="${role.users.id}"></g:select>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="update"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>