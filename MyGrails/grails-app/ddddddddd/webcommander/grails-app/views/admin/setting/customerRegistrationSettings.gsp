<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants; com.webcommander.config.SiteConfig" %>
<div class="toolbar-share">
    <div class="toolbar toolbar-right">
        <div class="tool-group remove-after-reload">
            <span class="toolbar-item reset reset-default" title="<g:message code="restore.default.setting"/>" ><i></i></span>
        </div>
    </div>
</div>
<form action="${app.relativeBaseUrl()}setting/saveConfigurations" method="post" class="fields-setting create-edit-form">
    <g:set var="configType" value="${DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_REGISTRATION_SETTINGS}"/>
    <input type="hidden" name="type" value="${configType}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="general"/></h3>
            <div class="info-content"><g:message code="section.text.registration.general.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row chosen-wrapper">
                <label><g:message code="registration.type"/><span class="suggestion"><g:message code="suggestion.setting.registration.type"/></span></label>
                <ui:namedSelect class="medium" toggle-target="message-row" name="${configType}.registration_status_type" key="${NamedConstants.CUSTOMER_REG_TYPE}"
                                value="${configs.registration_status_type}"/>
            </div>
            <div class="form-row message-row-${DomainConstants.CUSTOMER_REG_TYPE.CLOSED}">
                <label><g:message code="closed.registration.message"/><span class="suggestion"><g:message code="suggestion.setting.registration.closed"/></span></label>
                <input type="text" class="medium" name="${configType}.close_registration_message" value="${configs.close_registration_message}"  validation="maxlength[500]" maxlength="500">
            </div>
            <div class="form-row message-row-${DomainConstants.CUSTOMER_REG_TYPE.AWAITING_APPROVAL}">
                <label><g:message code="awaiting.approval.registration.message"/><span class="suggestion"><g:message code="suggestion.setting.registration.approval"/></span></label>
                <input type="text" value="${configs.restricted_registration_message}" class="medium" name="${configType}.restricted_registration_message" maxlength="500" validation="maxlength[500]">
            </div>
            <div class="form-row">
                <label><g:message code="use.captcha"/></label>
                <input type="radio" name="${configType}.registration_captcha" value="yes" ${configs.registration_captcha == "yes" ? "checked='true'" : ""} ><span class="value"><g:message code="yes"/></span>
                <input type="radio" name="${configType}.registration_captcha" value="no" ${configs.registration_captcha == "no"?"checked='true'":""}><span class="value"><g:message code="no"/></span>
            </div>
            <div class="form-row thicker-row">
                <label><g:message code="registration.terms.text"/><span class="suggestion"><g:message code="suggestion.setting.registration.term.text"/></span></label>
                <textarea class="medium" name="${configType}.registration_terms_text">${configs.registration_terms_text}</textarea>
            </div>
            <div class="form-row">
                <label><g:message code="enable.facebook.login"/> </label>
                <input type="checkbox" name="${configType}.enable_fb_login" class="single" value="true" uncheck-value="false" toggle-target="fb-login-config" ${configs.enable_fb_login == "true" ? "checked" : ""}/>
            </div>
            <div class="form-row fb-login-config">
                <label><g:message code="facebook.app.id"/> </label>
                <input type="text" name="${configType}.fb_app_id" value="${configs.fb_app_id}" validation="skip@if{self::hidden} required"/>
            </div>
            <div class="form-row">
                <label><g:message code="enable.google.login"/> </label>
                <input type="checkbox" name="${configType}.enable_google_login" class="single" value="true" uncheck-value="false" toggle-target="google-login-config" ${configs.enable_google_login == "true" ? "checked" : ""}/>
            </div>
            <div class="form-row google-login-config">
                <label><g:message code="google.client.id"/> </label>
                <input type="text" name="${configType}.google_client_id" value="${configs.google_client_id}" validation="skip@if{self::hidden} required"/>
            </div>
        </div>
    </div>
    <div class="section-separator"></div>
    <div class="form-section">
        <g:set var="customerArg" value="${(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customer":"Member"}"/>
        <div class="form-section-info">
            <h3><g:message code="customer.reg.form.fields" args="${[customerArg]}"/></h3>
            <div class="info-content"><g:message code="section.text.customer.reg.form.field.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="sortable-container">
                <g:set var="configType" value="${DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_REGISTRATION_FIELD}"/>
                <input type="hidden" name="type" value="${configType}">
                <g:each in="${fields}" var="field">
                    <g:set var="active" value="${fieldsConfigs[field + '_active']}"/>
                    <g:set var="required" value="${fieldsConfigs[field + '_required']}"/>
                    <g:set var="order" value="${fieldsConfigs[field + '_order']}"/>
                    <g:set var="label" value="${fieldsConfigs[field + '_label']}"/>
                    <div class="configurable-row ${active ? 'active' : 'inactive'}">
                        <label><g:message code="${field.toString().replaceAll('_', '.')}"/></label>
                        <span class="edit-block">
                            <input type="text" class="small" name="${configType}.${field}_label" value="${label}">
                            <g:if test="${registrationFieldConfigs[field + '_active'] != null}">
                                <input class="active" type="hidden" name="${configType}.${field}_active" value="${active}">
                                <input class="required single" type="checkbox" name="${configType}.${field}_required" value="true" uncheck-value="false" ${required ? 'checked="checked"' : ''} ${active ? '' : 'disabled="disabled"'}>
                                <g:message code="required"/> &nbsp &nbsp
                                <span class="tool-icon ${active ? 'remove' : 'add'}"></span>
                            </g:if>
                            <g:else>
                                <input class="required single" type="checkbox" name="${configType}.${field}_required" value="true"  ${required ? 'checked="checked"' : ''} disabled="disabled">
                                <g:message code="required"/> &nbsp &nbsp
                            </g:else>
                            <input class="order" type="hidden" name="${configType}.${field}_order" value="${order}">
                        </span>
                    </div>
                </g:each>
                <g:set var="active" value="${fieldsConfigs['registration_terms_active']}"/>
            </div>
            <div class="configurable-row ${active ? 'active' : 'inactive'}">
                <label><g:message code="${'registration.terms'}"/></label>
                <span class="edit-block">
                    <input class="active" type="hidden" name="${configType}.registration_terms_active" value="${active}">
                    <input class="required single" type="checkbox" value="true" checked="checked" disabled="disabled">
                    <g:message code="required"/> &nbsp &nbsp
                    <span class="tool-icon ${active ? 'remove' : 'add'}"></span>
                </span>
            </div>
            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>