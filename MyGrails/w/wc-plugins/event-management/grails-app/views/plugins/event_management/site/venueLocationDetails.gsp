<div class="venue-location-container">
    <div class="header-container">
        <span class="title venue-location-name">${location.name.encodeAsBMHTML()}</span>
    </div>
    <div class="upper-container">
        <div class="upper-left-container">
            <div class="location-image-container">
                <g:set var="imgPath" value="${location.images ? 'location-' + location.id + '/300-' + location.images[0].name : 'default/300-default.png'}"/>
                <div class="image-preview-box location-image-preview-box">
                    <div class="vertical-aligner"></div><img src="${app.customResourceBaseUrl()}resources/venue-location/${imgPath}">
                </div>
                <g:if test="${location.images?.size() > 1}">
                    <div class="multi-image-scroll-wrapper">
                        <div class="image-left-scroller scroll-navigator"></div>
                        <div class="image-right-scroller scroll-navigator"></div>
                        <div class="image-thumb-container location-thumb-view">
                            <g:each in="${location.images}" var="image">
                                <g:set var="originalSrc" value="${app.customResourceBaseUrl()}resources/venue-location/location-${location?.id}/300-${image.name}"/>
                                <span class="location-thumb-image" name="${image.name}" original-src="${originalSrc}">
                                    <div class="vertical-aligner"></div><img src="${app.customResourceBaseUrl()}resources/venue-location/location-${location?.id}/100-${image.name}">
                                </span>
                            </g:each>
                        </div>
                    </div>
                </g:if>
            </div>
        </div>
        <div class="upper-right-container image-container">
            <div class="venue-location-details">
                <g:if test="${location.venue.address}">
                    <div class="address">
                        <span class="address">${location.venue.address.encodeAsBMHTML()}</span>
                    </div>
                </g:if>
                <g:if test="${location.venue.url}">
                    <div class="venue-site-url">
                        <span class="site-url"><a href="${location.venue.siteUrl}" target="_blank">${location.venue.siteUrl}</a></span>
                    </div>
                </g:if>
            </div>
        </div>
    </div>
    <g:if test="${location.venue.showGoogleMap}">
        <div class="googleMap">
            <iframe class="google-map" src='http://maps.google.com/maps?source=s_q&hl=en&geocode=&aq=0&ie=UTF8&hq=&z=12&ll=${location.venue.latitude},${location.venue.longitude}&output=embed' frameborder='0'>
            </iframe>
        </div>
    </g:if>
    <div class="lower-container">
        <div class="bmui-tab">
            <div class="bmui-tab-header-container">
                <div class="bmui-tab-header" data-tabify-tab-id="description">
                    <span class="title"><g:message code="description"/></span>
                </div>
                <g:each in="${location.sections}" var="section" status="i">
                    <div class="bmui-tab-header" data-tabify-tab-id="section-${i}" data-tabify-url="${app.relativeBaseUrl()}event/seatMap?section=${section.id}">
                        <span class="title"><g:message code="seat.plan"/>: ${section.name}</span>
                    </div>
                </g:each>
            </div>
            <div class="bmui-tab-body-container">
                <div id="bmui-tab-description">
                    <g:if test=" ${location.description}">
                        <div class="description">
                            <span class="description">${location.description}</span>
                        </div>
                    </g:if>
                </div>
                <g:each in="${location.sections}" var="section" status="i">
                    <div id="bmui-tab-section-${i}"></div>
                </g:each>
            </div>
        </div>
    </div>
</div>