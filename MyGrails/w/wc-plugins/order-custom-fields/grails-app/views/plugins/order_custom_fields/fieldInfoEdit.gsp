<%@ page import="com.webcommander.util.StringUtil; com.webcommander.plugin.order_custom_fields.DomainConstants" %>
<form class="create-edit-form order-custom-field-create-form" action="${app.relativeBaseUrl()}customFieldsAdmin/saveField"
      xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="custom.field.information"/> </h3>
            <div class="info-content"><g:message code="form.section.text.custom.field.info"/> </div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row chosen-wrapper">
                    <label><g:message code="type"/><span class="suggestion">e.g. Long Text</span></label>
                    <g:select name="type" from="${DomainConstants.ORDER_CHECKOUT_FIELD_TYPE.values().collect {g.message(code: it)}}" keys="${DomainConstants.ORDER_CHECKOUT_FIELD_TYPE.values()}"
                              value="${field.type}" class="medium" toggle-target="field-type"/>
                </div><div class="form-row mandatory">
                    <label><g:message code="name"/><span class="suggestion">e.g. Peter</span></label>
                    <input type="text" value="${field.name.encodeAsHTML()}" name="name" validation="required maxlength[100]" maxlength="100" class="medium">
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="field.label"/><span class="suggestion">e.g. Order Comment</span></label>
                    <input type="text" value="${field.label.encodeAsHTML()}" maxlength="100" validation="required maxlength[100]" name="label" class="medium">
                </div><div class="form-row">
                    <label><g:message code="html.class.s"/><span class="suggestion">e.g. order-comment</span></label>
                    <input type="text" value="${field.clazz.encodeAsHTML()}" name="clazz" validation="maxlength[100]" maxlength="100" class="medium">
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <label><g:message code="hover.text"/><span class="suggestion">e.g. Order Comment</span></label>
                    <input type="text" value="${field.title.encodeAsHTML()}" name="title" validation="maxlength[100]" maxlength="100" class="medium">
                </div><div class="form-row">
                    <label><g:message code="default.value"/><span class="suggestion"></span></label>
                    <input type="text" value="${field.value.encodeAsHTML()}" name="value" validation="maxlength[100]" maxlength="100" class="medium">
                </div>
            </div>

            <div class="form-row field-type-long_text field-type-text text" do-reverse-toggle>
                <label><g:message code="options"/></label>
                <div class="medium multitxtchosen" name="options">
                    <g:each in="${field.options}" var="option">
                        <input type="hidden" name="options" value="${option}">
                    </g:each>
                </div>
            </div>
            <div class="form-row field-type-long_text field-type-text text">
                <label><g:message code="placeholder"/><span class="suggestion">Enter Order Comment</span></label>
                <input type="text" value="${field.placeholder.encodeAsHTML()}" name="placeholder" validation="maxlength[100]" maxlength="100" class="medium">
            </div>
        </div>
     </div>
    <div class="form-section-separator field-type-long_text field-type-text"></div>
    <div class="form-section">
        <div class="form-section-info field-type-long_text field-type-text">
            <h3><g:message code="validation"/></h3>
            <div class="info-content"><g:message code="section.text.validation.info"/></div>
        </div>
        <div class="form-section-container validation-fields-group">
            <div class="field-type-long_text field-type-text">
                <g:set var="validation" value="${field.validation}"/>
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
                    <input type="checkbox" name="r-validation" class="validation-required" value="required">
                    <label class="value"><g:message code="required" /></label>
                </div>
                <g:set var="ref1" value="${StringUtil.uuid}"/>
                <g:set var="ref2" value="${StringUtil.uuid}"/>
                <div class="double-input-row">
                    <div class="form-row with-left-check-box">
                        <label><g:message code="max.length"/><span class="suggestion"> e.g. 160</span> </label>
                        <input type="checkbox" id="${ref1}" name="r-validation" class="validation-maxlength single" toggle-target="maxlength-limit" independent toggle-anim="none"/>
                        <input restrict="numeric" type="text" class="maxlength-limit" validation="skip@not{global:#${ref1}:checked} skip@if{self::hidden} required digits"/>
                        </div><div class="form-row with-left-check-box">
                        <label><g:message code="custom.validation"/></label>
                        <input type="checkbox" id="${ref2}" name="r-validation" class="validation-custom single" toggle-target="custom-validation" toggle-anim="none" independent />
                        <input type="text" class="custom-validation" validation="skip@not{global:#${ref2}:checked} skip@if{self::hidden} required"/>
                    </div>
                </div>
                <input type="hidden" name="validation" value="${validation}">
                <input type="hidden" name="id" value="${field.id}">
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="${field.id ? 'update' : 'save'}"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
  </form>