app.widget = {
    base: function(params) {
        $.extend(this, params);
    }
}

var _w = app.widget.base.prototype;

_w.init = function() {
    var _self = this;
    var configToolIcon
    var slideDown = function() {
        _self.confingPanel.slideDown(function() {
            configToolIcon.removeClass('down').addClass('up');
            $(document).on('mousedown.widget-config', function(ev) {
                if(!_self.confingPanel.isParentOf(ev.target)) {
                    slideUp();
                }
            })
        })
    }
    function slideUp() {
        _self.confingPanel.slideUp(function() {
            configToolIcon.removeClass('up').addClass('down');
        });
        $(document).off('mousedown.widget-config');
    }
    _self.confingPanel = this.content.find(".widget-config-panel").hide().on("invalid", function() {
        if(_self.confingPanel.is(":hidden")) {
            slideDown();
        }
    });
    configToolIcon = _self.content.find(".configure-btn").addClass('down').click(function() {
        if(_self.confingPanel.is(":hidden")) {
            slideDown();
        } else {
            slideUp();
        }
    })
}

_w.getParams = function() {
    if(this.confingPanel) {
        return JSON.stringify(this.confingPanel.serializeObject());
    } else {
        return "";
    }
}

_w.updateCacheForShortConfig = function(cache, serialized, contentParam, contentType) {
    cache = JSON.parse(cache)
    var params
    if(cache.params) {
        params = JSON.parse(cache.params)
        $.extend(params, serialized)
    } else {
        params = serialized
    }
    if(contentParam) {
        var contents = params[contentParam]
        cache.widgetContent = []
        delete params[contentParam]
        if(contents) {
            if(!$.isArray(contents)) {
                contents = [contents]
            }
            contents.every(function(i) {
                cache.widgetContent[i] = {widget: {sid: cache.sid}, type: contentType, contentId: +this}
            })
        }
    }
    cache.params = JSON.stringify(params)
    return JSON.stringify(cache)
}

app.widget.breadcrumb = function() {}
app.widget.breadcrumb.no_config = true