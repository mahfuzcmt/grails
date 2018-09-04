<form class="edit-popup-form" action="${app.relativeBaseUrl()}customFieldsAdmin/saveFieldTitle">
    <div class="form-row mandatory">
        <label><g:message code="title.group.field"/></label>
        <input type="text" value="${title.encodeAsHTML()}" name="title" validation="required maxlength[100]" maxlength="100" class="medium">
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="update"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>