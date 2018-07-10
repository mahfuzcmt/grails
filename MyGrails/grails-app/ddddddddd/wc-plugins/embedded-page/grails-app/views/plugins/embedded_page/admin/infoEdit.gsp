<form method="post" action="${app.relativeBaseUrl()}embeddedPage/save" class="edit-popup-form create-edit-page">
    <input type="hidden" name="id" value="${page.id}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="embedded.page.info"/></h3>
            <div class="info-content"><g:message code="section.text.embedded.page.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row mandatory">
                <label><g:message code="name"/><span class="suggestion"><g:message code="suggestion.embedded.page.name"/></span></label>
                <input type="text" class="medium unique" name="name" value="${page.name?.encodeAsBMHTML()}" validation="required rangelength[2,100]" maxlength="100">
            </div>
            <div class="form-row mandatory">
                <label><g:message code="dom.id"/><span class="suggestion"><g:message code="suggestion.embedded.page.dom.id"/></span></label>
                <input type="text" class="medium unique" name="domId" validation="required rangelength[2,100] alphanumeric" value="${page.domId?.encodeAsBMHTML()}" maxlength="100">
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="${page.id ? "update" : "save"}"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</form>