<%@ page import="com.webcommander.admin.Operator" %>
<form action="${app.relativeBaseUrl()}eventAdmin/saveVenue" method="post" class="create-edit-form">
    <input type="hidden" name="id" value="${venue.id}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="venue.info"/> </h3>
            <div class="info-content"><g:message code="form.section.text.event.venue.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row mandatory">
                <label><g:message code="name"/><span class="suggestion"><g:message code="suggestion.venue.name"/></span></label>
                <input type="text" name="name" class="large unique"  value="${venue?.name}" validation="required rangelength[2, 100]" maxlength="100" unique-action="isVenueUnique">
            </div>
            <div class="form-row">
                <label><g:message code="address"/><span class="suggestion"><g:message code="suggestion.venue.address"/></span></label>
                <textarea class="large" type="text" name="address">${venue?.address}</textarea>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <label><g:message code="site.url"/><span class="suggestion"><g:message code="suggestion.venue.url"/></span></label>
                    <input type="text" class="large" name="siteUrl" value="${venue.siteUrl.encodeAsBMHTML()}" validation="url">
                </div><div class="form-row chosen-wrapper">
                    <label><g:message code="manager"/><span class="suggestion"><g:message code="suggestion.venue.manager"/></span></label>
                    <ui:domainSelect class="large organiser-selector" name="manager" domain="${Operator}" text="fullName" value="${venue.manager?.id}"/>
                </div>
            </div>
            <div class="form-row">
                <g:set var="googleMap" value="${UUID.randomUUID().toString()}"/>
                <label><g:message code="show.google.map"/></label>
                <input toggle-target="show-latitude-longitude" class="single" type="checkbox" id="${googleMap}" name="showGoogleMap" value="true" uncheck-value="false" ${venue.showGoogleMap ? "checked" : ""}>
            </div>
            <div class="double-input-row show-latitude-longitude">
                <div class="form-row mandatory">
                    <label><g:message code="latitude"/></label>
                    <input type="text" class="large" restrict="decimal" value="${venue?.latitude}" name="latitude"
                           validation="required@if{global:#${googleMap}:checked} number max[99999999]">
                </div><div class="form-row mandatory">
                    <label><g:message code="longitude"/></label>
                    <input type="text" class="large" restrict="decimal" value="${venue?.longitude}" name="longitude"
                           validation="required@if{global:#${googleMap}:checked} number max[99999999]">
                </div>
            </div>
            <div class="form-row">
                <label><g:message code="description"/><span class="suggestion"><g:message code="suggestion.venue.description"/></span></label>
                <textarea class="wceditor no-auto-size xx-larger" style="height: 240px" data-type="advanced" name="description">${venue.description}</textarea>
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="${venue.id ? "update" : "save"}"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</form>