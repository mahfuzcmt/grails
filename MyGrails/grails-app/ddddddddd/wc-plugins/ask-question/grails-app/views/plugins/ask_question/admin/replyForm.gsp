<form class="edit-popup-form create-edit-form" action="${app.relativeBaseUrl()}askQuestionAdmin/answer">
    <input type="hidden" name="id" value="${question.id}">
    <div class="form-section">
        <div class="form-row">
            <label class="bold-text"><g:message code="product"/></label>
            <span class="info">${question.product.name.encodeAsBMHTML()}</span>
        </div>
        <div class="form-row">
            <label class="bold-text"><g:message code="question"/></label>
            <span class="info view-content-block description-view-block">${question.question.encodeAsBMHTML()}</span>
        </div>
        <div class="form-row">
            <label class="bold-text mandatory-label"><g:message code="reply"/></label>
            <textarea class="xx-large" name="answer" validation="required maxlength[1000]" maxlength="1000"></textarea>
        </div>
        <div class="form-row">
            <label class="bold-text"><g:message code="date"/></label>
            <span class="info">${question.created.toAdminFormat(true, false, session.timezone)}</span>
        </div>
        <div class="form-row">
            <label class="bold-text"><g:message code="email"/></label>
            <span class="info">${question.email.encodeAsBMHTML()}</span>
        </div>
        <div class="button-line">
            <button type="submit" class="submit-button"><g:message code="reply"/></button>
            <button type="button" class="cancel-button"><g:message code="cancel"/></button>
        </div>
    </div>
</form>