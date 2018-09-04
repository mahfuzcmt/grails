<form action="${app.relativeBaseUrl()}eventAdmin/saveTopic" method="post" class="edit-popup-form create-edit-form">
    <input type="hidden" name="id" value="${topic.id}">
    <input type="hidden" name="sessionId" value="${sessionId}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="session.topic.information"/></h3>
            <div class="info-content"><g:message code="section.text.session.topic.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row mandatory">
                <label><g:message code="name"/></label>
                <input type="text" class="large" name="name" value="${topic.name.encodeAsBMHTML()}" maxlength="100" validation="required rangelength[2,100]">
            </div>
            <div class="form-row">
                <label><g:message code="description"/></label>
                <textarea class="large" name="description" maxlength="500"  validation="maxlength[500]">${topic.description.encodeAsBMHTML()}</textarea>
            </div>
            <div class="button-line">
                <button type="submit" class="submit-button"><g:message code="${topic.id ? 'update' : 'save'}"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</form>