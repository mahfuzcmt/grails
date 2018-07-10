package com.webcommander.plugin.form_editor.controllers.admin.design

import com.webcommander.authentication.annotations.License
import com.webcommander.plugin.form_editor.admin.FormService

/**
 * Created by shahin on 27/02/14.
 */
class WidgetController {
    
    FormService formService


    @License(required = "allow_form_builder_feature")
    def formShortConfig() {
        def forms = formService.getFormsForWidget();
        render(view: "/plugins/form_editor/admin/widget/loadFormSettingsShort", model: [noAdvance: true, form: params.widget.widgetContent.size() ? params.widget.widgetContent[0].contentId : null, forms: forms]);
    }
}