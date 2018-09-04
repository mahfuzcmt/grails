package com.webcommander.plugin.event_management.controllers.admin.design

import com.webcommander.authentication.annotations.License
import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.event_management.Event
import com.webcommander.plugin.event_management.VenueLocation
import com.webcommander.plugin.event_management.webmarketing.EventService
import com.webcommander.plugin.event_management.webmarketing.VenueService
import com.webcommander.widget.WidgetContent
import grails.util.Holders
import grails.web.Action

class WidgetController {

    static EventService _eventService
    static VenueService _venueService

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

    @Action
    def editEvent() {
        Collection<WidgetContent> widgetContents = params.widget.widgetContent
        List<Event> events = []
        List<VenueLocation> venueLocations = []
        if(widgetContents.size() > 0) {
            List<Long> contentIds = params.widget.widgetContent.contentId
            if((params.widget.widgetContent.type).contains(DomainConstants.WIDGET_CONTENT_TYPE.EVENT)) {
                events = eventService.getEventsForWidgetContent(contentIds)
            } else {
                venueLocations = venueService.getVenueLocationsForWidgetContent(contentIds)
            }
        }
        render view: "/plugins/event_management/admin/widget/eventSettings", model: [selectedEvents: events, selectedLocations: venueLocations, preventSort: true];
    }

    @Action
    @License(required = "allow_event_feature")
    def eventShortConfig() {
        render view: "/plugins/event_management/admin/widget/eventSettingsShort", model: [advanceText: g.message(code: "configure")];
    }

    @Action
    def saveEventWidget() {
        saveAnyWidget("Event")
    }
}