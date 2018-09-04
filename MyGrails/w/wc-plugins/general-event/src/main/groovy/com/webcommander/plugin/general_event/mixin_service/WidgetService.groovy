package com.webcommander.plugin.general_event.mixin_service

/**
 * Created by arman on 1/3/2016.
 */
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.general_event.GeneralEventService
import com.webcommander.widget.Widget
import com.webcommander.widget.WidgetContent
import grails.converters.JSON
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.transaction.annotation.Transactional

class WidgetService {

    static CommonService _commonService
    static GeneralEventService _generalEventService

    private CommonService getCommonService() {
        if(_commonService) {
            return _commonService
        }
        return _commonService = Holders.grailsApplication.mainContext.getBean(CommonService)
    }
    private GeneralEventService getGeneralEventService() {
        if(_generalEventService) {
            return _generalEventService
        }
        return _generalEventService = Holders.grailsApplication.mainContext.getBean(GeneralEventService)
    }

    @Transactional(readOnly = true)
    def saveGeneralEventWidget(Widget widget, GrailsParameterMap params) {
        Map props = [
                selectionType: params.selectionType,
                displayType: params.displayType,
                showPrice: params.showPrice,
                showBookNow: params.showBookNow,
                labelForBookNow: params.labelForBookNow,
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
                            type: DomainConstants.WIDGET_CONTENT_TYPE.GENERAL_EVENT
                    )
            )
        }
        return widget;
    }

    def populateGeneralEventInitialContentNConfig(Widget widget) {
        widget.params = '{"selectionType": "event", "showPrice": "1", "labelForBookNow": "s:book.now", "displayType": "basic-calendar", "detailsOn": "mouseover", "dayChar": "3"}'
    }

    def renderGeneralEventWidget(Widget widget, Writer writer) {
        Map modelAndView = generalEventService.renderEventWidget(widget)
        renderService.renderView(modelAndView.view, modelAndView.model, writer)
    }
}
