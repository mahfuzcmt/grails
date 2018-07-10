package com.webcommander

class RenderTagLib {

    static namespace = "render";
    RenderService renderService;

    def renderPageContent = { attrs, body ->
        if (attrs["value"] instanceof String) {
            try {
                renderService.renderContent(new StringReader(attrs["value"]), out)
            } catch(Throwable u) {
                log.error u.message, u
            }
        } else if(attrs["value"] instanceof Collection) {
            Map groupeds = [:]
            try {
                attrs["value"].each { widget ->
                    if(widget.groupId) {
                        List widgets = groupeds[widget.groupId]
                        if(!widgets) {
                            widgets = groupeds[widget.groupId] = []
                        }
                        widgets.add(widget)
                    } else {
                        String type = widget.widgetType;
                        out << wi.widget(widget: widget, type: type);
                    }
                }
                groupeds.each { group ->
                    out << "<div class='widget-group' id='group-$group.key'>"
                    group.value.each { widget ->
                        String type = widget.widgetType;
                        out << wi.widget(widget: widget, type: type);
                    }
                    out << "</div>"
                }
            } catch(Throwable u) {
                log.error u.message, u
            }
        }
    }
}
