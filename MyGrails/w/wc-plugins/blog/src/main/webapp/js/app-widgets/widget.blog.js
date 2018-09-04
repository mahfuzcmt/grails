app.widget.blogPost = function(config) {
    app.widget.blogPost._super.constructor.apply(this, arguments);
}

var _b = app.widget.blogPost.inherit(app.widget.base);

(function() {
    function postSelectionPopup(config) {
        var posts = []
        config.find("input[name='post']").each(function() { posts.push(this.value) })
        bm.editPopup(app.baseUrl + "blogAdmin/selectPosts", $.i18n.prop("select.posts"), "", {post: posts}, {
            width: 950,
            events: {
                content_loaded: function () {
                    var _self = this;
                    var postLeftPanelUrl = app.baseUrl + "blogAdmin/loadPostForMultiSelect";
                    var cncSelector = bm.twoSideSelection(_self, 10, "post", postLeftPanelUrl, {view: false, edit: false, "column-sort": true}, ["post"])
                    var removeSearch = _self.find(".search-form .remove-search");
                    var searchText = _self.find("input.search-text");
                    cncSelector.beforeLoadTableContent = function (params) {
                        var _param = {
                            searchText: searchText.val()
                        }
                        $.extend(params, _param);
                        if(searchText.val()) {
                            removeSearch.show();
                        } else {
                            removeSearch.hide();
                        }
                    }
                    removeSearch.on("click", function() {
                        searchText.val("");
                        cncSelector.reload();
                    });
                    _self.find(".search-form").form({
                        disable_on_submit: false,
                        preSubmit: function() {
                            cncSelector.reload();
                            return false;
                        }
                    })
                }
            },
            beforeSubmit: function(form, settings, popup) {
                config.find("input[name='post']").remove();
                form.find("input:hidden[name]").each(function() {
                    var name = $(this).attr("name");
                    var inp = $("<input type='hidden' name='" + name + "' value='" + $(this).val() + "'>")
                    config.find(".widget-specific-config").append(inp)
                })
                config.find(".widget-specific-config").trigger("change")
                popup.close()
                return false;
            }
        })
    }

    app.widget.blogPost.initShortConfig = function(config) {
        config.find("select[name='selection']").change(function() {
            if(this.value == "custom") {
                $(this).siblings(".sidebar-input").addClass("single-action")
            } else {
                $(this).siblings(".sidebar-input").removeClass("single-action")
            }
        })
        config.find(".post-hierarchy-custom").click(function () {
            postSelectionPopup(config)
        })
    }
})();

_b.updateCacheForShortConfig = function(cache, serialized) {
    return app.widget.blogPost._super.updateCacheForShortConfig.call(this, cache, serialized, "post", "blogPost")
}


_b.afterContentChange = function(widget) {
    var paginator = widget.editor.iframeWindow.$(widget.elm).find("paginator").paginator()
}