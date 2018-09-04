<form action="${app.relativeBaseUrl()}setting/saveConfigurations" method="POST" onsubmit="return false" id="frmEmailSetting" class="create-edit-form">
    <input type="hidden" name="type" value="email">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="email.settings"/></h3>
            <div class="info-content"><g:message code="section.text.setting.email"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="sender.name"/><span class="suggestion"><g:message code="suggestion.setting.email.sender"/></span></label>
                    <input type="text" class="medium" name="email.sender_name" validation="required rangelength[2,100]" value="${email.sender_name.encodeAsBMHTML()}">
                </div><div class="form-row mandatory">
                <label><g:message code="sender.email.address"/><span class="suggestion"><g:message code="suggestion.setting.email.address"/></span></label>
                <input type="text" class="medium" name="email.sender_email" validation="required email rangelength[6,100]" value="${email.sender_email.encodeAsBMHTML()}">
            </div>
            </div>
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="smtp.host"/><span class="suggestion"><g:message code="suggestion.setting.email.smtp"/></span></label>
                    <input type="text" class="medium" name="email.smtp_host" validation="required maxlength[100]" value="${email.smtp_host.encodeAsBMHTML()}">
                </div><div class="form-row mandatory">
                <label><g:message code="smtp.port"/><span class="suggestion"><g:message code="suggestion.setting.email.smtp.port"/></span></label>
                <input type="text" restrict="numeric" class="medium" name="email.smtp_port" validation="required maxlength[9] number" maxlength="9" value="${email.smtp_port.encodeAsBMHTML()}">
            </div>
            </div>
            <div class="double-input-row">
                <div class="form-row chosen-wrapper">
                    <label><g:message code="has.encryption"/>?</label>
                    <g:select class="medium" name="email.smtp_encryption" from="['No Encryption','SSL','TLS']" keys="['no','ssl','starttls']" value="${email.smtp_encryption}"/>
                </div><div class="form-row">
                <label><g:message code="has.authentication"/>?</label>
                <div class="multiple">
                    <span class="side">
                        <input type="radio" name="email.has_smtp_authentication" class="radio" value="true" ${email.has_smtp_authentication == 'true' ? 'checked' : ''}>
                        <g:message code="yes"/>
                    </span>
                    <span class="side">
                        <input type="radio" name="email.has_smtp_authentication" class="radio" value="false" ${email.has_smtp_authentication == 'true' ? '' : 'checked'}>
                        <g:message code="no"/>
                    </span>
                </div>
            </div>
            </div>
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="user.name"/><span class="suggestion">e.g. peter.smith@abc.com</span></label>
                    <input class="medium" type="text" validation="required maxlength[100]" name="email.smtp_username" value="${email.smtp_username.encodeAsBMHTML()}">
                </div><div class="form-row mandatory">
                <label><g:message code="smtp.password"/><span class="suggestion"></span></label>
                <input class="medium" type="password" validation="required maxlength[100]" name="email.smtp_password" value="${email.smtp_password.encodeAsBMHTML()}">
            </div>
            </div>
            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>