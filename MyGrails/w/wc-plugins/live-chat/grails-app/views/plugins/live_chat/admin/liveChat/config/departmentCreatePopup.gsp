<form class="edit-popup-form" action="${app.relativeBaseUrl()}liveChatAdmin/saveOrUpdateChatDepartment" method="post">
    <input type="hidden" class="medium" name="id" value="${chatDepartment ? chatDepartment.id : ""}">
    <div class="form-row">
        <label><g:message code="name"/></label>
        <input type="text" class="medium" name="name" value="${chatDepartment? chatDepartment.name : ""}" validation="required rangelength[2,100]" maxlength="100">
    </div>
    <div class="form-row">
        <label><g:message code="description"/></label>
        <input type="text" class="medium" name="description" value="${chatDepartment? chatDepartment.description : ""}" validation="required rangelength[2,300]" maxlength="300">
    </div>
    <div class="form-row">
        <label><g:message code="default.welcome.message"/></label>
        <textarea class="medium" name="defaultWelcomeMessage" validation="rangelength[2,550]" maxlength="550">${chatDepartment? chatDepartment.defaultWelcomeMessage : ""}</textarea>
    </div>
    <div class="form-row chosen-wrapper">
        <label><g:message code="agents"/></label>
        <g:select name="operators" class="medium" multiple="" from="${operators.fullName}" keys="${operators.id}" value="${chatDepartment ? chatDepartment.operators?.id : ""}" validation="required"></g:select>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="${chatDepartment ? "update" : "save"}"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>