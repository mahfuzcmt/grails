app.widget.youtube = function (config) {
    app.widget.youtube._super.constructor.apply(this, arguments);
}

var _y = app.widget.youtube.inherit(app.widget.base);

(function() {
    _y.showSearchResult = function(result) {
        var _self = this;
        var html
        if(result) {
            _self.mediaUrl.val(result.id.videoId)
            var snippet = result.snippet;
            html = '<div class="thumb"><img src="' + snippet.thumbnails.default.url + '"/></div>' +
                '<div class="title">' + snippet.title + '</div>' +
                '<div class="description">' + snippet.description + '</div>'

        } else {
            _self.mediaUrl.val("")
            html = '<span class="no-result"> ' + $.i18n.prop("no.result.found") + ' </span>'
        }
        _self.content.find(".resultViewer").html(html);
    };

    _y.search = function(search, pageToken) {
        var _self = this;
        _self.nextPageToken = _self.prevPageToken = null
        _self.searchText = search;
        bm.ajax({
            url: "https://www.googleapis.com/youtube/v3/search",
            type: "GET",
            data: {
                key: _self.api_key,
                part: "snippet",
                q: search,
                maxResults: 1,
                type: "video",
                pageToken: pageToken ? pageToken : ""
            },
            success: function(resp) {
                if(resp.prevPageToken) {
                    _self.prevPageToken = resp.prevPageToken
                    _self.leftNav.removeClass("disabled")
                } else {
                    _self.leftNav.addClass("disabled")
                }
                if(resp.nextPageToken) {
                    _self.nextPageToken = resp.nextPageToken
                    _self.rightNav.removeClass("disabled")
                } else {
                    _self.rightNav.addClass("disabled")
                }
                if(resp.items.length) {
                    _self.showSearchResult(resp.items[0])
                } else {
                    this.error()
                }
            },
            error: function() {
                _self.rightNav.addClass("disabled")
                _self.leftNav.addClass("disabled")
                _self.showSearchResult();
            }
        })
    };

    _y.searchInitiator = function (search) {
        var _self = this;
        if ($.trim(search) == "") {
            return;
        }
        if ($.trim(search).indexOf("http") == 0) {
            var groups = /v=([^&]*)/.exec(search);
            if(!groups){
                _self.rightNav.addClass("disabled")
                _self.leftNav.addClass("disabled")
               _self.showSearchResult()
                return;
            }
            search = groups[0]
        }
        _self.search(search)
    }

    _y.init = function () {
        var _self = this, content = _self.content;
        _self.resultViewer = content.find(".resultViewer");
        _self.leftNav = content.find(".navigation.left").on("click", function() {
            var $this = $(this);
            if($this.is(".disabled"))  {
                return;
            }
            _self.search(_self.searchText, _self.prevPageToken)
        });
        _self.rightNav = content.find(".navigation.right").on("click", function() {
            var $this = $(this);
            if($this.is(".disabled"))  {
                return;
            }
            _self.search(_self.searchText, _self.nextPageToken)
        });;

        app.widget.youtube._super.init.call(this);
        var searchField = _self.content.find(".youtube-source-url")
        _self.content.find(".search").on("click", function () {
            _self.searchInitiator(searchField.val());
        });
        searchField.bind("keydown.key_return", function(ev) {
            ev.preventDefault()
            _self.searchInitiator(searchField.val());
        })
        _self.mediaUrl = content.find("[name=mediaUrl]")
        if (_self.mediaUrl.val()) {
            _self.searchInitiator(_self.mediaUrl.val());
        }
    }

    _y.beforeSubmit = function () {
        if (!this.mediaUrl.val()) {
            bm.notify($.i18n.prop("no.video.found"), "error");
            return false;
        }
        return true;
    }

    _y.updateCacheForShortConfig = function(cache, serialized) {
        cache = JSON.parse(cache)
        var params
        if(cache.params) {
            params = JSON.parse(cache.params)
            $.extend(params, serialized)
        } else {
            params = serialized
        }
        cache.content = params.mediaUrl
        delete params.mediaUrl
        cache.params = JSON.stringify(params)
        return JSON.stringify(cache)
    }

    bm.ajax({
        controller: "youtube",
        action: "fetchConfig",
        success: function(resp) {
            app.widget.youtube.prototype.api_key = resp.api_key
        }
    })

})();

