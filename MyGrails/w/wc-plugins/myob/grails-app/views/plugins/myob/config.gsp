<form class="myob-config-form create-edit-form" action="${app.relativeBaseUrl()}myob/saveConfigurations" method="post" id="myobConfigForm">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="myob.config"/></h3>
            <div class="info-content"><g:message code="section.text.myob.config.info"/></div>
        </div>
        <div class="form-section-container">
            <input type="hidden" name="type" value="myob">
            <input type="hidden" name="isSetting" value="true">
            <div class="form-row">
                <label><g:message code="version"/><span class="suggestion"></span></label>
                <select name="myob.app_version" toggle-target="version">
                    <option ${config.app_version == "19.x" ? "selected='selected'" : ""}>19.x</option>
                    <option ${config.app_version == "21.x" ? "selected='selected'" : ""}>21.x</option>
                </select>
            </div>
            <div class="version-21_x">
                <div class="double-input-row">
                    <div class="form-row">
                        <label><g:message code="client.id"/><span class="suggestion">e.g. petersmith</span></label>
                        <input type="text" name="myob.client_id" value="${config.client_id}">
                    </div><div class="form-row">
                        <label><g:message code="client.secret"/><span class="suggestion">e.g. enter Secret</span></label>
                        <input type="text" name="myob.client_secret" value="${config.client_secret}">
                    </div>
                </div>
                <div class="form-row mandatory">
                    <label><g:message code="redirect.uri"/><span class="suggestion">e.g. http://abc.com</span></label>
                    <input type="text" class="medium" name="myob.redirect_uri" value="${config.redirect_uri ?: "${app.nonRequestBaseUrl()}myob/callback"}" validation="skip@hidden required url">
                </div>
                <div class="form-row">
                    <button type="button" class="authorize" url="${app.relativeBaseUrl()}myob/authorize"><g:message code="authorize"/></button>
                </div>
            </div>
            <div class="form-row version-19_x">
                <button type="submit" class="submit-button myob-config-form-submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
    <div class="section-separator version-21_x"></div>
    <div class="form-section version-21_x">
        <div class="form-section-info">
            <h3><g:message code="myob.company.file.settings"/></h3>
            <div class="info-content"><g:message code="section.text.myob.company.file.settings.info"/></div>
        </div>
        <div class="form-section-container">
            <input type="hidden" name="type" value="myob"/>
            <div class="double-input-row">
                <div class="form-row chosen-wrapper">
                    <div class="form-row-content-wrapper">
                        <label><g:message code="company.file"/><span class="suggestion"></span></label>
                        <myob:companyFileSelector class="medium" name="myob.company_file_uri" value="${config.company_file_uri}"/>
                    </div>
                </div><div class="form-row">
                    <label><g:message code="company.file.username"/><span class="suggestion">e.g. Administrator</span></label>
                    <input class="medium" type="text" name="myob.company_file_username" value="${config.company_file_username}">
                </div>
            </div>
            <div class="form-row">
                <label><g:message code="company.file.password"/></label>
                <input class="medium" type="password" name="myob.company_file_password" value="${config.company_file_password}" >
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button myob-config-form-submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>