<%@ page import="com.webcommander.plugin.google_trusted_store.Constants; com.webcommander.constants.DomainConstants" %>
<form action="${app.relativeBaseUrl()}setting/saveConfigurations" method="post" class="fields-setting create-edit-form">
    <g:set var="configType" value="${DomainConstants.SITE_CONFIG_TYPES.GOOGLE_TRUSTED_STORE}"/>
    <input type="hidden" name="type" value="${configType}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="google.trusted.store"/></h3>
            <div class="info-content"><g:message code="section.text.setting.google.trusted.store"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row">
                    <label><g:message code="store.id"/><span class="suggestion">e.g. 685783</span></label>
                    <input type="text" name="${configType}.store_id" value="${config.store_id}" validation="required">
                </div><div class="form-row chosen-wrapper">
                    <label><g:message code="badge.position"/></label>
                    <ui:namedSelect key="${Constants.BADGE_POSITION_MESSAGE_KEY}" name="${configType}.badge_position" value="${config.badge_position}" toggle-target="badge-container"/>
                </div>
            </div>
            <div class="form-row badge-container-USER_DEFINED">
                <label>
                    <g:message code="badge.container"/>
                    <span class="suggestion">An HTML element ID which you would like the Trusted Stores Badge to be injected into.</span>
                </label>
                <input type="text" name="${configType}.badge_container" value="${config.badge_container}" validation="required@if{self::visible}">
            </div>
            <div class="form-row chosen-wrapper">
                <label><g:message code="locale"/></label>
                <ui:namedSelect key="${Constants.LOCALE}" name="${configType}.locale" value="${config.locale}"/>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <label><g:message code="ship.date.after"/></label>
                    <input type="text" name="${configType}.ship_date_after" value="${config.ship_date_after}" validation="required max[7] min[1]" restrict="numeric">
                    <span><g:message code="day"/></span>
                </div><div class="form-row">
                    <label><g:message code="deliver.date.after"/></label>
                    <input type="text" name="${configType}.deliver_date_after" value="${config.deliver_date_after}" validation="required max[7] min[1]" restrict="numeric">
                    <span><g:message code="day"/></span>
                </div>
            </div>
            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>