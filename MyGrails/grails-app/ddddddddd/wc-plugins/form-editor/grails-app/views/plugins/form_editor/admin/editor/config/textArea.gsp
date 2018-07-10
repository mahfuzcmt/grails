<%@ page import="com.webcommander.util.StringUtil" %>
<div class="body">
    <div type="basic" class="properties config-section">
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
    <div type="validation" class="validation config-section">
        <input type="hidden" name="field.validation" class="validation-field">
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
    </div>
</div>