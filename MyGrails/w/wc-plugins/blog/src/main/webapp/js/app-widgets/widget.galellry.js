bm.onReady(app.widget, "gallery", function() {
    var _g = app.widget.gallery.prototype;
    var _afterContentConfigRender = _g.afterContentConfigRender;
    _g.afterContentConfigRender = function(contentType, contentConfig) {
        _afterContentConfigRender.apply(this, arguments)
        if(contentType == "blogPost") {
            var postSelector = bm.twoSideSelection(contentConfig, 10, "post", app.baseUrl + "blogAdmin/loadBlogPostForSelection", {view: false, edit: false, "column-sort": true}, ["post"])
            postSelector.beforeLoadTableContent = function (params) {
                var _param = {
                    searchText: contentConfig.find("input.search-text").val(),
                }
                $.extend(params, _param);
            }
            contentConfig.find(".filter-block").form({
                disable_on_submit: false,
                preSubmit: function () {
                    postSelector.reload();
                }
            });
        }
    };
});