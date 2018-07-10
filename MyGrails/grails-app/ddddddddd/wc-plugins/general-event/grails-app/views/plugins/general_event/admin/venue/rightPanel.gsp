<div class="venue-container">
    <form action="" method="post" class="create-edit-form edit-popup-form venue-edit-form">
        <input type="hidden" class="venue-id" name="id" value="${venue?.id}" >
        <div class="form-section">
            <div class="form-section-info-block">
                <h3>${venue.name.encodeAsBMHTML()}</h3>
                <div class="info-content"><g:message code="section.text.venue.info"/></div>
            </div>
            <div class="form-section-container-block">
                <div class="double-input-row">
                    <div class="form-row mandatory">
                        <label><g:message code="name"/></label>
                        <input type="text" name="name" class="large unique"  value="${venue?.name}" validation="required rangelength[2, 100]" maxlength="100" unique-action="isVenueUnique">
                    </div><div class="form-row">
                        <label><g:message code="url"/><span class="suggestion">e. g. http://www.abc.com</span></label>
                        <input type="text" class="large" name="siteUrl" value="${venue.siteUrl.encodeAsBMHTML()}" validation="url">
                    </div>
                </div>
                <div class="double-input-row">
                    <div class="form-row">
                        <label><g:message code="address"/></label>
                        <input type="text" class="large" type="text" value="${venue.generalAddress}" name="generalAddress" />
                    </div><div class="form-row">
                        <g:set var="googleMap" value="${UUID.randomUUID().toString()}"/>
                        <label><g:message code="show.google.map"/></label>
                        <input toggle-target="show-latitude-longitude" class="single" type="checkbox" id="${googleMap}" name="showGoogleMap" value="true" uncheck-value="false" ${venue.showGoogleMap ? "checked" : ""}>
                    </div>
                </div>
                <div class="double-input-row show-latitude-longitude">
                    <div class="form-row mandatory">
                        <label><g:message code="latitude"/></label>
                        <input type="text" class="large" value="${venue?.latitude}" name="latitude" validation="required@if{global:#${googleMap}:checked} number max[99999999]">
                    </div><div class="form-row mandatory">
                        <label><g:message code="longitude"/></label>
                        <input type="text" class="large" value="${venue?.longitude}" name="longitude" validation="required@if{global:#${googleMap}:checked} number max[99999999]">
                    </div>
                </div>
            </div>
        </div>
    </form>
    <g:include view="/plugins/general_event/admin/venue/location/appView.gsp" model="[venue: venue, clazz: 'scrollable-rule']"/>
</div>