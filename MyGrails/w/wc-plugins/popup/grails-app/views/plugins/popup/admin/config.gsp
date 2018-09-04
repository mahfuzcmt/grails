<%@ page import="com.webcommander.plugin.popup.constants.Constants; com.webcommander.plugin.popup.Popup; com.webcommander.constants.DomainConstants" %>
<form action="${app.relativeBaseUrl()}setting/saveConfigurations" method="post" class="create-edit-form">
    <g:set var="type" value="${DomainConstants.SITE_CONFIG_TYPES.POPUP}"/>
    <input type="hidden" name="type" value="${type}">
    <input type="hidden" name="${type}.config_version" value="${config.config_version}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="popup"/></h3>
            <div class="info-content"><g:message code="section.text.setting.popup"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <label><g:message code="initial.popup"/></label>
                <ui:domainSelect domain="${Popup}" name="${type}.initial_popup" prepend="${["": g.message(code: "none")]}" value="${config.initial_popup.toLong(0)}"/>
            </div>
            <div class="form-row">
                <label><g:message code="loading.frequency"/></label>
                <ui:namedSelect key="${Constants.FREQUENCY_NAMES}" name="${type}.loading_frequency" value="${config.loading_frequency}"/>
            </div>
            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>