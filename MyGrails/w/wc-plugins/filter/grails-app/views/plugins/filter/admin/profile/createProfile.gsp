<div class="profile-editor-panel">
    <form class="edit-popup-form" action="${app.relativeBaseUrl()}filterAdmin/saveFilterProfile">
        <input type="hidden" name="id" value="${profile.id}">
        <div class="form-row mandatory">
            <label><g:message code="name"/> </label>
            <input type="text" value="${profile.name}" name="name" class="unique" validation="required maxlength[100]" maxlength="100" unique-action="isFilterUnique">
        </div>
        <div class="form-row">
            <input type="checkbox" class="single" name="isDefault" uncheck-value="false" value="true" ${profile.isDefault ? "checked='checked'" : ""}>
            <span><g:message code="make.default.profile"/></span>
        </div>
        <div class="form-row">
            <label toggle-target="description" row-expanded="false"><g:message code="description"/></label>
            <textarea class="description" name="description" validation="maxlength[500]" maxlength="500">${profile.description}</textarea>
        </div>
        <div class="button-line">
            <button type="submit" class="submit-button"><g:message code="${profile.id ? 'update' : 'add'}"/></button>
            <button type="button" class="cancel-button"><g:message code="cancel"/></button>
        </div>
    </form>
</div>