<div class="event-details event-container">
    <input type="hidden" name="event" value="${event.id}">
    <div class="title"><h1>${event.heading ? event.heading.encodeAsBMHTML() : event.name.encodeAsBMHTML()}</h1></div>
    <div class="two-column-container">
        <div class="image-preview event-image-container">
            <g:set var="imgPath" value="${event.images ? 'event-' + event.id + '/images/300-' + event.images[0].name : 'default/300-default.png'}"/>
            <div class="image-preview-box event-image-preview-box">
                <div class="vertical-aligner"></div><img src="${app.customResourceBaseUrl()}resources/event/${imgPath}">
            </div>
            <g:if test="${event.images.size() > 1}">
                <div class="event-image-container multi-image-scroll-wrapper" image-size=4>
                    <div class="image-left-scroller scroll-navigator"></div>
                    <div class="image-right-scroller scroll-navigator"></div>
                    <div class="image-thumb-container event-thumb-image-view">
                        <g:each in="${event.images}" var="image">
                            <g:set var="originalSrc" value="${app.customResourceBaseUrl()}resources/event/event-${event?.id}/images/300-${image.name}"/>
                            <div class="thumb-image" name="${image.name}" original-src="${originalSrc}">
                                <div class="vertical-aligner"></div><img src="${app.customResourceBaseUrl()}resources/event/event-${event?.id}/images/100-${image.name}">
                            </div>
                        </g:each>
                    </div>
                </div>
            </g:if>
        </div>
        <div class="basic-info">
            <div class="info-row start-time-row">
                <label><g:message code="start.time"/>: </label><span>${event.startTime?.toSiteFormat(true, false, session.timezone)}</span>
            </div>
            <div class="info-row end-time-row">
                <label><g:message code="end.time"/>: </label><span>${event.endTime?.toSiteFormat(true, false, session.timezone)}</span>
            </div>
            <g:if test="${event.file}">
                <g:set var="path" value="${app.customResourceBaseUrl() + "resources/event/event-${event.id}/personalized/" + event.file}"/>
                <div class="info-row event-downloadable-spec">
                    <a href="${path}" class="et_pdp_download_spec" et-category="link" target="_blank">
                        <span class="file ${event.file.substring(event.file.lastIndexOf(".") + 1, event.file.length())}">
                            <span class="tree-icon"></span>
                        </span>
                        <span class="name">${event.file.encodeAsBMHTML()}</span>
                    </a>
                </div>
            </g:if>
            <g:if test="${event.venueLocation}">
                <div class="info-row venue-row">
                    <label><g:message code="venue"/>: </label>
                    <span class="venue">
                        <a href="${app.relativeBaseUrl()}venueLocation/${event.venueLocation?.url}">${event.venueLocation?.venue?.name?.encodeAsBMHTML()}</a>
                    </span>
                </div>
                <div class="info-row venue-location-row">
                    <label></label>
                    <span class="venue-location">${event.venueLocation?.name?.encodeAsBMHTML()}</span>
                </div>
                <div class="info-row venue-address-row">
                    <label></label>
                    <span class="venue-address">
                        <span>${event.venueLocation?.venue?.address?.encodeAsBMHTML()}</span>
                    </span>
                </div>
            </g:if>
            <div class="section-view">
                <g:each in="${event.venueLocation?.sections}" var="section">
                    <g:include controller="event" action="sectionInfo" params="${[section: section.id, event: event.id, showCart: event.isPurchasable]}"/>
                </g:each>
            </div>
        </div>
    </div>
    <div class="detail-info">
        <g:if test="${event.description?.size() > 0}">
            <div class="description">${event.description}</div>
        </g:if>
    </div>
</div>