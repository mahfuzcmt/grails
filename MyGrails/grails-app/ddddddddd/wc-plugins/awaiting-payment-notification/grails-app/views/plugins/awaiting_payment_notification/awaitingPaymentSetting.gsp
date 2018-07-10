<%@ page import="com.webcommander.constants.NamedConstants; com.webcommander.constants.DomainConstants" %>
<form action="${app.relativeBaseUrl()}setting/saveConfigurations" method="post" class="fields-setting create-edit-form">
    <g:set var="configType" value="${DomainConstants.SITE_CONFIG_TYPES.AWAITING_PAYMENT}"/>
    <input type="hidden" name="type" value="${configType}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="awaiting.payment"/></h3>
            <div class="info-content"><g:message code="section.text.setting.awaiting.payment"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <label><g:message code="no.of.max.time"/><span class="suggestion">e.g. 5</span></label>
                <input type="text" name="${configType}.no_of_max_time" value="${config.no_of_max_time}" validation="required digits" restrict="numeric">
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <label><g:message code="interval"/><span class="suggestion">e.g. 10</span></label>
                    <input type="text" name="${configType}.interval" value="${config.interval}" validation="required digits" restrict="numeric">
                </div><div class="form-row">
                    <label><g:message code="interval.type"/><span class="suggestion">e.g. day</span></label>
                    <ui:namedSelect key="${NamedConstants.INTERVAL_TYPE}" name="${configType}.interval_type" value="${config.interval_type}"/>
                </div>
            </div>
            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>