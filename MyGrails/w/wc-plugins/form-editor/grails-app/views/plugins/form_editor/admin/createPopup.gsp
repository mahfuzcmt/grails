<div class="form-editor-panel">
    <div class="edit-popup-form">
        <div class="form-templates">
            <g:each in="${templates}" var="template">
                <div template-id="${template.id}" class="form-template">
                    <span class="icon ${template.id}"></span>
                    <span class="label"><g:message code="${template.name}"/></span>
                </div>
            </g:each>
        </div>
        <div class="button-line">
            <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="create"/> </button>
            <button type="button" class="cancel-button"><g:message code="cancel"/></button>
        </div>
    </div>
</div>