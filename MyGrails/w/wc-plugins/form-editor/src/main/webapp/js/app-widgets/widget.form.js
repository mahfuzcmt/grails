app.widget.form = function(config) {
    app.widget.form._super.constructor.apply(this, arguments);
}

app.widget.form.config_width = 400;

var _f = app.widget.form.inherit(app.widget.base);

_f.updateCacheForShortConfig = function(a, b) {
    return app.widget.form._super.updateCacheForShortConfig.call(this, a, b, "form", "form")
}