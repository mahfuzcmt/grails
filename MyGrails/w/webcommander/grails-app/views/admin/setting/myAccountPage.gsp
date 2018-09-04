<%@ page import="com.webcommander.constants.DomainConstants"%>
<form action="${app.relativeBaseUrl()}setting/saveConfigurations" method="post" class="fields-setting create-edit-form">
    <g:set var="configType" value="${DomainConstants.SITE_CONFIG_TYPES.MY_ACCOUNT_PAGE}"/>
    <input type="hidden" name="type" value="${configType}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="my.account.page"/></h3>
            <div class="info-content"><g:message code="section.text.setting.my.account.page"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <input type="checkbox" class="single" value="true" uncheck-value="false" name="${configType}.pending_order" ${config.pending_order == "true" ? "checked" : ""}>
                <span><g:message code="pending.order"/> </span>
            </div>
            <div class="form-row">
                <input type="checkbox" class="single" value="true" uncheck-value="false" name="${configType}.completed_order" ${config.completed_order == "true" ? "checked" : ""}>
                <span><g:message code="completed.order"/> </span>
            </div>
            <div class="form-row">
                <input type="checkbox" class="single" value="true" uncheck-value="false" name="${configType}.assigned_page" ${config.assigned_page == "true" ? "checked" : ""}>
                <span><g:message code="assigned.pages"/> </span>
            </div>
            <div class="form-row">
                <input type="checkbox" class="single" value="true" uncheck-value="false" name="${configType}.wallet" ${config.wallet == "true" ? "checked" : ""}>
                <span><g:message code="wallet"/> </span>
            </div>
            <div class="form-row">
                <input type="checkbox" class="single" value="true" uncheck-value="false" name="${configType}.store_credit" ${config.store_credit == "true" ? "checked" : ""}>
                <span><g:message code="store.credit"/> </span>
            </div>
            <plugin:hookTag hookPoint="myAccountPageSettings"/>
            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>