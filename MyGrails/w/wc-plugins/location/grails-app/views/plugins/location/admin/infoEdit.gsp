<%@  page import="com.webcommander.manager.HookManager; com.webcommander.constants.NamedConstants; com.webcommander.util.StringUtil; com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants" %>
<g:set var="defaultCountryId" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, 'default_country')}"></g:set>
<form id="location-form" action="${app.relativeBaseUrl()}LocationAdmin/save" method="post" class="create-edit-form location-create-edit">
    <input name="id" type="hidden" value="${location?.id}">
    <input name="latitude" id="lat" type="hidden" value="${location?.latitude}">
    <input name="longitude" id="lng" type="hidden" value="${location?.longitude}">
    <input name="formattedAddress" id="formatted-address" type="hidden" value="${location?.formattedAddress}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="configure.your.location"/></h3>
            <div class="info-content"><g:message code="section.text.location.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <label><g:message code="location"/><span class="suggestion"><g:message code="suggestion.location.name"/> </span></label>
                <input name="name" id="location-name" type="text" placeholder="" class="form-full-width" value="${location?.name.encodeAsBMHTML()}">
            </div>
            <div class="form-row">
                <label><g:message code="location.name"/><span class="suggestion"><g:message code="suggestion.location.heading.name"/> </span></label>
                <input name="locationHeadingName" id="location-heading-name" type="text" placeholder="" class="form-full-width" value="${location?.locationHeadingName.encodeAsBMHTML()}">
            </div>
            <div class="form-row">
                <label><g:message code="location.address"/></label>
                <input name="locationAddress" id="location-address" type="text" class="form-full-width" value="${location?.locationAddress.encodeAsBMHTML()}">
            </div>
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="location.post.code"/>  <span class="suggestion"><g:message code="suggestion.location.post.code"/> </span></label>
                    <input name="postCode" id="location-post-code" type="text" class="post-code form-full-width" value="${location?.postCode.encodeAsBMHTML()}" validation="required">
                </div>
                <div class="form-row half">
                    <label><g:message code="location.suburb.city"/><span class="suggestion"><g:message code="suggestion.location.suburb.city"/> </span></label>
                    <input name="city" id="location-subrub-city" type="text" class="form-full-width" value="${location?.city.encodeAsBMHTML()}">
                </div>
            </div>
            <div class="form-row country-selector-row chosen-wrapper">
                <label><g:message code="country"/><span class="suggestion"><g:message code="suggestion.setting.country"/></span></label>
                <ui:countryList id="countryId" name="country.id"  class="large" value="${location?.country?.id ?: defaultCountryId.toLong()}"/>
            </div>
            <g:include view="/plugins/location/admin/stateFormFieldView.gsp" model="[states : states, stateId: location?.state?.id]" params="[stateName: 'state.id']"/>
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="location.contact.email"/> <span class="suggestion"><g:message code="suggestion.location.contact-email"/></span></label>
                    <input name="contactEmail" id="location-contact-email" type="text" class="form-full-width" value="${location?.contactEmail.encodeAsBMHTML()}" validation="required email">
                    <span><input type="checkbox" id="location-contact-email-checkbox" name="showEmailInDetails" ${location?.showEmailInDetails == true ? "checked": ""}><g:message code="location.contact.email.checkbox"/></span>
                </div>
                <div class="form-row half">
                    <label><g:message code="location.phone.number"/><span class="suggestion"><g:message code="suggestion.location.phone.number"/> </span></label>
                    <input name="phoneNumber" id="location-phone-number" type="text" class="form-full-width" value="${location?.phoneNumber.encodeAsBMHTML()}" validation="phone">
                    <span><input type="checkbox" id="location-phone-number-checkbox" name="showPhoneNumberInDetails" ${location?.showPhoneNumberInDetails == true ? "checked": ""}><g:message code="location.phone.number.checkbox"/></span>
                </div>
            </div>
            <div class="form-row">
                <label><g:message code="location.description"/></label>
                <textarea name="description" id="location-description" class="no-auto-size form-full-width wceditor xx-larger" toolbar-type="advanced">${location?.description}</textarea>
            </div>
            <div class="display-availability-true">
                <div class="double-input-row">
                    <div class="form-row">
                        <span><g:message code="location.webpage.details"/></span>
                        <input type="checkbox" class="single" name="showWebpageInDetails" toggle-target="webpage-details" ${location?.showWebpageInDetails == true ? "checked": ""}>
                    </div>
                </div>
                <div class="webpage-details">
                    <div class="form-row">
                        <label><g:message code="location.webpage.link.text"/><span class="suggestion"><g:message code="suggestion.location.webpage.link.text"/> </span></label>
                        <input name="linkText" id="location-webpage-link-text" type="text" %{--class="form-full-width"--}% value="${location?.linkText.encodeAsBMHTML()}">
                    </div>
                    <div class="form-row">
                        <label><g:message code="location.webpage.link.webpage"/><span class="suggestion"><g:message code="suggestion.location.webpage.link.webpage"/> </span></label>
                        <input name="webpageUrl" id="location-webpage-link-webpage" type="text" class="form-full-width" value="${location?.webpageUrl.encodeAsBMHTML()}">
                    </div>
                </div>
            </div>

            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="${location?.id ? "update" : "save"}"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</form>
