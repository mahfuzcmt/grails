<%@ page import="com.webcommander.util.AppUtil" %>
<g:applyLayout name="_widget">
    <g:set var="id" value="${UUID.randomUUID().toString()}"/>
    <g:if test="${request.page}">
    <%
        if(!request.is_event_widget_list_view_script_loaded) {
            request.js_cache.push("plugins/event-management/js/widget/event-widget.js")
            request.is_event_widget_list_view_script_loaded = true;
        }
    %>
    </g:if>
    <div class="event-widget-container ${config.listViewType}-view">
        <g:if test="${config.listViewType == 'scrollable' || config.listViewType == 'paginated'}">
                <g:if test="${config.listViewType == 'paginated' && config.paginationPlacement != 'bottom'}">
                    <div class="header">
                        <paginator data-urlprefix="${url_prefix}"  total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
                    </div>
                </g:if>
                <g:if test="${config.listViewType == 'scrollable'}">
                    <div class="header">
                        <div class="scroller">
                            <span class="scroll-left"></span>
                            <span class="scroll-right"></span>
                        </div>
                    </div>
                </g:if>
        </g:if>
        <div class="content event-management event-list ${config.listViewType}-view column two"
            ${config.listViewType == 'show_all' ? 'style="height:' + config.listViewHeight + 'px"' : ''}>
            <div class="${config.listViewType == 'paginated' ? 'paginated' : 'scrollable-wrapper'}">
                <div class="content">
                    <g:each in="${events}" var="event">
                        <div class="event-wrap scrollable-item">
                            <g:set var="imgPath" value="${event.images ? 'event-' + event.id + '/images/100-' + event.images[0].name : 'default/100-default.png'}"/>
                            <div class="thumb-image">
                                <a href="${app.relativeBaseUrl() + 'event/' + event.id}">
                                    <img src="${app.customResourceBaseUrl()}resources/event/${imgPath}">
                                </a>
                            </div>
                            <div class="event-details">
                                <div class="title"><a href="${app.relativeBaseUrl() + 'event/' + event.id}">${event.name.encodeAsBMHTML()}</a></div>
                                <div class="time-range">
                                    <span class="time start">${event.startTime?.toSiteFormat(true, false, session.timezone)}</span>
                                </div>
                                <g:set var="summary" value="${event.summary ? event.summary.encodeAsBMHTML() : event.description ? event.description.textify().truncate(250).encodeAsBMHTML() : ""}"/>
                                <g:if test="${summary?.size() > 0}">
                                    <div class="content">${summary}</div>
                                </g:if>
                                <div class="basic-info">
                                    <g:if test="${config.showPrice == '1' && event.isPurchasable}">
                                        <div class="price">
                                            <label><g:message code="ticket.price"/></label>
                                            <span>${event.ticketPriceWithCurrency()}</span>
                                        </div>
                                    </g:if>
                                    <g:if test="${config.showRequestInfo == '1' && event.file != null}">
                                        <div class="request-info">
                                            <span class="button" event-id="${event.id}" event-name="${event.name}"><g:message code="request.for.details"/></span>
                                        </div>
                                    </g:if>
                                    <g:if test="${config.showBookNow == '1'}">
                                        <div class="book-now">
                                            <a class="button" href="${app.relativeBaseUrl() + 'event/' + event.id}"><g:message code="book.now"/></a>
                                        </div>
                                    </g:if>
                                </div>
                            </div>
                        </div>
                    </g:each>
                </div>
            </div>
        </div>
        <div class="footer">
            <g:if test="${config.listViewType == 'paginated' && config.paginationPlacement != 'top'}">
                <paginator data-urlprefix="${url_prefix}"  total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
            </g:if>
        </div>
    </div>
</g:applyLayout>