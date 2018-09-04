<%@ page import="com.webcommander.constants.DomainConstants" %>
<form action="${app.relativeBaseUrl()}setting/saveConfigurations" method="post" class="fields-setting create-edit-form">
    <g:set var="configType" value="${DomainConstants.SITE_CONFIG_TYPES.STORE_CREDIT}"/>
    <input type="hidden" name="type" value="${configType}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="store.credit"/></h3>
            <div class="info-content"><g:message code="section.text.setting.store.credit"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <label><g:message code="apply.by.default"/> </label>
                <input type="checkbox" class="single" value="true" uncheck-value="false" name="${configType}.apply_by_default" ${config.apply_by_default == "true" ? "checked" : ""}>
            </div>
            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>