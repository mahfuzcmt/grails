app.widget.snippet = function(config) {
    app.widget.snippet._super.constructor.apply(this, arguments);
}

app.widget.snippet.config_width = 500;

var _w = app.widget.snippet.inherit(app.widget.base);

_w.updateCacheForShortConfig = function(cache, serialized) {
    return app.widget.snippet._super.updateCacheForShortConfig.call(this, cache, serialized, "snippetId", "snippet")
};

app.widget.snippet.initShortConfig = function(configDom, widget, editorTab) {
    configDom.find(".edit-snippet-content").on("click", function() {
        var snippetSelector = configDom.find('#snippet'), evKey = "snippet-" + snippetSelector.val() + "-content-update." + editorTab.id;
        var snippetEditor = app.tabs.content.snippet.prototype.editSnippetContent(snippetSelector.val(), snippetSelector.find("option:selected").text(), "content-editor");
        app.global_event.off(evKey)
        app.global_event.on(evKey, function() {
            var wiObj = editorTab.getWidgetObject(widget)
            var cache = widget.data("data-cache");
            wiObj.render({
                widgetId: widget.attr("widget-id"),
                cache: cache,
                type: widget.attr("widget-type")
            });
            if(!snippetEditor.isClosed) {
                snippetEditor.close()
                editorTab.setActive()
            }
        });
        widget.on("remove", function() {
            app.global_event.off(evKey)
        });
        editorTab.on("close", function() {
            app.global_event.off(evKey)
        })
    });
};