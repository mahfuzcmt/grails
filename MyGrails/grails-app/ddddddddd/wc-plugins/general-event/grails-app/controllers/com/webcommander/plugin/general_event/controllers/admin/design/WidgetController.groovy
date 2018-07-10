package com.webcommander.plugin.general_event.controllers.admin.design

/**
 * Created by arman on 1/3/2016.
 */
import com.webcommander.authentication.annotations.License
import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.general_event.GeneralEvent
import com.webcommander.plugin.general_event.GeneralEventService
import com.webcommander.widget.WidgetContent
import grails.util.Holders
import grails.web.Action

class WidgetController {

    GeneralEventService generalEventService


    def editGeneralEvent() {
        Collection<WidgetContent> widgetContents = params.widget.widgetContent
        List<GeneralEvent> events = []
        if(widgetContents.size() > 0) {
            List<Long> contentIds = params.widget.widgetContent.contentId
            if((params.widget.widgetContent.type).contains(DomainConstants.WIDGET_CONTENT_TYPE.GENERAL_EVENT)) {
                events = generalEventService.getEventsForWidgetContent(contentIds)
            }
        }
        render view: "/plugins/general_event/admin/widget/eventSettings", model: [selectedEvents: events, preventSort: true];
    }


    @License(required = "allow_general_event_feature")
    def generalEventShortConfig() {
        render view: "/plugins/general_event/admin/widget/eventSettingsShort", model: [advanceText: g.message(code: "configure")];
    }


    def saveGeneralEventWidget() {
        saveAnyWidget("GeneralEvent")
    }
}
