package com.webcommander.plugin.simplified_event_management.mixin_service

import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.simplified_event_management.webmarketing.SimplifiedEventService
import com.webcommander.plugin.simplified_event_management.webmarketing.SimplifiedEventService
import com.webcommander.widget.Widget
import com.webcommander.widget.WidgetContent
import grails.converters.JSON
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.transaction.annotation.Transactional

class WidgetService {

    static CommonService _commonService
    static SimplifiedEventService _simplifiedEventService

    private CommonService getCommonService() {
        if(_commonService) {
            return _commonService
        }
        return _commonService = Holders.grailsApplication.mainContext.getBean(CommonService)
    }

    private SimplifiedEventService getSimplifiedEventService() {
        if(_simplifiedEventService) {
            return _simplifiedEventService
        }
        return _simplifiedEventService = Holders.grailsApplication.mainContext.getBean(SimplifiedEventService)
    }

    @Transactional(readOnly = true)
    def saveSimplifiedEventWidget(Widget widget, GrailsParameterMap params) {
        Map props = [
            selectionType: params.selectionType,
            displayType: params.displayType,
            showPrice: params.showPrice,
            showBookNow: params.showBookNow,
            labelForBookNow: params.labelForBookNow,
            showRequestInfo: params.showRequestInfo,
            showSubscribe: params.showSubscribe,
            detailsOn: params.detailsOn,
            dayChar: params.dayChar
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
                            type: DomainConstants.WIDGET_CONTENT_TYPE.SIMPLIFIED_EVENT
                    )
            )
        }

        return widget;
    }

    def populateSimplifiedEventInitialContentNConfig(Widget widget) {
        widget.params = '{"selectionType": "event", "showPrice": "1", "labelForBookNow": "s:book.now", "displayType": "basic-calendar", "detailsOn": "mouseover", "dayChar": "3"}'
    }

    def renderSimplifiedEventWidget(Widget widget, Writer writer) {
        Map modelAndView = simplifiedEventService.renderEventWidget(widget)
        renderService.renderView(modelAndView.view, modelAndView.model, writer)
    }
}
