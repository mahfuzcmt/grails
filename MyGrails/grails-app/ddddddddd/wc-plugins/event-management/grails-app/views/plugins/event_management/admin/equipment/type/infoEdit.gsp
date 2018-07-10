<%@ page import="com.webcommander.admin.Operator" %>
<form action="${app.relativeBaseUrl()}eventAdmin/saveEquipmentType" method="post" class="create-edit-form">
    <input type="hidden" name="id" value="${type.id}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="equipment.type.info"/> </h3>
            <div class="info-content"><g:message code="form.section.text.equipment.type.info"/> </div>
        </div>
        <div class="form-section-container">
            <div class="form-row mandatory">
                <label><g:message code="name"/><span class="suggestion">e.g. Sound System</span></label>
                <input type="text" name="name" class="large unique" value="${type?.name}" validation="required rangelength[2, 100]" maxlength="100" unique-action="isEquipmentTypeUnique">
            </div>
            <div class="form-row">
                <label><g:message code="description"/><span class="suggestion">e.g. BAC Sound System</span></label>
                <textarea class="large" name="description" validation="maxlength[500]" maxlength="500">${type.description}</textarea>
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="${type.id ? "update" : "save"}"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</form>