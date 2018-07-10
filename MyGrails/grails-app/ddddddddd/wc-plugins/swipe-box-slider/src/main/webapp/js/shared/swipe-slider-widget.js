$(function() {
    var _afterContentChange = app.widget.gallery.prototype.afterContentChange;
    app.widget.gallery.prototype.afterContentChange = function(widget, cache, config) {
        if(typeof cache == "string") {
            cache = JSON.parse(cache)
            config = JSON.parse(cache.params)
        }
        if(config.gallery == "swipeBoxSlider") {
            var _$ = widget.editor.iframeWindow.$
            _$(widget.elm).find("paginator").paginator();
        } else {
            _afterContentChange(widget, cache, config)
        }
    }
})