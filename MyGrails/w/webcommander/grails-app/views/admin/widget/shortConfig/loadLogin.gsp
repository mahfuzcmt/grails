<g:applyLayout name="_widgetShortConfig">
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="login.name.label"/></div>
        <div class="sidebar-group-body">
            <input type="text" class="sidebar-input" name="name_label" value="${config.name_label.encodeAsBMHTML()}">
        </div>
    </div>
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="password.label"/></div>
        <div class="sidebar-group-body">
            <input type="text" class="sidebar-input" name="password_label" value="${config.password_label.encodeAsBMHTML()}">
        </div>
    </div>
    <div class="sidebar-group">
        <div class="sidebar-group-body">
            <input type="checkbox" class="single" name="reset_password_active" value="activated" uncheck-value="deactivated" ${config.reset_password_active == "activated" ? 'checked="checked"' : ''}>
            <label><g:message code="reset.password.link" /></label>
        </div>
    </div>
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="reset.password.label"/></div>
        <div class="sidebar-group-body">
            <input type="text" class="sidebar-input" name="reset_password_label" value="${config.reset_password_label.encodeAsBMHTML()}">
        </div>
    </div>
    <div class="sidebar-group">
        <div class="sidebar-group-body">
            <input type="checkbox" class="single" name="reg_link_active" value="activated" uncheck-value="deactivated" ${config.reg_link_active == "activated" ? 'checked="checked"' : ''}>
            <label><g:message code="reg.link"/></label>
        </div>
    </div>
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="reg.link.label"/></div>
        <div class="sidebar-group-body">
            <input type="text" class="sidebar-input" name="reg_link_label" value="${config.reg_link_label.encodeAsBMHTML()}">
        </div>
    </div>
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="after.failure"/></div>
        <div class="sidebar-group-body">
            <div>
                <input type="radio" name="after_failure" value="login_page"  ${config.after_failure == "login_page" ? "checked" : ""} >
                <label><g:message code="move.to.login.page"/></label>
            </div>
            <div>
                <input type="radio" name="after_failure" value="same_page" ${config.after_failure ? (config.after_failure == "same_page" ? "checked" : "") : "checked"}>
                <label><g:message code="stay.on.same.page"/></label>
            </div>
        </div>
    </div>
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="after.login"/></div>
        <div class="sidebar-group-body">
            <div>
                <input type="radio" name="after_login" value="profile_page" ${config.after_login == "profile_page" ? "checked" : ""}>
                <label><g:message code="move.to.profile.page"/></label>
            </div>
            <div>
                <input type="radio" name="after_login" value="same_page" ${config.after_login ? (config.after_login == "same_page" ? "checked" : "") : "checked"}>
                <label><g:message code="stay.on.same.page"/></label>
            </div>
        </div>
    </div>
</g:applyLayout>