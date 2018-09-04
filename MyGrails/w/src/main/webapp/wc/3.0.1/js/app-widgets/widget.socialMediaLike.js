app.widget.socialMediaLike = function(config) {
    app.widget.socialMediaLike._super.constructor.apply(this, arguments);
}

app.widget.socialMediaLike.config_width = 400

var _s = app.widget.socialMediaLike.inherit(app.widget.base);

_s.updateCacheForShortConfig = function(cache, serialized) {
    cache = JSON.parse(cache)
    var params
    if(cache.params) {
        params = JSON.parse(cache.params)
        $.extend(params, serialized)
    } else {
        params = serialized
    }
    if(!serialized.socialMediaConfig) {
        delete params.socialMediaConfig
    }
    cache.params = JSON.stringify(params)
    return JSON.stringify(cache)
}