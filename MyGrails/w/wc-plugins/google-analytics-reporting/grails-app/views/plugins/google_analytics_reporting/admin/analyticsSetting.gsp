<%@ page import="com.webcommander.constants.DomainConstants" %>
<form class="google-analytics-authorize" action="${app.relativeBaseUrl()}setting/saveConfigurations" method="POST">
    <g:set var="type" value="${DomainConstants.SITE_CONFIG_TYPES.GOOGLE_ANALYTICS}"/>
    <input type="hidden" name="type" value="${type}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="google.analytics"/></h3>
            <div class="info-content"><g:message code="section.text.settings.google.analytics.config"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row mandatory">
                <label><g:message code="application.name"/><span class="suggestion"><g:message code="ga.application.name.report.which.for"/></span></label>
                <input type="text" class="medium" name="${type}.application_name" validation="required rangelength[2,50]" value="${config.application_name}">
            </div>
            <div class="form-row mandatory">
                <label><g:message code="client.id"/><span class="suggestion"><g:message code="ga.client.id.authorize.ga.api"/></span></label>
                <input type="text" class="medium" name="${type}.client_id" validation="required rangelength[2,100]" value="${config.client_id}">
            </div>
            <div class="form-row mandatory">
                <label><g:message code="client.secret"/><span class="suggestion"><g:message code="ga.client.secret.authorize.ga.api"/></span></label>
                <input type="text" class="medium" name="${type}.client_secret" validation="required rangelength[2,100]" value="${config.client_secret}">
            </div>
            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="authorized"/></button>
            </div>
        </div>
    </div>
</form>