<form class="create-edit-form" action="${app.relativeBaseUrl()}setting/saveConfigurations" method="post">
    <input type="hidden" name="type" value="location">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="location"/></h3>
            <div class="info-content"><g:message code="section.text.location.setting.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row thicker-row chosen-wrapper">
                <label><g:message code="api.key"/></label>
                <input type="text" name="location.api_key" value="${configs.api_key}">
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>