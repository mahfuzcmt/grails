<g:form class="edit-popup-form" controller="setting" action="sendMail">
    <input type="hidden" name="templateId" value="${params.id}">
    <div class="form-row mandatory">
        <label><g:message code="email"/></label>
        <input type="text" class="medium" name="recipient" validation="required email">
    </div>
    <div class="form-row button-line">
        <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="send"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</g:form>