package com.webcommander.plugin.news



class NewsConstants {
    static NEWS_WIDGET_TRANSITION = [
            VERTICAL_SCROLL: "vertical_scroll",
            HORIZONTAL_SCROLL: "horizontal_scroll",
            FADE: "fade",
            NO_TRANSITION:"no_transition"
    ]

    static NEWS_WIDGET_TRANSITION_DIRECTION = [
            ASCENDING_OF_NEWS_DATE:"ascending_of_news_date",
            DESCENDING_OF_NEWS_DATE:"descending_of_news_date",
            RANDOM: "random"
    ]

    static NEWS_WIDGET_TRANSITION_DIRECTION_MESSAGE_KEYS = [
            (NEWS_WIDGET_TRANSITION_DIRECTION.ASCENDING_OF_NEWS_DATE): "ascending.news.date",
            (NEWS_WIDGET_TRANSITION_DIRECTION.DESCENDING_OF_NEWS_DATE): "descending.news.date",
            (NEWS_WIDGET_TRANSITION_DIRECTION.RANDOM): "random"
    ]

    static NEWS_WIDGET_TRANSITION_MESSAGE_KEYS = [
            (NEWS_WIDGET_TRANSITION.VERTICAL_SCROLL): "vertical.scroll",
            (NEWS_WIDGET_TRANSITION.HORIZONTAL_SCROLL): "horizontal.scroll",
            (NEWS_WIDGET_TRANSITION.FADE): "fade",
            (NEWS_WIDGET_TRANSITION.NO_TRANSITION): "no.transition"
    ]
}
