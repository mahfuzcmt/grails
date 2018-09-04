<form action="${app.relativeBaseUrl()}generalEventAdmin/saveVenueLocation" method="post" class="create-edit-form" enctype="multipart/form-data">
    <input type="hidden" name="id" value="${location?.id}">
    <input type="hidden" name="venueId" value="${venueId}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="location.info"/> </h3>
            <div class="info-content"><g:message code="form.section.text.event.location"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row mandatory">
                <label><g:message code="name"/></label>
                <input type="text" name="name" class="large" value="${location?.name.encodeAsBMHTML()}" validation="required rangelength[2, 100]" maxlength="100">
            </div>
            <div class="form-row">
                <label><g:message code="description"/></label>
                <textarea class="wceditor no-auto-size xx-larger" style="height: 240px" name="description">${location?.description}</textarea>
            </div>
            <div class="form-row">
                <label>&nbsp;</label>
                <label><g:message code="image"/></label>
                <input type="file" name="images" file-type="image" queue="location-image-queue" multiple="true" >
                <div id="location-image-queue" class="multiple-image-queue">
                </div>
                <hr>
                <div class="location-image-container">
                    <div class="left-scroller scroll-navigator" style="display: none"></div><div class="right-scroller scroll-navigator" style="display: none"></div>
                    <div class="location-image-wrapper one-line-scroll-content">
                        <g:each in="${location?.images}" var="image">
                            <div image-id="${image.id}" image-name="${image.name}" class="image-thumb">
                                <span class="tool-icon remove"></span>
                                <input type="hidden" name="imageId" value="${image.id}">
                                <div class="image-container">
                                    <img src="${app.customResourceBaseUrl()}resources/general-event-venue-location/location-${location?.id}/100-${image.name}">
                                </div>
                            </div>
                        </g:each>
                    </div>
                </div>
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="${location.id ? "update" : "save"}"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>

</form>