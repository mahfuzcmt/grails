<%@ page import="com.webcommander.util.StringUtil" %>
<div class="body leftbar-accordion">
    <div class="label-bar properties-header">
        <label><g:message code="properties.for.single.line"/></label>
        <span class="tool-icon toggle-icon"></span>
    </div>

    <div type="basic" class="accordion-item properties config-section">
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
            <label><g:message code="placeholder"/></label>
            <input type="text" name="field.placeholder" validation="maxlength[255]" maxlength="255">
        </div>
        <div class="form-row">
            <label><g:message code="hover.text"/></label>
            <input type="text" value="" name="field.title" validation="maxlength[100]" maxlength="100">
        </div>
        <div class="form-row">
            <label><g:message code="default.value"/></label>
            <input type="text" class="text" name="field.value" validation="maxlength[255]" maxlength="255">
        </div>
    </div>
    <div class="label-bar validation-header">
        <label><g:message code="validation"/></label>
        <span class="tool-icon toggle-icon"></span>
    </div>
    <div type="validation" class="accordion-item validation config-section">
        <input type="hidden" name="field.validation" class="validation-field" field-update="true">
        <div class="form-row">
            <input type="radio" name="r-validation" class="validation-none" value="">
            <label><g:message code="none"/> </label>
        </div>
        <div class="form-row">
            <input type="radio" name="r-validation" class="validation-email" value="email" toggle="config-email-conf">
            <label><g:message code="email"/> </label>
            <div class="sub-form-row config-email-conf">
                <input type="checkbox" name="field.extra.config.show_confirm_email" class="single " value="true">
                <label><g:message code="display.email.confirm.field"/> </label>
            </div>
        </div>
        <div class="form-row">
            <input type="radio" name="r-validation" class="validation-alphanumeric" value="alphanumeric">
            <label><g:message code="alphaNumeric"/> </label>
        </div>
        <div class="form-row">
            <input type="radio" name="r-validation"  class="validation-alphabetic" value="alphabetic">
            <label><g:message code="alphabetic"/> </label>
        </div>
        <div class="form-row">
            <input type="radio" name="r-validation" class="validation-number" value="number" toggle="max-min-wrap">
            <label><g:message code="number"/> </label>
        </div>
        <div class="double-input-row max-min-wrap">
            <g:set var="id" value="${StringUtil.uuid}"/>
            <div class="form-row">
                <input type="text" id="${id}" placeholder="<g:message code="min"/>" class="min-limit" validation="digits" maxlength="9" restrict="numeric" rule="min">
            </div><div class="form-row">
                <input type="text" placeholder="<g:message code="max"/>" class="max-limit" validation="skip@if{self::hidden} digits gt[0] compare[${id},number,gt]" maxlength="9" restrict="numeric" rule="max" depends="#${id}">
            </div>
        </div>
        <div class="form-row">
            <input type="radio" name="r-validation" class="validation-phone" value="phone">
            <label><g:message code="phone" /></label>
        </div>
        <div class="form-row">
            <input type="radio" name="r-validation" class="validation-url" value="url">
            <label><g:message code="url" /></label>
        </div>
        <div class="form-row">
            <input type="checkbox" name="r-validation" class="validation-required single" value="required">
            <label><g:message code="required" /></label>
        </div>
        <div class="form-row">
            <label><g:message code="length"/></label>
        </div>
        <div class="double-input-row max-min-length-wrap">
            <input type="hidden" name="r-validation" class="validation-length single" value="">
            <g:set var="id" value="${StringUtil.uuid}"/>
            <div class="form-row">
                <input type="text" id="${id}" placeholder="<g:message code="min.length"/>" class="minlength-limit" validation="digits" rule="minlength" restrict="numeric">
            </div><div class="form-row">
                <input type="text" placeholder="<g:message code="max.length"/>" class="maxlength-limit" validation="digits gt[0] compare[${id},number,gt]" rule="maxlength" restrict="numeric" depends="#${id}">
            </div>
        </div>
        <div class="form-row">
            <input type="checkbox" name="r-validation" class="validation-custom single" toggle="custom-validation" independent>
            <label><g:message code="custom"/></label>
            <input type="text" class="custom-validation">
        </div>
    </div>
</div>