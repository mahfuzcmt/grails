$(function() {
    var _afterContentChange = app.widget.gallery.prototype.afterContentChange;
    app.widget.gallery.prototype.afterContentChange = function(widget, cache, config) {
        if(typeof cache == "string") {
            cache = JSON.parse(cache)
            config = JSON.parse(cache.params)
        }
        if(config.gallery == "jssorSlider" && widget.elm.find(".container").children().length > 0) {
            var _window = widget.editor.iframeWindow;
            var _$ = _window.$
            var widgetId = widget.elm.attr("widget-id") || "";
            function initJssorSlider() {
                _window = widget.editor.iframeWindow;
                var _jssorApp = _window.jssorApp;
                var options = {
                    $AutoPlay: config.auto_play == "true",
                    $AutoPlayInterval: +config.auto_play_interval,
                    $PauseOnHover: +config.pause_on_hover,
                    $ArrowKeyNavigation: true,
                    $SlideEasing: _window.$JssorEasing$.$EaseOutQuint,
                    $SlideDuration: +config.slide_duration,
                    //$SlideWidth: 600,
                    //$SlideHeight: 300,
                    $DisplayPieces: 1,
                    $UISearchMode: 1,
                    // $PlayOrientation: (config.sliding_effect == "Slide Down" ? 2 : 1),
                    $DragOrientation: 3,
                    $SlideshowOptions: {
                        $Class: _window.$JssorSlideshowRunner$,
                        $Transitions: [_jssorApp.slideshowTransitions[config.sliding_effect]],
                        $TransitionsOrder: 1,
                        $ShowLink: true
                    },
                    $CaptionSliderOptions: {
                        $Class: _window.$JssorCaptionSlider$,
                        $CaptionTransitions: _jssorApp.captionTransitions,
                        $PlayInMode: 1,
                        $PlayOutMode: 3
                    },
                    $ArrowNavigatorOptions: {
                        $Class: _window.$JssorArrowNavigator$,
                        $ChanceToShow: +config.arrow_chance_to_show,
                        $AutoCenter: 2,
                        $Steps: 1
                    },
                    $BulletNavigatorOptions: {
                        $Class: _window.$JssorBulletNavigator$,
                        $ChanceToShow: +config.bullet_chance_to_show,
                        $AutoCenter: 1,
                        $Steps: 1,
                        $Lanes: 1,
                        $SpacingX: 4,
                        $SpacingY: 4,
                        $Orientation: 1
                    }
                };

                var jssorSlider = new _window.$JssorSlider$("jssor-slider-" + widgetId, options);

                //responsive code begin
                if(config.scale_slider == "true") {
                    ScaleSlider();
                    $(_window).bind("load", ScaleSlider);
                    $(_window).bind("resize", ScaleSlider);
                    $(_window).bind("orientationchange", ScaleSlider);
                }
                function ScaleSlider() {
                    var parentWidth = jssorSlider.$Elmt.parentNode.clientWidth;
                    if (parentWidth) {
                        jssorSlider.$ScaleWidth(Math.max(Math.min(parentWidth, 980), 380));
                    } else {
                        _window.setTimeout(ScaleSlider, 30);
                    }
                }
                //responsive code end
            }
            bm.onReady(_window, "jssorApp", {
                ready: function() {
                    initJssorSlider();
                },
                not: function() {
                    var head = _window.$("head");
                    head.append("<script src='" + app.systemResourceUrl + "plugins/jssor-slider/js/slider/jssor.slider.mini.js' type='text/javascript'></script>");
                    head.append("<script src='" + app.systemResourceUrl + "plugins/jssor-slider/js/slider/transitions.js' type='text/javascript'></script>");
                    head.append("<link rel='stylesheet' type='text/css' href='"+ app.systemResourceUrl +"plugins/jssor-slider/css/shared/jssor-slider.css'>");
                }
            })
        } else {
            _afterContentChange(widget, cache, config)
        }
    }
})