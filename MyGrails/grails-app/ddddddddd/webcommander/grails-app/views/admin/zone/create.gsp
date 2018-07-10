<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<g:set var="defaultCountryId" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, 'default_country')}"></g:set>
<div class="form-row mandatory">
    <label><g:message code="zone.name"/></label>
    <input type="text" class="${size} unique" name="zone.name" value="${zone ? zone.name : ""}" unique-action="isZoneUnique" validation="required maxlength[100]" maxlength="100">
</div>
<div class="zone-selection">
    <div class="form-row mandatory mandatory-chosen-wrapper">
        <label><g:message code="country"/></label>
        <ui:countryList id="countryId" name="zone.country.id" validation="required" multiple="true" class="${size}" value="${zone ? zone.countries.id : 0}" data-placeholder="${g.message(code: 'select.country')}"/>
    </div>
    <g:include view="/admin/customer/stateFormFieldView.gsp" model="[states: states, stateId: zone?.states?.id]" params="${[inputClass: size, stateName: 'zone.state.id', isMultiple: true]}"/>
    <div class="form-row post-code">
        <label><g:message code="post.code"/></label>
        <div class="${size} multitxtchosen" data-placeholder="<g:message code="enter.post.codes"/>" chosen-validation='match[(^\d{4,7}(-\d{4,7})?$)]' name="zone.postcode">
            <g:each in="${zone?.postCodes}" var="postCode">
                <input type="hidden" name="zone.postcode" value="${postCode}">
            </g:each>
        </div>
    </div>
</div>