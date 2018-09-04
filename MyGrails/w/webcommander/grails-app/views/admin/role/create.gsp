<form class="create-edit-form edit-popup-form" action="${app.relativeBaseUrl()}role/save" method="post">

    <input type="hidden" name="id" value="${role.id}"/>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="role.info"/></h3>
            <div class="info-content"><g:message code="section.text.crate.role.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="mandatory form-row">
                <label><g:message code="role.name"/><span class="suggestion"><g:message code="create.role.suggestion"/></span></label>
                <input type="text" class="medium unique" name="name" validation="required rangelength[2,100]" value="${role.name.encodeAsBMHTML()}" maxlength="100">
            </div>

            <div class="form-row">
                <label><g:message code="role.description"/><span class="suggestion"><g:message code="create.role.description.suggestion"/></label>
                <input type="text" class="medium" validation="maxlength[255]" value="${role.description.encodeAsBMHTML()}" name="description" maxlength="255">
            </div>

            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="${role.id ? "update" : "save"}"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</form>