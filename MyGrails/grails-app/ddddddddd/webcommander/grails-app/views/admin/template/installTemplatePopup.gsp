<form action="${app.relativeBaseUrl()}templateAdmin/install" method="post" class="edit-popup-form">
    <input type="hidden" name="id" value="${params.id}">
    <input type="hidden" name="color" value="${params.color}">
    <div class="install-template-popup-content">
        <div class="warning-box first-option-warning">
            <g:message code="install.template.first.option.warning.message"/>
        </div>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="confirm"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>