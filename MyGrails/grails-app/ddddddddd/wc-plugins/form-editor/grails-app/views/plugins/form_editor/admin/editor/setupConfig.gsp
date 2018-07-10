<%@ page import="com.webcommander.plugin.form_editor.constants.DomainConstants; com.webcommander.util.StringUtil" %>
<div class="setup-config leftbar-accordion active">
    <div class="label-bar general-setting-header">
        <label><g:message code="general.setting"/></label>
        <span class="tool-icon toggle-icon"></span>
    </div>
    <div class="accordion-item general-setting">
        <div class="form-row">
            <label><g:message code="name"/> </label>
            <input type="text" value="${form.name.encodeAsBMHTML()}" name="name" validation="required maxlength[255]" maxlength="255">
        </div>
        <div class="form-row">
            <input type="checkbox" class="single" uncheck-value="false" value="true" name="useCaptcha" ${form.useCaptcha ? "checked" : ""}>
            <label><g:message code="use.captcha"/></label>
        </div>
        <div class="form-row">
            <input type="checkbox" class="single" uncheck-value="false" value="true" name="resetEnabled" ${form.resetEnabled ? "checked" : ""}>
            <label><g:message code="enabled.reset"/></label>
        </div>
        <div class="form-row">
            <label><g:message code="html.class.s"/> </label>
            <input type="text" value="${form.clazz}" name="clazz" validation="maxlength[255]" maxlength="255">
        </div>
        <div class="form-row">
            <label><g:message code="submit.button.label"/> </label>
            <input type="text" value="${form.submitButtonLabel.encodeAsBMHTML()}" name="submitButtonLabel" validation="required maxlength[100]" maxlength="100">
        </div>
        <div class="form-row">
            <input type="checkbox" class="single" uncheck-value="false" value="true" toggle-target="term-condition-text" toggle-anim="slide" name="isTermConditionTextEnabled" ${form.isTermConditionTextEnabled ? "checked" : ""}>
            <label><g:message code="custom.term.condition.text"/></label>
        </div>
        <div class="form-row term-condition-text">
            <textarea name="termConditionText" validation="maxlength[500]" maxlength="500">${form.termConditionText.encodeAsBMHTML()}</textarea>
        </div>
        <div class="form-row">
            <input type="checkbox" class="single" uncheck-value="false" value="true" toggle-target="term-condition" toggle-anim="slide" name="isTermConditionEnabled" ${form.isTermConditionEnabled ? "checked" : ""}>
            <label><g:message code="term.condition.acceptance"/></label>
        </div>
        <div class="form-row term-condition">
            <input type="text" name="termCondition" validation="maxlength[500]" maxlength="500" value="${form.termCondition ? form.termCondition.encodeAsBMHTML() : site.message(code: "s:i.agree.term.condition")}"/>
        </div>
        <div class="form-row">
            <label><g:message code="success.message"/></label>
            <input type="text" name="successMessage" validation="maxlength[500]" maxlength="500" value="${form.successMessage.encodeAsBMHTML()}"/>
        </div>
        <div class="form-row">
            <label><g:message code="failure.message"/></label>
            <input type="text" name="failureMessage" validation="maxlength[500]" maxlength="500" value="${form.failureMessage.encodeAsBMHTML()}"/>
        </div>
    </div>
    <div class="label-bar data-handler-header">
        <label><g:message code="data.handler"/></label>
        <span class="tool-icon toggle-icon"></span>
    </div>
    <div class="accordion-item data-handler">
        <g:set var="did" value="_${StringUtil.uuid}"/>
        <g:set var="rid1" value="_${StringUtil.uuid}"/>
        <g:set var="rid2" value="_${StringUtil.uuid}"/>
        <div class="form-row" id="${did}">
            <ui:radioGroup name="actionType" value="${form.actionType}">
                <div class="radio-group vertical">
                    <div class="radio">
                        <ui:radio value="${DomainConstants.FORM_ACTION_TYPE.EMAIL}" toggle-target="target-email" id="${rid1}"/>
                        <label><g:message code="send.to.email"/></label>
                    </div>
                    <div class="radio">
                        <ui:radio value="${DomainConstants.FORM_ACTION_TYPE.EXTERNAL_URL}" toggle-target="target-external" id="${rid2}"/>
                        <label><g:message code="submit.to.url"/> </label>
                    </div>
                    <div class="radio">
                        <ui:radio value="${DomainConstants.FORM_ACTION_TYPE.INTERNAL}"/>
                        <label><g:message code="save.internally"/></label>
                    </div>
                </div>
            </ui:radioGroup>
        </div>
        <div class="form-row target-email mandatory">
            <label><g:message code="email.subject"/> </label>
            <input type="text" class="small" name="emailSubject" validation="required@if{global:#${rid1}:checked} maxlength[255]" maxlength="255" depends="#${did}" value="${form.emailSubject}">
        </div>
        <div class="form-row target-email mandatory">
            <label><g:message code="email.to.address"/> </label>
            <input type="text" validation="skip@if{global:#${rid1}:not(:checked)} required email maxlength[255]" maxlength="255" class="small" name="emailTo" depends="#${did}" value="${form.emailTo}">
        </div>
        <div class="form-row target-email">
            <label><g:message code="email.cc.address"/> </label>
            <input type="text" validation="email@if{global:#${rid1}:checked} maxlength[255]" maxlength="255" class="small" name="emailCc" depends="#${did}" value="${form.emailCc}">
        </div>
        <div class="form-row target-email">
            <label><g:message code="email.bcc.address"/></label>
            <input type="text" validation="email@if{global:#${rid1}:checked} maxlength[255]" maxlength="255" class="small" name="emailBcc" depends="#${did}" value="${form.emailBcc}">
        </div>
        <div class="form-row target-external mandatory">
            <label><g:message code="url"/> </label>
            <input type="text" validation="skip@if{global:#${rid2}:not(:checked)} required url maxlength[500]" maxlength="500" class="small" name="actionUrl" value="${form.actionUrl}"
                   depends="#${did}">
        </div>
        <div class="form-row">
            <label><g:message code="sender.email"/></label>
            <select name="senderEmailFieldUUID" value="${form.senderEmailFieldUUID}"></select>
        </div>
    </div>

    <div class="label-bar custom-js-header">
        <label><g:message code="custom.js"/></label>
        <span class="tool-icon toggle-icon"></span>
    </div>
    <div class="accordion-item custom-js">
        <div class="form-row">
            <span class="hedaer-info">function beforeSubmit(form) {</span>
            <textarea placeholder="Place your javascript code here" name="beforeHandler" validation="maxlength[1000]" maxlength="1000">${form.beforeHandler}</textarea>
            <span class="footer-info">}</span>
            <br>
            <span class="hedaer-info">function afterSubmit(form) {</span>
            <textarea placeholder="Place your javascript code here" name="afterHandler" validation="maxlength[1000]" maxlength="1000">${form.afterHandler}</textarea>
            <span class="footer-info">}</span>
        </div>
    </div>
    <div class="label-bar conditions-accordion-header">
        <label><g:message code="conditions"/></label>
        <span class="tool-icon toggle-icon"></span>
    </div>
    <div class="accordion-item conditions-accordion">
        <div class="conditions">
        </div>
        <div class="condition template" style="display: none">
            <span><g:message code="if.field.value"/> = </span>
            <span class="targetOption"></span>
            <span class="action"></span>
            <span class="dependentFieldName"></span>
        </div>
    </div>
</div>