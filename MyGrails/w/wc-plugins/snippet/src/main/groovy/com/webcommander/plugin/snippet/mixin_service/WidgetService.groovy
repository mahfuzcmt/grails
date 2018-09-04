package com.webcommander.plugin.snippet.mixin_service

import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.snippet.Snippet
import com.webcommander.widget.Widget
import com.webcommander.widget.WidgetContent
import grails.web.servlet.mvc.GrailsParameterMap
import com.webcommander.manager.PathManager

class WidgetService {

    def populateSnippetInitialContentNConfig(Widget widget) {
        Snippet snippet = Snippet.first()
        if(snippet) {
            widget.widgetContent.add(new WidgetContent(type: DomainConstants.WIDGET_CONTENT_TYPE.SNIPPET, contentId: snippet.id, widget: widget))
        }
    }

    def renderSnippetWidget(Widget widget, Writer writer) {
        Long id = widget.widgetContent.size() ? widget.widgetContent[0].contentId : null
        File contentFile = new File(PathManager.getResourceRoot("snippet/snippet-${id}"), "snippet.html")
        String content = contentFile.exists() ? contentFile.text : ""
        renderService.renderView("/plugins/snippet/widget/snippetWidget", [widget: widget, id: id, content: content], writer)
    }

    def saveSnippetWidget(Widget widget, GrailsParameterMap params) {
        if(params.snippetId) {
            WidgetContent widgetContent = new WidgetContent(contentId: params.long("snippetId"));
            widgetContent.widget = widget;
            widgetContent.type = DomainConstants.WIDGET_CONTENT_TYPE.SNIPPET
            widget.widgetContent.add(widgetContent);
        }
    }
}
