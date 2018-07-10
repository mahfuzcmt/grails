app.widget.navigation = function(config) {
    app.widget.navigation._super.constructor.apply(this, arguments);
}

app.widget.navigation.config_width = 500;

var _w = app.widget.navigation.inherit(app.widget.base);

_w.updateCacheForShortConfig = function(cache, serialized) {
    return app.widget.navigation._super.updateCacheForShortConfig.call(this, cache, serialized, "navigation", "navigation")
}