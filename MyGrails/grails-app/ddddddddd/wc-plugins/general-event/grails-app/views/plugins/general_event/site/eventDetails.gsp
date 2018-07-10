<%
    if(!request.elevate_zoom_added) {
        if(!request.js_cache) {
            request.js_cache = []
        }
        request.js_cache.add("js/site/jquery.elevatezoom.js");
        request.elevate_zoom_added = true;
    }
%>
<div class="event-details event-container">
    <g:set var="flag" value="${true}" />
    <input type="hidden" name="eventId" value="${childEvent?.id ?: parentEvent.id}">
    <input type="hidden" name="isRecurring" value="${childEvent ? true : false}" />
    <input type="hidden" id="event_image_zoom_type" value="standard"/>
    <div class="title"><h1>${parentEvent.heading ? parentEvent.heading.encodeAsBMHTML() : parentEvent.name.encodeAsBMHTML()}</h1></div>
    <div class="two-column-container">
        <div class="image-preview event-image-container">
            <div class="image-preview-box event-image-preview-box">
                <g:set var="basePath" value="resources/general-event/event-${parentEvent.id}/images/"/>
                <g:set var="imgPath" value="${parentEvent.images ? "${basePath}300-${parentEvent.images[0].name}" : "resources/general-event/default/300-default.png"}"/>
                <g:set var="originalImageUrl" value="${parentEvent.images ? basePath + parentEvent.images[0].name : "resources/general-event/default/default.png"}"/>
                <div class="vertical-aligner"></div><img src="${app.customResourceBaseUrl() + imgPath}" data-zoom-image="${app.customResourceBaseUrl() + originalImageUrl}">
            </div>
            <g:if test="${parentEvent.images.size() > 1}">
                <input type="hidden" id="thumb-image-size-cache" value="${150}">
                <input type="hidden" id="detail-image-size-cache" value="${600}">
                <input type="hidden" id="popup-image-size-cache" value="${800}">
                <div class="event-image-container multi-image-scroll-wrapper" image-size=4>
                    <div class="image-left-scroller scroll-navigator"></div><div class="image-right-scroller scroll-navigator"></div>
                    <div class="image-thumb-container event-thumb-image-view">
                        <g:each in="${parentEvent.images}" var="image" status="i">
                            <g:set var="originalSrc" value="${app.customResourceBaseUrl()}resources/general-event/event-${parentEvent?.id}/images/${image.name}"/>
                            <div class="thumb-image" image-name="${image.name}" index="${i+1}" original-src="${originalSrc}">
                                <div class="vertical-aligner"></div><img src="${app.customResourceBaseUrl()}resources/general-event/event-${parentEvent?.id}/images/150-${image.name}" index="" data-zoom-image="${originalSrc}">
                            </div>
                        </g:each>
                    </div>
                </div>
            </g:if>
        </div>
        <div class="basic-info">
            <g:if test="${parentEvent.isRecurring.toBoolean()}">
                <%
                    flag = childEvent ? true : false
                %>
                <div class="info-row start-time-row">
                    <label><g:message code="start.time"/>: </label><span>${childEvent?.start ? childEvent.start.toSiteFormat(true, false, session.timezone) : parentEvent.startDateTime.toSiteFormat(true, false, session.timezone)}</span>
                </div>
                <div class="info-row end-time-row">
                    <label><g:message code="end.time"/>: </label><span>${childEvent?.end ? childEvent.end.toSiteFormat(true, false, session.timezone) : parentEvent.endDateTime.toSiteFormat(true, false, session.timezone)}</span>
                </div>
            </g:if>
            <g:else>
                <div class="info-row start-time-row">
                    <label><g:message code="start.time"/>: </label><span>${parentEvent.startDateTime.toSiteFormat(true, false, session.timezone)}</span>
                </div>
                <div class="info-row end-time-row">
                    <label><g:message code="end.time"/>: </label><span>${parentEvent.endDateTime.toSiteFormat(true, false, session.timezone)}</span>
                </div>
            </g:else>
            <g:if test="${parentEvent.file}">
                <g:set var="path" value="${app.customResourceBaseUrl() + "resources/general-event/event-${parentEvent.id}/personalized/" + parentEvent.file}"/>
                <div class="info-row event-downloadable-spec">
                    <a href="${path}" class="et_pdp_download_spec" et-category="link" target="_blank">
                        <span class="file ${parentEvent.file.substring(parentEvent.file.lastIndexOf(".") + 1, parentEvent.file.length())}">
                            <span class="tree-icon"></span>
                        </span>
                        <span class="name">${parentEvent.file.encodeAsBMHTML()}</span>
                    </a>
                </div>
            </g:if>
            <g:if test="${parentEvent.isTicketPurchaseEnabled.toBoolean() && flag && !parentEvent.isVenueEnabled.toBoolean()}">
                <span class="section-price price">
                    <label class="price-amount"><g:message code="ticket.price"/>: $${parentEvent.ticketPrice?.toCurrency()?.toPrice()}</label>
                    <input type="number" class="ticket-quantity-selector text-type" value="${1}">
                    <span class="event-ticket-add-to-cart-button button et_pdp_add_to_cart" et-category="button"><g:message code="add.to.cart"/></span>
                </span>
            </g:if>
            <g:if test="${parentEvent.isVenueEnabled.toBoolean() && parentEvent.venueLocation}">
                <div class="info-row venue-row">
                    <label><g:message code="venue"/>: </label>
                    <span class="venue">
                        <a href="${app.relativeBaseUrl()}location/${parentEvent.venueLocation?.url}">${parentEvent.venueLocation?.venue?.name?.encodeAsBMHTML()}</a>
                    </span>
                </div>
                <div class="info-row venue-location-row">
                    <label></label>
                    <span class="venue-location">${parentEvent.venueLocation?.name?.encodeAsBMHTML()}</span>
                </div>
                <div class="info-row venue-address-row">
                    <label></label>
                    <span class="venue-address">
                        <span>${parentEvent.venueLocation?.venue?.generalAddress?.encodeAsBMHTML()}</span>
                    </span>
                </div>
            </g:if>
            <div class="section-view">
                <g:if test="${parentEvent.isVenueEnabled.toBoolean()}">
                    <g:each in="${parentEvent.venueLocation?.sections}" var="section">
                        <g:if test="${flag}">
                            <g:include controller="generalEvent" action="sectionInfo" params="${[section: section.id, event: parentEvent.id, showCart: parentEvent.isTicketPurchaseEnabled]}"/>
                        </g:if>
                    </g:each>
                </g:if>
            </div>
        </div>
    </div>
    <div class="detail-info">
        <g:if test="${parentEvent.description?.size() > 0}">
            <div class="description">${parentEvent.description}</div>
        </g:if>
        <g:if test="${!parentEvent.isVenueEnabled}">
            <div class="address"><g:message code="location" />: ${parentEvent.generalAddress}</div>
        </g:if>
        <g:if test="${parentEvent.showGoogleMap && !parentEvent.isVenueEnabled}">
            <div class="googleMap">
                <iframe class="google-map" src='http://maps.google.com/maps?source=s_q&hl=en&geocode=&aq=0&ie=UTF8&hq=&z=12&ll=${parentEvent.latitude},${parentEvent.longitude}&output=embed' frameborder='0'>
                </iframe>
            </div>
        </g:if>
    </div>
</div>