<div class="event-details event-container">
    <div class="event-session-details">
        <g:if test="${eventSession}">
            <input type="hidden" name="event" value="${eventSession.event.id}">
            <input type="hidden" name="eventSession" value="${eventSession.id}">
            <div class="title"><h2>${eventSession.name.encodeAsBMHTML()}</h2></div>
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
                                    <g:set var="originalSrc" value="${app.customResourceBaseUrl()}resources/event/event-${event.id}/images/300-${image.name}"/>
                                    <div class="thumb-image" name="${image.name}" original-src="${originalSrc}">
                                        <div class="vertical-aligner"></div><img src="${app.customResourceBaseUrl()}resources/event/event-${event.id}/images/100-${image.name}">
                                    </div>
                                </g:each>
                            </div>
                        </div>
                    </g:if>
                </div>
                <div class="basic-info">
                    <div class="info-row">
                        <label><g:message code="start.time"/>: </label><span>${eventSession.startTime?.toSiteFormat(true, false, session.timezone)}</span>
                    </div>
                    <div class="info-row">
                        <label><g:message code="end.time"/>: </label><span>${eventSession.endTime?.toSiteFormat(true, false, session.timezone)}</span>
                    </div>
                    <g:if test="${eventSession.file}">
                        <g:set var="path" value="${app.customResourceBaseUrl() + "resources/event/event-session-${eventSession.id}/personalized/" + eventSession.file}"/>
                        <div class="info-row event-downloadable-spec">
                            <a href="${path}" class="et_pdp_download_spec" et-category="link" target="_blank">
                                <span class="file ${eventSession.file.substring(eventSession.file.lastIndexOf(".") + 1, eventSession.file.length())}">
                                    <span class="tree-icon"></span>
                                </span>
                                <span class="name">${eventSession.file.encodeAsBMHTML()}</span>
                            </a>
                        </div>
                    </g:if>
                    <g:if test="${eventSession.venueLocation}">
                        <div class="info-row">
                            <label><g:message code="venue"/>: </label>
                            <span class="venue">
                                <a href="${app.relativeBaseUrl()}venueLocation/${eventSession.venueLocation?.url}">${eventSession.venueLocation?.venue?.name?.encodeAsBMHTML()}</a>
                            </span>
                        </div>
                        <div class="info-row">
                            <label></label>
                            <span class="venue-location">${eventSession.venueLocation?.name?.encodeAsBMHTML()}</span>
                        </div>
                        <div class="info-row">
                            <label></label>
                            <span class="venue-address">
                                <span>${eventSession.venueLocation?.venue?.address?.encodeAsBMHTML()}</span>
                            </span>
                        </div>
                    </g:if>
                    <g:if test="${event.isPurchasable}">
                        <div class="section-view">
                            <g:each in="${eventSession.venueLocation?.sections}" var="section">
                                <g:include controller="event" action="sectionInfo" params="${[section: section.id, session: eventSession.id, showCart: event.isPurchasable]}" />
                            </g:each>
                        </div>
                    </g:if>
                </div>
            </div>
            <div class="detail-info">
                <div class="event-session-list">
                    <g:if test="${upcomingSessions.size() > 1}">
                        <g:each in="${upcomingSessions}" var="session" status="i">
                            <span class="session-link ${eventSession.id != session.id ?: 'active'}">
                                <a href="${app.relativeBaseUrl()}event/${event.id}/session/${session.id}">${session.name}</a>
                            </span>
                        </g:each>
                    </g:if>
                </div>
                <g:if test="${eventSession.topics?.size() > 0}">
                    <div class="event-session-topic-list">
                        <g:each in="${eventSession.topics}" var="topic" status="i">
                            <div class="session-topic ${i == 0 ? 'first' : (i == eventSession.topics.size() - 1 ? 'last' : '')}">
                                <label>${topic.name}</label>
                                <g:if test="${topic.description?.size() > 0}">
                                    <span>${topic.description.encodeAsBMHTML()}</span>
                                </g:if>
                            </div>
                        </g:each>
                    </div>
                </g:if>
                <g:if test="${eventSession.description?.size() > 0}">
                    <div class="description">${eventSession.description}</div>
                </g:if>
            </div>
        </g:if>
        <g:else>
            <div class="info-message">
                <span><g:message code="no.event.session.found"/></span>
            </div>
        </g:else>
    </div>
</div>
