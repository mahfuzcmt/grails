<%@ page import="com.webcommander.constants.DomainConstants" %>
<form class="google-analytics-profile create-edit-form" action="${app.relativeBaseUrl()}setting/saveConfigurations" method="POST">
    <g:set var="type" value="${DomainConstants.SITE_CONFIG_TYPES.GOOGLE_ANALYTICS}"/>
    <input type="hidden" name="type" value="${type}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="google.analytics.info"/></h3>
            <div class="info-content"><g:message code="section.text.settings.google.analytics.profile.config"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <label><g:message code="profile"/></label>
                <select class="medium" name="${type}.profile">
                    <g:each in="${profiles}" var="account">
                        <optgroup label="${account.key}">
                            <g:each in="${account.value}" var="profile">
                                <option value="${profile.key}" ${config.profile == profile.key ? 'selected' : ''}>${profile.value}</option>
                            </g:each>
                        </optgroup>
                    </g:each>
                </select>
            </div>
            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>