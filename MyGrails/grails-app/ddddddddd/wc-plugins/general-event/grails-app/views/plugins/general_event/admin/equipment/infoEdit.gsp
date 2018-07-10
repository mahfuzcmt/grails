<form action="${app.relativeBaseUrl()}generalEventAdmin/saveEquipment" method="post" class="create-edit-form">
    <input type="hidden" name="id" value="${equipment.id}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="equipment.info"/> </h3>
            <div class="info-content"><g:message code="form.section.text.equipment.info"/> </div>
        </div>
        <div class="form-section-container">
            <div class="form-row mandatory">
                <label><g:message code="equipment.name"/><span class="suggestion"><g:message code="suggestion.equipment.name"/></span></label>
                <input type="text" name="name" class="large unique" value="${equipment?.name}" validation="required rangelength[2, 100]" unique-action="isEquipmentUnique" maxlength="100">
            </div>
            <div class="form-row">
                <label><g:message code="description"/></label>
                <textarea class="large" name="description" validation="maxlength[500]" maxlength="500">${equipment.description}</textarea>
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="${equipment.id ? "update" : "save"}"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</form>