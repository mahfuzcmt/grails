<form class="edit-popup-form" action="${app.relativeBaseUrl()}productCustomField/saveFieldTitle">
    <div class="form-row">
        <label><g:message code="title.group.field"/></label>
        <input type="text" value="${title?.title.encodeAsHTML()}" name="title" validation="maxlength[255]" maxlength="250" class="medium">
    </div>
    <input type="hidden" name="${type}Id" value="${pageScope[type + 'Id']}">
    <input type="hidden" name="type" value="${type}">
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="update"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>