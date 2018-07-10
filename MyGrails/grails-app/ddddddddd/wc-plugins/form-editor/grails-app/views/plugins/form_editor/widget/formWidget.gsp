<%@ page import="com.webcommander.manager.LicenseManager; com.webcommander.plugin.form_editor.constants.DomainConstants; com.webcommander.plugin.form_editor.util.TemplateHelper as TH" %>
<g:applyLayout name="_widget">
    <g:if test="${request.page}">
        <%
            if (!request.form_submit_script_loaded) {
                request.js_cache.push("plugins/form-editor/js/form-page/form.js")
                request.form_submit_script_loaded = true;
            }
        %>
    </g:if>
    <form class="custom-form ${TH.text(form.clazz)}" action="${app.relativeBaseUrl()}formPage/submitForm" method="post" enctype="multipart/form-data">
        <input type="hidden" name="id" value="${form.id}">
        <input type="hidden" name="${form.senderEmailFieldUUID}.senderEmail" class="sender-email-hidden-filed">
        <input type="hidden" name="senderEmailFieldUUID" value="${form.senderEmailFieldUUID}">

        <span class="before-form-submit" style="display: none;">${TH.text(form.beforeHandler)}</span>
        <span class="after-form-submit" style="display: none;">${TH.text(form.afterHandler)}</span>
        <g:set var="fieldConditions" value="${[:]}"/>
        <g:each in="${form.fields}" var="field">
            <g:if test="${field.type == DomainConstants.FORM_FIELD.FIELD_GROUP}">
                <div class="${field.fields.size() == 2 ? "double-input-row" : "triple-input-row"}">
                    <g:each in="${field.fields}" var="formField">
                        <g:include view="/plugins/form_editor/widget/fieldRenderer.gsp" model="${[field: formField, form: field, fieldConditions: fieldConditions]}" />
                    </g:each>
                </div>
            </g:if>
            <g:else>
                <div class="single-input-row">
                    <g:include view="/plugins/form_editor/widget/fieldRenderer.gsp" model="${[field: field, form: field, fieldConditions: fieldConditions]}" />
                </div>
            </g:else>
        </g:each>
        <g:if test="${form.isTermConditionEnabled}">
            <g:if test="${form.isTermConditionTextEnabled}">
                <div class="form-row mandatory">
                    <label><site:message code="${label ?: "s:registration.terms"}"/>:</label>
                    <textarea class="large" readonly >${form.termConditionText}</textarea>
                </div>
            </g:if>
            <div class="form-row" validation="least_selection" message_template="${g.message(code: "required.to.proceed")}">
                <input class="checkbox" type="checkbox" name="registrationTerms">${form.termCondition ? form.termCondition : site.message(code: "s:i.agree.term.condition")}
            </div>
        </g:if>
        <g:if test="${(boolean) form.useCaptcha}">
            <ui:captcha/>
        </g:if>
        <license:allowed id="allow_form_builder_feature">
        </license:allowed>
        <license:otherwise>
            <div class="form-row service-disabled">
                <g:message code="service.temporarily.disabled"/>
            </div>
        </license:otherwise>
        <div class="form-row btn-row">
            <label>&nbsp</label>
            <button class="submit-button" type="submit"><site:message code="${form.submitButtonLabel}"/></button>
            <g:if test="${(boolean) form.resetEnabled}">
                &nbsp;<button class="reset-button" type="reset"><g:message code="reset"/></button>
            </g:if>
        </div>
        <span class="conditions-cache" style="display: none">${fieldConditions as grails.converters.JSON}</span>
    </form>
</g:applyLayout>
