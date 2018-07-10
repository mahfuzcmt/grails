app.widget.gallery = function(config) {
    app.widget.gallery._super.constructor.apply(this, arguments);
};

app.widget.gallery.config_width = 715;

var _g = app.widget.gallery.inherit(app.widget.base);

_g.init = function() {
    app.widget.gallery._super.init.call(this);
    var content = this.content;
    content.find("[step]:not([step=1])").hide()
    content.find("button.previous-button").on("click", function() {
        var previous = $(this).attr("previous");
        content.find("[step]").hide()
        content.find("[step=" + previous + "]").show();
    });
    var toolbarSubmit = content.find(".toolbar-btn.save")
    toolbarSubmit.addClass("disabled").hide();
    this.attachFirstViewEvents();
    this.attachSecondViewEvents();
    this.attachThirdViewEvents();
}

_g.attachFirstViewEvents = function() {
    var _self = this, content = this.content, firstView = this.content.find(".gallery-widget-first-view"), secondView = this.content.find(".gallery-widget-second-view");
    firstView.form({
        disable_on_submit: false,
        preSubmit: function() {
            var contentType = content.find("[name=galleryContentType]:checked").val();
            if(secondView.find(".gallery-content-config." + contentType).length) {
                firstView.hide();
                secondView.show();
                return
            }
            firstView.loader()
            secondView.find(".gallery-content-config").remove();
            bm.ajax({
                url: app.baseUrl + "galleryWidget/loadContentConfig",
                dataType: "html",
                data: {
                    contentType: contentType,
                    widgetId: _self.widget.attr("widget-id"),
                    data: _self.widget.data("data-cache")
                },
                success: function (resp) {
                    var contentConfig = $(resp)
                    secondView.prepend(contentConfig)
                    contentConfig.updateUi()
                    bm.autoToggle(contentConfig)
                    _self.afterContentConfigRender(contentType, contentConfig)
                    firstView.loader(false)
                    firstView.hide();
                    secondView.show();
                }
            });
            return false
        }
    });
}

_g.attachSecondViewEvents = function() {
    var _self = this, content = this.content, secondView = this.content.find(".gallery-widget-second-view"), thirdView = content.find(".gallery-widget-third-view"), galleryField = content.find("[name='gallery']");
    secondView.form({
        preSubmit: function() {
            var contentType = content.find("[name=galleryContentType]:checked").val();
            if(_self.beforePassSecondView(secondView) !== false ) {
                secondView.loader()
                bm.ajax({
                    url: app.baseUrl + "galleryWidget/loadSupportedGallery",
                    dataType: "html",
                    data: {
                        contentType: contentType,
                        widgetId: _self.widget.attr("widget-id"),
                        data: _self.widget.data("data-cache")
                    },
                    success: function(resp) {
                        thirdView.find(".gallery-types-container").html(resp)
                        thirdView.find(".gallery-item").click(function () {
                            var item = $(this);
                            item.addClass("selected").siblings().removeClass("selected");
                            galleryField.val(item.attr("gallery-name"))
                        });
                        secondView.hide()
                        thirdView.show();
                        content.find("[name='gallery']").val(thirdView.find(".gallery-item.selected").attr("gallery-name"))
                        secondView.loader(false)
                    }
                })
            }
            return false
        }
    })
};

