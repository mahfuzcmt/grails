<form class="facebook-config-form create-edit-form" action="${app.relativeBaseUrl()}facebook/saveConfigs" method="post" id="facebookConfigForm">
    <input type="hidden" name="type" value="facebook">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="fb.app.credentials"/></h3>
            <div class="info-content"><g:message code="section.text.settings.fb"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <label><g:message code="fb.app.id"/><span class="suggestion"><g:message code="appid.facebook.where.used"/></span></label>
                <input type="text" name="facebook.appId" class="medium" value="${config.appId.encodeAsBMHTML()}">
            </div>
            <div class="form-row">
                <label><g:message code="fb.app.secret"/><span class="suggestion"><g:message code="appsecret.facebook.where.used"/></span></label>
                <input type="password" name="facebook.appSecret" class="medium" value="${config.appSecret.encodeAsBMHTML()}">
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button myob-config-form-submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>