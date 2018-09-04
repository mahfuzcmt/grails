package com.webcommander.plugin.ask_question

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class AskQuestionGrailsPlugin extends WebCommanderPluginBase {

    def title = "Ask Question"
    def author = "Sadikullah Zobair"
    def authorEmail = "zobair@bitmascot.com"
    def description = '''Take questions about product'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/ask-question";
    {
        _plugin = new PluginMeta(identifier: "ask-question", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        hooks = [productInfoTabBody: [taglib: "askQuestion", callable: "body"], productInfoTabHeader: [taglib: "askQuestion", callable: "header"]]
    }


}
