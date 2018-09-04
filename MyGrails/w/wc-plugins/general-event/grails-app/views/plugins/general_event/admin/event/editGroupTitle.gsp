<form class="edit-popup-form" action="${app.relativeBaseUrl()}generalEventAdmin/saveCustomFieldTitle">
    <div class="form-row mandatory">
        <label><g:message code="title.group.field"/></label>
        <input type="text" value="${title?.title.encodeAsHTML()}" name="title" validation="required maxlength[255]" maxlength="250" class="medium">
    </div>
    <input type="hidden" name="eventId" value="${pageScope['eventId']}">
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="update"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>