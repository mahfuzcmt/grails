<%@ page import="com.webcommander.constants.DomainConstants" %>
<%--
  Created by IntelliJ IDEA.
  User: sajedur
  Date: 5/28/2015
  Time: 3:46 PM
--%>
<form action="${app.relativeBaseUrl()}setting/saveConfigurations" method="POST" class="create-edit-form">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="quote"/></h3>
            <div class="info-content"><g:message code="section.text.quote.settings"/></div>
        </div>
        <g:set var="type" value="${DomainConstants.SITE_CONFIG_TYPES.QUOTE}"/>
        <div class="form-section-container">
            <input type="hidden" name="type" value="${type}">
            <div class="form-row">
                <label><g:message code="enable.quote"/></label>
                <input type="checkbox" name="${type}.enabled" class="single" value="true" uncheck-value="false" ${config.enabled == "true" ? "checked" : ""}>
            </div>
            <div class="form-row">
                <label><g:message code="button.label"/></label>
                <input type="text" name="${type}.button_label" value="${config.button_label}" validation="required">
            </div>
            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>