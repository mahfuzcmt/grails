<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.plugin.sendle.constants.Constants" %>

<form action="${app.relativeBaseUrl()}setting/saveConfigurations" method="POST" class="create-edit-form">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="sendle"/></h3>
            <div class="info-content"><g:message code="section.text.sendle.settings"/></div>
        </div>
        <g:set var="type" value="${DomainConstants.SITE_CONFIG_TYPES.SENDLE}"/>
        <div class="form-section-container">
            <input type="hidden" name="type" value="${type}">
            <div class="double-input-row">
                <div class="form-row chosen-wrapper">
                    <label><g:message code="mode"/></label>
                    <ui:namedSelect key="${Constants.MODES}" name="${type}.mode" value="${config.mode}" />
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="sendle.id"/></label>
                    <input type="text" name="${type}.sendleId" value="${config.sendleId}" validation="required">
                </div>
                <div class="form-row mandatory">
                    <label><g:message code="api.key"/></label>
                    <input type="text" name="${type}.apiKey" value="${config.apiKey}" validation="required">
                </div>
            </div>
            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>