app.widget.rssFeed = function (config) {
    app.widget.rssFeed._super.constructor.apply(this, arguments);
}
var _v = app.widget.rssFeed.inherit(app.widget.base)

_v.updateCacheForShortConfig = function(cache, serialized) {
    cache = JSON.parse(cache)
    var params
    if(cache.params) {
        params = JSON.parse(cache.params)
        $.extend(params, serialized)
    } else {
        params = serialized
    }
    cache.content = params.feed_url
    delete params.feed_url
    cache.params = JSON.stringify(params)
    return JSON.stringify(cache)
}