_g.attachThirdViewEvents = function() {
    var _self = this, content = this.content, toolbarSubmit = content.find(".toolbar-btn.save"), gallery
    var galleryField = content.find("[name='gallery']");
    content.find(".gallery-widget-third-view").form({
        preSubmit: function() {
            gallery = galleryField.val()
            if(gallery) {
                var thirdView = content.find(".gallery-widget-third-view"),
                    lastView = content.find(".gallery-widget-last-view"),
                    galleryConfig = lastView.find(".gallery-config-view");
                thirdView.addClass("updating").loader();
                if(galleryConfig.length && galleryConfig.find("[slider='" + gallery + "']").length) {
                    thirdView.hide()
                    lastView.show()
                    toolbarSubmit.removeClass("disabled").show();
                } else {
                    galleryConfig.remove()
                    bm.ajax({
                        url: app.baseUrl + "widget/loadConfigForSlider",
                        dataType: "html",
                        data: {
                            gallery: gallery,
                            widgetId: _self.widget.attr("widget-id"),
                            data: _self.widget.data("data-cache")
                        },
                        success: function (resp) {
                            lastView.html(resp);
                            toolbarSubmit.removeClass("disabled").show();
                            galleryConfig = lastView.find(".gallery-config-view");
                            galleryConfig.updateUi()
                            galleryConfig.closest("form").form("append", galleryConfig).form("set_submit", galleryConfig.find("[type='submit']"))
                            var manualAdvance = galleryConfig.find(".manual-advance-row");
                            galleryConfig.find("[name=controlNav], [name=directionNav]").on("change", function() {
                                if(content.find("[name=controlNav]").val().bool() || content.find("[name=directionNav]").val().bool()) {
                                    manualAdvance.show();
                                } else {
                                    manualAdvance.hide();
                                }
                            });
                            galleryConfig.find("[name=controlNav]").trigger("change");
                            content.find(".previous").click(function() {
                                galleryConfig.remove()
                                thirdView.show()
                                content.find(".body").scrollTop(0)
                                toolbarSubmit.addClass("disabled").hide();
                            })
                            content.find(".cancel-button").click(function() {
                                _self.popup.close()
                            })
                            thirdView.removeClass("updating").loader(false);
                            thirdView.hide();
                            lastView.show()
                        }
                    })
                }
            } else {
                bm.notify($.i18n.prop("no.gallery.selected"), "warning")
            }
            return false;
        }
    })
};

_g.afterContentConfigRender = function(contentType, contentConfig) {
    if(contentType == "product") {
        bm.initProductSelection(contentConfig, "product", {edit: true, tab: this.editor});
    } else if(contentType == "category") {
        bm.initCategorySelection( contentConfig, "category", {edit: true, tab: this.editor});
    }  else if(contentType == "article") {
        bm.initArticleSelection( contentConfig, "article", {edit: true, tab: this.editor});
    }
};

_g.afterContentChange = function(widget, cache, params) { // third parameter comes from other galleries
    if(typeof cache == "string") {
        cache = JSON.parse(cache)
        params = JSON.parse(cache.params)
    }
    if(params.gallery == "nivoSlider") {
        if(!params.directionNav) {
            params = JSON.parse(widget.elm.find(".config-value-cache").val())
        }
        var _$ = widget.editor.iframeWindow.$
        bm.onReady(_$.prototype, "nivoSlider", {
            ready: function() {
                var nextLabel
                var prevLabel
                bm.ajax({
                    dataType: "html",
                    url: app.baseUrl + "app/siteMessage",
                    data: {code: params.prevText},
                    success: function(resp) {
                        prevLabel = resp;
                        bm.ajax({
                            dataType: "html",
                            url: app.baseUrl + "app/siteMessage",
                            data: {code: params.nextText},
                            success: function (resp) {
                                nextLabel = resp;
                                _$(widget.elm).find(".gallery").nivoSlider({
                                    manualAdvance: (params.directionNav.bool() || params.controlNav.bool()) ? params.manualAdvance.bool() : false,
                                    controlNav: params.controlNav.bool(),
                                    directionNav: params.directionNav.bool(),
                                    controlNavThumbs: params.controlNavThumbs.bool(),
                                    pauseOnHover: params.pauseOnHover.bool(),
                                    animSpeed: +params.animSpeed,
                                    effect: params.effect,
                                    prevText: prevLabel,
                                    nextText: nextLabel,
                                    slice: 20,
                                    pauseTime: params.config_width
                                })
                            }
                        })
                    }
                })
            },
            not: function() {
                var head = widget.editor.iframeWindow.$("head");
                head.append("<script src='" + app.systemResourceUrl + "galleries/nivoSlider/jquery.nivo.slider.pack.js' type='text/javascript'></script>");
                head.append('<link rel="stylesheet" type="text/css" href="' + app.systemResourceUrl + 'galleries/nivoSlider/nivo-slider.css">');
            }
        })
    }
};

_g.beforePassSecondView = function(secondView, contentType) {
    switch (contentType) {
        case "album":
            return !secondView.find("[name=album]").val() ? false : true
            break;

    }
}

_g.updateCacheForShortConfig = function(cache, serialized) {
    cache = JSON.parse(cache)
    var params
    if(cache.params) {
        params = JSON.parse(cache.params)
        $.extend(params, serialized)
    } else {
        params = serialized
    }
    if(!cache.widgetContent || cache.widgetContent.length == 0) {
        cache.widgetContent[0] = {widget: {sid: cache.sid}, type: "album"}
    }
    cache.widgetContent[0].contentId = +params.album
    delete params.album
    cache.params = JSON.stringify(params)
    return JSON.stringify(cache)
}

