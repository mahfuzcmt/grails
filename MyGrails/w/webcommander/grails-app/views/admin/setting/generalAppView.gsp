<%@ page import="com.webcommander.util.AppUtil; com.webcommander.util.StringUtil; com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants" %>
<form id="generalSettingsForm" class="create-edit-form" action="${app.relativeBaseUrl()}setting/saveGeneralSettings" onsubmit="return false" method="post" enctype="multipart/form-data">
    <input type="hidden" name="type" value="general"/>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="general"/></h3>
            <div class="info-content"><g:message code="section.text.setting.general"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row settings-group">
                <div class="form-row chosen-wrapper">
                    <label><g:message code="404.page"/><span class="suggestion"><g:message code="suggestion.setting.404"/></span></label>
                    <g:select class="medium" name="general.page404" from="${pageNames}" keys="${pageUrls}" value="${generalSettings.page404}"/>
                </div><div class="form-row chosen-wrapper">
                    <label><g:message code="403.page"/><span class="suggestion"><g:message code="suggestion.setting.403"/></span></label>
                    <g:select name="general.page403" from="${pageNames}" keys="${pageUrls}" value="${generalSettings.page403}"/>
                </div>
            </div>
            <div class="form-row country-selector-row chosen-wrapper">
                <label><g:message code="default.country"/><span class="suggestion"><g:message code="suggestion.setting.country"/></span></label>
                <ui:countryList id="countryId"  name="general.default_country" class="medium" value="${generalSettings.default_country.toLong()}"/>
            </div><g:include view="/admin/customer/stateFormFieldView.gsp" model="[states : states]" params="${[inputClass: 'medium', stateLabel: 'default.state', stateName: 'general.default_state']}"/>
            <div class="form-row chosen-wrapper">
                <label><g:message code="max.precision.for.price"/></label>
                <g:select name="general.max_precision" from="${1..9}" keys="${1..9}" value="${generalSettings.max_precision}"/>
            </div>
            <div class="double-input-row settings-group">
                <div class="form-row chosen-wrapper">
                    <label><g:message code="unit.length"/><span class="suggestion"><g:message code="suggestion.setting.unit.length"/></span></label>
                    <g:select from="${NamedConstants.LENGTHTYPE.values().collect {g.message(code: it)}}" class="medium" name="general.unit_length" keys="${NamedConstants.LENGTHTYPE.values()}"
                              value="${generalSettings.unit_length}"/>
                </div><div class="form-row chosen-wrapper">
                    <label><g:message code="unit.mass"/><span class="suggestion"><g:message code="suggestion.setting.unit.mass"/></span></label>
                    <g:select from="${NamedConstants.MASSTYPE.values().collect {g.message(code: it)}}" class="medium" name="general.unit_weight" keys="${NamedConstants.MASSTYPE.values()}"
                              value="${generalSettings.unit_weight}"/>
                </div>
            </div>
            <div class="form-row">
                <label><g:message code="favicon"/><span class="suggestion"><g:message code="suggestion.setting.favicon"/></span></label>
                <div class="form-image-block">
                    <g:set var="imgSrc" value="${ appResource.getFaviconURL(isEnable:  generalSettings.favicon_enabled == 'true')}"/>
                    <input type="file" name="image" file-type="image" size-limit="10240" previewer="favicon-image-preview" remove-option-name="remove-image"
                        ${generalSettings.favicon_enabled == 'true' ? 'remove-support="true"' : 'reset-support="true"'} ${generalSettings.favicon_enabled == 'true' ? "" : "style='display: none'"}>
                    <div class="preview-image">
                        <img id="favicon-image-preview" src="${imgSrc}">
                    </div>
                </div>
            </div>
            <div class="form-row">
                <input type="checkbox" class="single display-captcha-setting" name="general.captcha_setting" value="enable" ${generalSettings.captcha_setting == "enable" ? "checked" : ""}><span><g:message code="captcha"/></span><span class="suggestion"><g:message code="suggestion.setting.captcha"/></span>
            </div>
            <div class="form-row display-captcha-type ${generalSettings.captcha_setting == "enable" ? "" : "hidden"}">
                <div class="form-row">
                    <input type="radio" name="general.captcha_type" value="simple_captcha" ${generalSettings.captcha_type == "simple_captcha" || generalSettings.captcha_setting != "enable" ? "checked='true'":""} >
                    <span><g:message code="simple.captcha"/></span>
                    <input toggle-target="display-recaptcha-keys" type="radio" name="general.captcha_type" value="re_captcha"
                        ${generalSettings.captcha_type == "re_captcha" ? "checked='true'":""} >
                    <span><g:message code="re.captcha"/></span>
                </div>
                <div class="display-recaptcha-keys hidden double-input-row">
                    <div class="form-row mandatory" ${(generalSettings.captcha_type=="re_captcha" && generalSettings.captcha_setting=="enable")  ?'':'hidden'}>
                        <label><g:message code="public.key"/> </label>
                        <input type="text" class="medium" name="general.captcha_public_key" value="${generalSettings.captcha_public_key}" validation="skip@if{self::hidden} required">
                    </div><div class="form-row mandatory" ${(generalSettings.captcha_type=="re_captcha" && generalSettings.captcha_setting=="enable")  ?'':'hidden'}>
                        <label><g:message code="private.key"/> </label>
                        <input type="text" class="medium" name="general.captcha_private_key" value="${generalSettings.captcha_private_key}" validation="skip@if{self::hidden} required">
                    </div>
                </div>
            </div>
            <div class="form-row">
                <g:set var="getting_started_wizard" value="${DomainConstants.SITE_CONFIG_TYPES.GET_STARTED_WIZARD}"/>
                <input type="hidden" name="type" value="${getting_started_wizard}" />
                <g:set var="passed" value="${AppUtil.getConfig(getting_started_wizard, "passed")}"/>
                <input type="checkbox" name="${getting_started_wizard}.passed" class="single" value="false" uncheck-value="true" ${passed == "false" ? "checked" : "" } ><span><g:message code="enable.getting.started"></g:message></span>
            </div>
            <div class="form-row">
                <input type="checkbox" class="single" name="general.auto_suggest_address" value="true" uncheck-value="false" ${generalSettings.auto_suggest_address == "true" ? "checked" : ""}> &nbsp;
                <span><g:message code="auto.suggest.address"/></span>
            </div>
            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>