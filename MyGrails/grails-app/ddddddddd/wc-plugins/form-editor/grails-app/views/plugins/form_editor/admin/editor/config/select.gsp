<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants" %>
<div class="body">
    <div type="basic" class="config-section">
        <div class="form-row">
            <label><g:message code="html.name"/></label>
            <input type="text" value="" name="field.name" validation="required maxlength[100]" maxlength="100">
        </div>

        <div class="form-row">
            <label><g:message code="label"/></label>
            <input type="text" value="" name="field.label" validation="maxlength[100]" maxlength="100">
        </div>

        <div class="form-row">
            <label><g:message code="html.class.s"/></label>
            <input type="text" value="" name="field.clazz" validation="maxlength[100]" maxlength="100">
        </div>
        <div class="form-row">
            <label><g:message code="hover.text"/></label>
            <input type="text" value="" name="field.title" validation="maxlength[100]" maxlength="100">
        </div>
    </div>
    <div type="validation" class="properties config-section">
        <input type="hidden" name="field.validation" class="validation-field">
        <div class="form-row">
            <input type="checkbox" name="r-validation" class="validation-required single" value="required">
            <label><g:message code="required" /></label>
        </div>
    </div>
    <div class="config-section" type="field-options" >
        <div class="type-change-from">
            <div class="form-row">
                <label><g:message code="selection.type"/></label>
                <div class="radio-group horizontal">
                    <div class="radio">
                        <input type="radio" name="selectionType" class="type-single" value="single" toggle-target="type-single-dependent">
                        <label><g:message code="single.select"/></label>
                    </div>
                    <div class="radio">
                        <input type="radio" name="selectionType" class="type-multi" value="multi" toggle-target="type-multi-dependent">
                        <label><g:message code="multi.select"/></label>
                    </div>
                </div>
            </div>
            <div class="form-row">
                <label><g:message code="style"/></label>
                <div class="radio-group horizontal">
                    <div class="radio">
                        <input type="radio" name="style" value="radio" class="style-radio">
                        <label class="type-single-dependent"><g:message code="radio.button"/></label>
                        <label class="type-multi-dependent"><g:message code="check.box"/></label>
                    </div>
                    <div class="radio">
                        <input type="radio" name="style" value="dropDown" class="style-dropDown">
                        <label><g:message code="drop.down"/></label>
                    </div>
                </div>
            </div>
            <div class="option-type">
                <div class="form-row">
                    <label><g:message code="option.type"/></label>
                </div>
                <div class="form">
                    <input type="radio" name="field.extra.config.option_type" value="none" field-update="true">
                    <label><g:message code="none"/> </label>
                </div>
                <div class="form">
                    <input type="radio" name="field.extra.config.option_type" value="country" field-update="true">
                    <label><g:message code="country"/></label>
                </div>
                <div class="form">
                    <input type="radio" name="field.extra.config.option_type" value="state" toggle-target="country-selector" field-update="true">
                    <label><g:message code="state"/> </label>
                </div>
                <div class="form-row country-selector country-selector">
                    <ui:countryList name="field.extra.config.state_country" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "default_country").toLong()}"/>
                </div>
            </div>
        </div>
        <div class="add-option-form" validation-attribute_name="validation-rule">
            <div class="form-row">
                <label><g:message code="option"/></label>
                <input type="hidden" name="id">
            </div>
            <div class="form-row">
                <input type="text" name="label" placeholder="<g:message code="name"/>" validation-rule="required" maxlength="200">
            </div>
            <div class="form-row">
                <input type="text" name="value" placeholder="<g:message code="value"/>" validation-rule="required" maxlength="200">
            </div>
            <div class="triple-input-row">
                <div class="form-row">
                    <button class="submit-btn" type="submit"><g:message code="add"/></button>
                </div><div class="form-row">
                        <button class="remove-btn hidden"><g:message code="remove"/></button>
                </div><div class="form-row">
                    <input type="checkbox" class="single" name="extraValue" value="selected">
                    <label><g:message code="selected"/> </label>
                </div>
            </div>
        </div>
    </div>
</div>