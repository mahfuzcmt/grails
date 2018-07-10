<%@ page import="com.webcommander.util.StringUtil; com.webcommander.plugin.simplified_event_management.constants.SimplifiedEventConstants" %>
<form class="edit-popup-form create-edit-form product-custom-filed-create-form" action="${app.relativeBaseUrl()}simplifiedEventAdmin/saveCustomField">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="custom.field.info"/></h3>
            <div class="info-content"><g:message code="section.text.event.custom.field"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row chosen-wrapper">
                    <label><g:message code="type"/><span class="suggestion"> e.g. Long Text</span> </label>
                    <g:select name="type" from="${SimplifiedEventConstants.EVENT_CHECKOUT_FIELD_TYPE.values().collect {g.message(code: it)}}" keys="${SimplifiedEventConstants.EVENT_CHECKOUT_FIELD_TYPE.values()}"
                              value="${field.type}" class="medium" toggle-target="field-type"/>
                </div><div class="form-row mandatory">
                    <label><g:message code="name"/><span class="suggestion"> e.g. Delivery Time</span> </label>
                    <input type="text" value="${field.name.encodeAsHTML()}" name="name" validation="required maxlength[100]" maxlength="100" class="medium">
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="field.label"/><span class="suggestion"> e.g. Monday Delivery</span> </label>
                    <input type="text" value="${field.label.encodeAsHTML()}" maxlength="100" validation="required maxlength[100]" name="label" class="medium">
                </div><div class="form-row">
                    <label><g:message code="html.class.s"/></label>
                    <input type="text" value="${field.clazz.encodeAsHTML()}" name="clazz" validation="maxlength[100]" maxlength="100" class="medium">
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <label><g:message code="hover.text"/><span class="suggestion"> e.g. Monday Delivery</span> </label>
                    <input type="text" value="${field.title.encodeAsHTML()}" name="title" validation="maxlength[100]" maxlength="100" class="medium">
                </div><div class="form-row">
                    <label><g:message code="default.value"/><span class="suggestion"> e.g. After 6:30 PM</span> </label>
                    <input type="text" value="${field.value.encodeAsHTML()}" name="value" validation="maxlength[100]" maxlength="100" class="medium">
                </div>
            </div>
            <div class="form-row field-type-long_text field-type-text text" do-reverse-toggle>
                <label><g:message code="options"/><span class="suggestion"> Enter Some Option</span></label>
                <div class="medium multitxtchosen" name="options">
                    <g:each in="${field.options}" var="option">
                        <input type="hidden" name="options" value="${option}">
                    </g:each>
                </div>
            </div>
            <div class="form-row field-type-long_text field-type-text text">
                <label><g:message code="placeholder"/><span class="suggestion"> e.g. After 6:30 PM</span> </label>
                <input type="text" value="${field.placeholder.encodeAsHTML()}" name="placeholder" validation="maxlength[100]" maxlength="100" class="medium">
            </div>
            <input type="hidden" name="validation" value="${field.validation}">
            <input type="hidden" name="id" value="${field.id}">
            <input type="hidden" name="event.id" value="${field.event.id}">
        </div>
    </div>
    <div class="form-section-separator"></div>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="validation"/></h3>
            <div><g:message code="section.text.event.custom.field.validation"/> </div>
        </div>
        <div class="form-section-container validation-fields-group">
            <div class="form-row">
                <span class="input-label-combined">
                    <input type="radio" name="r-validation" class="validation-none" value="" checked>
                    <label class="value"><g:message code="none"/> </label>
                </span>
                <span class="input-label-combined">
                    <input type="radio" name="r-validation" class="validation-email" value="email">
                    <label class="value"><g:message code="email"/> </label>
                </span>
                <span class="input-label-combined">
                    <input type="radio" name="r-validation" class="validation-alphanumeric" value="alphanumeric">
                    <label class="value"><g:message code="alpha.numeric"/> </label>
                </span>
                <span class="input-label-combined">
                    <input type="radio" name="r-validation"  class="validation-alphabetic" value="alphabetic">
                    <label class="value"><g:message code="alphabetic"/> </label>
                </span>
                <span class="input-label-combined">
                    <input type="radio" name="r-validation" class="validation-number" value="number">
                    <label class="value"><g:message code="numeric"/> </label>
                </span>
                <span class="input-label-combined">
                    <input type="radio" name="r-validation" class="validation-phone" value="phone">
                    <label class="value"><g:message code="phone" /></label>
                </span>
            </div>
            <div class="form-row">
                <label><g:message code="required" /></label>
                <input type="checkbox" name="r-validation" class="validation-required single" value="required">
            </div>
            <g:set var="ref1" value="${StringUtil.uuid}"/>
            <g:set var="ref2" value="${StringUtil.uuid}"/>
            <div class="double-input-row">
                <div class="form-row">
                    <label><g:message code="max.length"/><span class="suggestion"> e.g. 160</span> </label>
                    <div class="input-group">
                        <input type="checkbox" id="${ref1}" name="r-validation" class="validation-maxlength single">
                        <input type="text" class="maxlength-limit" validation="required@if{global:#${ref1}:checked}  digits" restrict="numeric">
                    </div>
                </div><div class="form-row">
                    <label><g:message code="custom"/></label>
                    <div class="input-group">
                        <input type="checkbox" id="${ref2}" name="r-validation" class="validation-custom single">
                        <input type="text" class="custom-validation" validation="required@if{global:#${ref2}:checked}">
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="${field.id ? 'update' : 'save'}"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>