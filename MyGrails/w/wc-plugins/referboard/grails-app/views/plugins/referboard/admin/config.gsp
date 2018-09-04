<form class="create-edit-form" action="${app.relativeBaseUrl()}setting/saveConfigurations" method="post">
    <input type="hidden" name="type" value="referboard">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="refer.board"/></h3>
            <div class="info-content"><g:message code="section.text.refer.board.setting.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <label><g:message code="enable"/></label>
                <input type="checkbox" class="single" name="referboard.is_enabled" value="true" ${configs.is_enabled == "true" ? "checked" : ""} toggle-target="api-key-row" />
            </div>
            <div class="form-row api-key-row">
                <label><g:message code="api.key"/><label>
                <input type="text" name="referboard.api_key" value="${configs.api_key}" validation="required@if{self::visible}"/>
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>