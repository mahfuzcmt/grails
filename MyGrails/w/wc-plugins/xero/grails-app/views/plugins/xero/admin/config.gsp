<%@ page import="com.webcommander.constants.DomainConstants" %>
<form action="${app.relativeBaseUrl()}xero/connectionConfig" method="POST" class="create-edit-form">
    <g:set var="type" value="${DomainConstants.SITE_CONFIG_TYPES.XERO}"/>
    <input type="hidden" name="type" value="${type}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="xero.configuration.info"/></h3>
            <div class="info-content"><g:message code="section.text.xero.config"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="consumer.key"/><span class="suggestion"><g:message code="suggestion.setting.xero.consumar.key"/></span></label>
                    <input type="text" name="${type}.consumer_key" class="large" value="${config["consumer_key"]}" validation="required">
                </div><div class="form-row mandatory">
                    <label><g:message code="consumer.secret"/><span class="suggestion"><g:message code="suggestion.setting.xero.consumar.secret"/></span></label>
                    <input type="text" name="${type}.consumer_secret" class="large" value="${config["consumer_secret"]}" validation="required">
                </div>
            </div>
            <div class="form-row mandatory">
                <label><g:message code="private.key"/><span class="suggestion"><g:message code="suggestion.setting.xero.private.key"/></span></label>
                <textarea name="${type}.private_key" validation="required" class="large">${config["private_key"]}</textarea>
            </div>
            <div class="form-row">
                <button class="submit-button"><g:message code="save"/></button>
            </div>
        </div>
    </div>
</form>