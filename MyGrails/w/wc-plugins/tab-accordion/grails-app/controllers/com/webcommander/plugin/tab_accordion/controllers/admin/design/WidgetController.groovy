package com.webcommander.plugin.tab_accordion.controllers.admin.design


class WidgetController{

    def widgetService

    def tabAccordionShortConfig() {
        render(view: "/plugins/tab_accordion/admin/widget/shortConfig", model: [advanceText: g.message(code: 'configure')])
    }


    def editTabAccordion() {
        if(!params.config['type']) {
            params.config['type'] = "tab"
        }
        render(view: "/plugins/tab_accordion/admin/widget/loadConfig", model: [:])
    }


    def saveTabAccordionWidget() {
        render(widgetService.saveAnyWidget("TabAccordion", params))
    }
}