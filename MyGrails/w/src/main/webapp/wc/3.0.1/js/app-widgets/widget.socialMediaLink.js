app.widget.socialMediaLink = function(config) {
    app.widget.socialMediaLink._super.constructor.apply(this, arguments);
}

var _l = app.widget.socialMediaLink.inherit(app.widget.base);

_l.updateCacheForShortConfig = function(cache, serialized) {
    cache = JSON.parse(cache)
    var params
    if(cache.params) {
        params = JSON.parse(cache.params)
        $.extend(params, serialized)
    } else {
        params = serialized
    }

    if(!$.isArray(params.socialProfileName)) {
        params.socialProfileName = [params.socialProfileName]
        params.socialProfileLink = [params.socialProfileLink]
    }

    var selecteds = {};
    params.socialProfileName.every(function(k, v) {
        if (params.socialProfileLink[k]) {
            selecteds[v] = params.socialProfileLink[k]
        }
    })
    params.socialMediaConfig = selecteds
    delete params.socialProfileName
    delete params.socialProfileLink
    cache.params = JSON.stringify(params)
    return JSON.stringify(cache)
}