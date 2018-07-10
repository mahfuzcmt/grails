<%@ page import="com.webcommander.constants.DomainConstants" %>
<form action="${app.relativeBaseUrl()}setting/saveConfigurations" method="post" class="create-edit-form administration-edit-form">
    <g:set var="type" value="${DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION}"/>
    <input type="hidden" name="type" value="${type}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="administration"/></h3>
            <div class="info-content"><g:message code="section.text.setting.administration"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <input type="checkbox" class="single ecommerce-switch" name="${type}.ecommerce" value="true" uncheck-value="false" ${config.ecommerce == "true" ? "checked='checked'" : ""}>
                <span><g:message code="e.commerce" /></span>
            </div>
            <div class="form-row">
                <button class="refresh-license-button" type="button"><g:message code="refresh.license"/></button>
            </div>
            <div class="form-row">
                <button class="clear-disposable-items" type="button"><g:message code="clear.disposable.items"/></button>
            </div>
            <div class="form-row">
                <label><g:message code="host.name"/><span class="suggestion">e.g. http://www.webcommander.com/ </span> </label>
                <input type="text" name="${type}.baseurl" value="${config.baseurl}" validation="url">
            </div>
            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>

<div class="form-section">
    <div class="form-section-info">
        <h3><g:message code="system.information"/></h3>
        <div class="info-content"><g:message code="section.text.setting.system.info"/></div>
    </div>
    <div class="form-section-container">
        <div class="form-row">
            <label><g:message code="version"/><span class="suggestion">${systemInformation.version}</span></label>
        </div>
        <div class="form-row">
            <label><g:message code="build.number"/><span class="suggestion">${systemInformation.build}</span></label>
        </div>
        <div class="form-row">
            <label><g:message code="identity"/><span class="suggestion">${systemInformation?.licenseCode}</span></label>
        </div>
        <div class="form-row">
            <label><g:message code="eight.digit"/><span class="suggestion">${systemInformation?.instance_eight_digit}</span></label>
        </div>
    </div>
</div>