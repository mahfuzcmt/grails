package com.webcommander.plugin.form_editor.controllers.admin.design

import com.webcommander.plugin.form_editor.admin.FormService


class FrontEndEditorController {

    FormService formService


    def formConfig() {
        def forms = formService.getFormsForWidget();
        render(view: "/plugins/form_editor/front-end-editor/loadConfig", model: [noAdvance: true, form: params.widget.widgetContent.size() ? params.widget.widgetContent[0].contentId : null, forms: forms])
    }


    def saveFormWidget() {
        saveWidget()
    }
}
