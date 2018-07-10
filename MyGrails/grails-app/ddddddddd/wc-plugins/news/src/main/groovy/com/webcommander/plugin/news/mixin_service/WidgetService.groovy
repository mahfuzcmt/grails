package com.webcommander.plugin.news.mixin_service

import com.webcommander.plugin.news.News
import com.webcommander.plugin.news.NewsService
import com.webcommander.widget.Widget
import grails.converters.JSON
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap

class WidgetService {
    private static NewsService _newsService
    private static NewsService getNewsService(){
        return _newsService ?: (Holders.grailsApplication.mainContext.getBean(NewsService))
    }

    def populateNewsInitialContentNConfig(Widget widget) {
        widget.params = '{"news_transition": "vertical.scroll", "height": 600, "transition_speed": 2, "transition_direction": "descending.news.date", "news_per_phase": 5, "selection": "all"}'
    }


    def saveNewsWidget(Widget widget, GrailsParameterMap params) {
        Map paramsMap = [
                news_transition     : params.news_transition,
                transition_speed    : params.transition_speed,
                transition_direction: params.transition_direction,
                news_per_phase      : params.news_per_phase,
                selection           : params.selection,
                height              : params.height];
        if ((params.selection == "date_range")) {
            Map tmpMap = [news_from: params.news_from,
                          news_to  : params.news_to];
            paramsMap << tmpMap;
        }
        widget.params = JSON.use("deep") {
            paramsMap as JSON
        }.toString();
    }

    def renderNewsWidget(Widget widget, Writer writer) {
        def config = JSON.parse(widget.params)
        List<News> newses = newsService.getNewsesForWidget(config)
        renderService.renderView("/plugins/news/widget/newsWidget", [widget: widget, config: config, newses: newses], writer)
    }
}
