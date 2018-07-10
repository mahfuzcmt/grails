package com.webcommander.plugin.form_editor.mixin_service

import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.form_editor.Form
import com.webcommander.plugin.form_editor.admin.FormService
import com.webcommander.widget.Widget
import com.webcommander.widget.WidgetContent
import grails.converters.JSON
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap

class WidgetService {
    static FormService _formService

    private FormService getFormService() {
        if (_formService) {
            return _formService
        }
        return _formService = Holders.grailsApplication.mainContext.getBean(FormService)
    }

    def populateFormInitialContentNConfig(Widget widget) {
        Form frm = Form.findByIsDisposableAndIsInTrash(false, false)
        if (frm) {
            widget.widgetContent.add(new WidgetContent(type: DomainConstants.WIDGET_CONTENT_TYPE.FORM, contentId: frm.id, widget: widget))
        }
    }

    def renderFormWidget(Widget widget, Writer writer) {
        def config = JSON.parse(widget.params)
        def formData = widget.widgetContent.size() ? Form.get(widget.widgetContent[0].contentId) : null;
        if (formData) {
            renderService.renderView("/plugins/form_editor/widget/formWidget", [widget: widget, config: config, form: formData], writer)
        } else {
            renderService.renderView("/widget/blankWidget", [widget: widget], writer)
        }
    }

    def saveFormWidget(Widget widget, GrailsParameterMap params) {
        widget.params = params.params;
        if (params.form) {
            WidgetContent widgetContent = new WidgetContent(contentId: params.long("form"));
            widgetContent.widget = widget;
            widgetContent.type = DomainConstants.WIDGET_CONTENT_TYPE.FORM
            widget.widgetContent.add(widgetContent);
        }
    }
}
