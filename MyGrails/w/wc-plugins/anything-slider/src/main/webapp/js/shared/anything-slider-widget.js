$(function() {
    var _afterContentChange = app.widget.gallery.prototype.afterContentChange;
    app.widget.gallery.prototype.afterContentChange = function(widget, cache, config) {
        if(typeof cache == "string") {
            cache = JSON.parse(cache)
            config = JSON.parse(cache.params)
        }
        if(config.gallery == "anythingSlider") {
            var iframeWindow = widget.editor.iframeWindow, _$ = iframeWindow.$
            function initAnythingSlider() {
                var slider = _$(widget.elm[0]).find(".anything-slider")
                var sliderWraper = slider.parent("div")
                config.album = slider.attr("album")
                var images = []
                slider.find("img").each(function() {images.push($(this).attr("name"))})
                sliderWraper.css({width: slider.closest('.widget-gallery').parent().width(), minHeight: +config.height})
                slider.anythingSlider({
                    expand: true,
                    aspectRatio: false,
                    theme: config.theme,
                    mode: config.mode,
                    showMultiple: config.show_multiple == "true" && config.mode == "h" ? +config.slide_at_once : false,
                    easing: config.transition_effect,
                    buildArrows: config.build_arrows == "true",
                    buildNavigation: config.build_navigation == "true",
                    buildStartStop: config.build_start_stop == "true",
                    toggleArrows: config.toggle_arrows == "true",
                    toggleControls: config.toggle_controls == "true",
                    startText: config.start_text,
                    stopText: config.stop_text,
                    forwardText: "&raquo;",
                    backText: "&laquo;",
                    enableNavigation: config.build_navigation == "true",
                    enableKeyboard: config.enable_keyboard == "true",
                    startPanel: +config.start_panel,
                    hashTags: false,
                    infiniteSlides: config.infinite_slides == "true",
                    navigationFormatter: function (index) {
                        return config.show_thumbnails == "true" ? '<img src="' + app.baseUrl + 'resources/albums/album-' + config.album + '/thumb-' + images[index - 1] + '">' : images[index - 1]
                    },
                    navigationSize: config.build_navigation == "true" ? parseInt(config.navigation_size) : false,
                    autoPlay: config.auto_play == "true",
                    pauseOnHover: config.pause_on_hover == "true",
                    playRtl: config.play_rtl == "true",
                    delay: +config.delay,
                    onInitialized: function (e, slider) {
                        $(slider.$items).find('div[class*=caption]').css({ position: 'absolute' })
                    },
                    onSlideComplete: function (slider) {
                        showCaptions($(slider.$currentPage))
                    },
                    onSlideInit: function (e, slider) {
                        hideCaptions($(slider.$currentPage))
                    },
                    onSlideBegin: function (e, slider) {
                        slider.navWindow(slider.$targetPage)
                    }
                })

                function showCaptions(el) {
                    if(config.show_caption != "true") {
                        return
                    }
                    var $this = el;
                    if ($this.find('.caption-bottom').length) {
                        $this.find('.caption-bottom').show().animate({ bottom: 0, opacity: .5 }, 400)
                    }
                };
                function hideCaptions(el) {
                    if(config.show_caption != "true") {
                        return
                    }
                    var $this = el;
                    if ($this.find('.caption-bottom').length) {
                        $this.find('.caption-bottom').stop().animate({ bottom: -100, opacity: 0 }, 400, function () {
                            $this.find('.caption-bottom').hide()
                        })
                    }
                };
                setTimeout(function() {
                    hideCaptions(slider.find('.panel'))
                }, +config.delay)

                slider.find(".panel").show()
            }
            bm.onReady(_$.prototype, "anythingSlider", {
                ready: function() {
                    initAnythingSlider()
                },
                not: function() {
                    var slider = _$(widget).find(".anything-slider")
                    var head = widget.editor.iframeWindow.$("head")
                    var THEME = ['minimalist-square', 'minimalist-round', 'metallic', 'cs-portfolio', 'construction']
                    iframeWindow.bm.addStyle('plugins/anything-slider/css/anythingslider.css')
                    THEME.every(function() {
                        iframeWindow.bm.addStyle('plugins/anything-slider/css/themes/theme-' + this + '.css')
                    });
                    iframeWindow.bm.addScript("plugins/anything-slider/js/easing/jquery.easing.1.3.js")
                    iframeWindow.bm.addScript("plugins/anything-slider/js/slider/jquery.anythingslider.min.js")
                }
            })
        } else {
            _afterContentChange(widget, cache, config)
        }
    }
})