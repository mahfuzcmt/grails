<%@ page import="com.webcommander.constants.NamedConstants; com.webcommander.constants.DomainConstants" %>
<form action="${app.relativeBaseUrl()}currencyAdmin/save" method="post" class="create-edit-form">
    <input type="hidden" name="id" value="${currency.id}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="currency.info"/></h3>
            <div class="info-content"><g:message code="section.text.currency.info"/> </div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="name"/><span class="suggestion"><g:message code="suggestion.currency.name"/></span></label>
                    <input type="text" class="medium unique" name="name" value="${currency.name.encodeAsBMHTML()}" maxlength="100" validation="required rangelength[2,100]">
                </div><div class="form-row mandatory">
                    <label><g:message code="code"/><span class="suggestion"><g:message code="suggestion.currency.code"/></span></label>
                    <input type="text" class="medium unique" name="code" value="${currency.code.encodeAsBMHTML()}" maxlength="3" validation="required maxlength[3]">
                </div>
            </div>
            <div class="form-row chosen-wrapper mandatory">
                <label><g:message code="country"/><span class="suggestion"> e.g. Australia</span></label>
                <ui:countryList id="country" name="countryId" validation="required"  class="large" value="${currency.countryId}" data-placeholder="${g.message(code: 'select.country')}"/>
            </div>
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="symbol"/><span class="suggestion"><g:message code="suggestion.currency.symbol"/></span></label>
                    <input type="text" class="medium" name="symbol" value="${currency.symbol.encodeAsBMHTML()}" maxlength="3" validation="required maxlength[3]">
                </div><div class="form-row">
                    <label><g:message code="active"/></label>
                    <input type="checkbox" ${currency.base ? "disabled " : ""} class="medium single" name="active" value="true" uncheck-value="false" ${currency.active ? "checked='checked'" : ""}>
                </div>
            </div>

            <div class="form-row mandatory chosen-wrapper">
                <label><g:message code="price.rounding"/><span class="suggestion"><g:message code="suggestion.currency.rounding"/></span></label>
                <ui:namedSelect name="roundingType" class="medium" key="${com.webcommander.constants.NamedConstants.ROUNDING_TYPE}" value="${currency.roundingType}"/>
            </div>
            <div class="form-row mandatory">
                <label><g:message code="precision"/><span class="suggestion"><g:message code="suggestion.currency.precision"/></span></label>
                <input name="decimalPoints" type="text" validation="required number price gt[0] max[10]" restrict="numeric" value="${currency.decimalPoints}">
            </div>


            %{--<div class="form-row chosen-wrapper">
                <label><g:message code="conversion.type"/><span class="suggestion">  e.g. Manual Conversion</span> </label>
                <select class="medium" ${currency.base ? "disabled " : ""}name="manualConversion" toggle-target="manual-conversion">
                    <option value="true" ${currency.manualConversion ? "selected" : ""}><g:message code="manual.conversion"/></option>
                    <option value="false" ${currency.manualConversion ? "" : "selected"}><g:message code="automatic.conversion"/></option>
                </select>
            </div>
            <div class="form-row mandatory manual-conversion-true">
                <label><g:message code="conversion.rate"/><span class="suggestion"><g:message code="suggestion.currency.rate"/></span></label>
                <input type="text" ${currency.base ? "disabled " : ""}class="medium" name="conversionRate" ${currency.id ? "value='${currency.conversionRate}'" : ""} validation="skip@if{self::hidden} required number price gt[0]" restrict="decimal">
            </div>
            <div class="form-row mandatory manual-conversion-false">
                <label><g:message code="url"/></label>
                <input type="text" ${currency.base ? "disabled " : ""}class="medium" name="url" ${currency.id ? "value='${currency.url.encodeAsBMHTML()}'" : ""}  validation="skip@if{self::hidden} required url">
            </div>
            <div class="form-row mandatory manual-conversion-false">
                <label><g:message code="update.script"/></label>
                <textarea class="medium" ${currency.base ? "disabled " : ""}name="updateScript" maxlength="500" validation="skip@if{self::hidden} required maxlength[500]">${currency.updateScript}</textarea>
            </div>--}%

            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="${currency.id ? "update" : "save"}"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>

        </div>
    </div>
</form>