package com.webcommander.plugin.event_management.mixin_service

import com.webcommander.JSONSerializableList
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.event_management.Event
import com.webcommander.plugin.event_management.EventSession
import com.webcommander.plugin.event_management.VenueLocation
import com.webcommander.plugin.event_management.model.EventData
import com.webcommander.plugin.event_management.webmarketing.EventService
import com.webcommander.plugin.event_management.webmarketing.VenueService
import com.webcommander.throwables.UnconfiguredWidgetExceptions
import com.webcommander.widget.Widget
import com.webcommander.widget.WidgetContent
import grails.converters.JSON
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.request.RequestContextHolder

class WidgetService {

    static CommonService _commonService
    static EventService _eventService
    static VenueService _venueService

    private CommonService getCommonService() {
        if(_commonService) {
            return _commonService
        }
        return _commonService = Holders.grailsApplication.mainContext.getBean(CommonService)
    }

    private EventService getEventService() {
        if(_eventService) {
            return _eventService
        }
        return _eventService = Holders.grailsApplication.mainContext.getBean(EventService)
    }

    private VenueService getVenueService() {
        if(_venueService) {
            return _venueService
        }
        return _venueService = Holders.grailsApplication.mainContext.getBean(VenueService)
    }

    @Transactional(readOnly = true)
    def saveEventWidget(Widget widget, GrailsParameterMap params) {
        Map props = [
            selectionType: params.selectionType,
            displayType: params.displayType,
            showPrice: params.showPrice,
            showBookNow: params.showBookNow,
            showAvailability: params.showAvailability,
            showRequestInfo: params.showRequestInfo,
            showSubscribe: params.showSubscribe,
            embedShortInfo: params.embedShortInfo,
            detailsOn: params.detailsOn,
            dayChar: params.dayChar,
            listViewType: params.listViewType,
            paginationPlacement: params.paginationPlacement,
            itemsPerPage: params.itemsPerPage,
            listViewHeight: params.listViewHeight
        ]

        widget.params = JSON.use("deep") {
            props as JSON
        }.toString();

        List<Long> eventIds = params.list("event").collect { it.toLong() }
        eventIds.each {
            widget.widgetContent.add(
                    new WidgetContent(
                            contentId: it,
                            widget: widget,
                            type: DomainConstants.WIDGET_CONTENT_TYPE.EVENT
                    )
            )
        }

        List<Long> venueIds = params.list("venueLocation").collect { it.toLong() }
        venueIds.each {
            widget.widgetContent.add(
                    new WidgetContent(
                            contentId: it,
                            widget: widget,
                            type: DomainConstants.WIDGET_CONTENT_TYPE.VENUE_LOCATION
                    )
            )
        }

        return widget;
    }

    def populateEventInitialContentNConfig(Widget widget) {
        widget.params = '{"selectionType": "all", "showPrice": "1", "displayType": "basic-calendar", "detailsOn": "mouseover", "dayChar": "3"}'
    }

    def renderEventWidget(Widget widget, Writer writer) {
        Map modelAndView = eventService.renderEventWidget(widget)
        renderService.renderView(modelAndView.view, modelAndView.model, writer)
    }
}
