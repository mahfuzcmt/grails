package com.webcommander.plugin.tab_accordion.controllers.admin.design



class FrontEndEditorController {

    def tabAccordionConfig() {
        if (!params.config['type']) {
            params.config['type'] = "tab"
        }
        render(view: "/plugins/tab_accordion/front-end-editor/loadConfig", model: [:])
    }

    def saveTabAccordionWidget() {
        saveWidget()
    }
}
