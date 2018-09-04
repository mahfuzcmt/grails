
app.widget.filter = function(config) {
    app.widget.filter._super.constructor.apply(this, arguments);
}

app.widget.filter.config_width = 400;
var _f = app.widget.filter.inherit(app.widget.base);

_f.updateCacheForShortConfig = function(cache, serialized) {
    cache = JSON.parse(cache)
    var params
    if(cache.params) {
        params = JSON.parse(cache.params)
        $.extend(params, serialized)
    } else {
        params = serialized
    }
    if(!serialized.filterConfig) {
        delete params.filterConfig
    }
    cache.params = JSON.stringify(params)
    return JSON.stringify(cache)
}

app.widget.shopByFilterGroup = function(config) {
    app.widget.shopByFilterGroup._super.constructor.apply(this, arguments);
}

var _sb = app.widget.shopByFilterGroup.inherit(app.widget.base);

app.widget.shopByFilterGroup.config_width = 400;
(function(){
    _sb.init = function() {
        app.widget.shopByFilterGroup._super.init.call(this);

    }
})();
