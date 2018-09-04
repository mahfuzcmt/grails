package com.webcommander.plugin.ask_question

import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

class AskQuestionShareTagLib {
    static namespace = "askQuestion"
    @Autowired
    @Qualifier("org.grails.plugins.web.taglib.ApplicationTagLib")
    ApplicationTagLib g;
    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    com.webcommander.ApplicationTagLib app

    def body = { attrs, body ->
        out << body()
        Boolean question = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ASK_QUESTION, "ask_question").toBoolean()
        if(question) {
            app.enqueueSiteJs(src: "plugins/ask-question/js/shared/ask-question.js", scriptId: "ask-question")
            out << '<div id="bmui-tab-askQuestion">'
            out << '</div>'
        }
    }

    def header = { attrs, body ->
        out << body()
        Boolean question = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ASK_QUESTION, "ask_question").toBoolean()
        if(question) {
            out << '<div class="bmui-tab-header" data-tabify-tab-id="askQuestion" data-tabify-url="' + app.relativeBaseUrl()
            out << 'askQuestion/questionTab?productId=' + attrs.productId + '"'
            out << 'load_url="' + app.relativeBaseUrl() + 'askQuestion/questionTab?productId=' + attrs.productId + '">'
            out << '<span class="title">' + g.message(code: "ask.question") + '</span>'
            out << '</div>'
        }
    }
}
