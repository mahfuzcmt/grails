package com.webcommander.plugin.simplified_event_management.controllers.admin.design

import com.webcommander.authentication.annotations.License
import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.simplified_event_management.SimplifiedEvent
import com.webcommander.plugin.simplified_event_management.webmarketing.SimplifiedEventService
import com.webcommander.widget.WidgetContent
import grails.util.Holders
import grails.web.Action

class WidgetController {
    def widgetService
    SimplifiedEventService simplifiedEventService

    def editSimplifiedEvent() {
        Collection<WidgetContent> widgetContents = params.widget.widgetContent
        List<SimplifiedEvent> events = []
        if(widgetContents.size() > 0) {
            List<Long> contentIds = params.widget.widgetContent.contentId
            if((params.widget.widgetContent.type).contains(DomainConstants.WIDGET_CONTENT_TYPE.SIMPLIFIED_EVENT)) {
                events = simplifiedEventService.getEventsForWidgetContent(contentIds)
            }
        }
        render view: "/plugins/simplified_event_management/admin/widget/eventSettings", model: [selectedEvents: events, preventSort: true];
    }


    @License(required = "allow_simplified_event_feature")
    def simplifiedEventShortConfig() {
        render view: "/plugins/simplified_event_management/admin/widget/eventSettingsShort", model: [advanceText: g.message(code: "configure")];
    }


    def saveSimplifiedEventWidget() {
        render(widgetService.saveAnyWidget("SimplifiedEvent", params))
    }
}