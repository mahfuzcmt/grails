<form action="${app.relativeBaseUrl()}setting/saveCustomerLoginFormSettings" method="post" id="loginPageSettingForm" class="create-edit-form">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="customer.login.form.fields"/></h3>
            <div class="info-content"><g:message code="section.text.customer.login.form.fields.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="login.name.label"/><span class="suggestion"><g:message code="suggestion.setting.login.page.name"/></span></label>
                    <input type="text" class="medium" name="customer_login.name_label" validation="required rangelength[2,50]" value="${loginConfig.name_label}">
                </div><div class="form-row mandatory">
                    <label><g:message code="password.label"/><span class="suggestion"><g:message code="suggestion.setting.login.page.password"/></span></label>
                    <input type="text" class="medium" name="customer_login.password_label" validation="required rangelength[2,50]"
                           value="${loginConfig.password_label}">
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <input type="hidden" name="field" value="reset_password_active">
                    <label><g:message code="reset.password.link"/></label>
                    <span class="edit-block">
                        <input type="checkbox" class="active single" name="reset_password_active"
                               value="${loginConfig.reset_password_active}" ${loginConfig.reset_password_active == "activated" ? 'checked="checked"' : ''}><g:message
                            code="active"/>
                    </span>
                </div><div class="form-row mandatory">
                    <label><g:message code="reset.password.label"/><span class="suggestion"><g:message code="suggestion.setting.login.page.reset.pass.label"/></span></label>
                    <input type="text" class="medium" name="customer_login.reset_password_label" validation="required rangelength[2,50]"
                           value="${loginConfig.reset_password_label}">
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <input type="hidden" name="field" value="reg_link_active">
                    <label><g:message code="reg.link"/></label>
                    <span class="edit-block">
                        <input type="checkbox" class="active single" name="reg_link_active"
                               value="${loginConfig.reg_link_active}" ${loginConfig.reg_link_active == "activated" ? 'checked="checked"' : ''}><g:message
                            code="active"/> <g:message code="registration.type.must.be.open.awaiting.approval"/>
                    </span>
                </div><div class="form-row mandatory">
                    <label><g:message code="reg.link.label"/><span class="suggestion"><g:message code="suggestion.setting.login.page.reg.link"/></span></label>
                    <input type="text" class="medium" name="customer_login.reg_link_label" validation="required rangelength[2,50]" value="${loginConfig.reg_link_label}">
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="fail.count"/><span class="suggestion"><g:message code="suggestion.setting.login.page.fail.count"/></span></label>
                    <input type="text" class="medium" restrict="decimal" name="customer_login.fail_count" validation="required digits max[10] min[1] maxlength[9]" maxlength="9" value="${loginConfig.fail_count}">
                </div><div class="form-row">
                    <label><g:message code="after.login"/><span class="suggestion">e.g. /customer/profile</span></label>
                    <input type="text" name="customer_login.after_login_url" validation="partial_url" value="${loginConfig.after_login_url}">
                </div>
            </div>
            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>