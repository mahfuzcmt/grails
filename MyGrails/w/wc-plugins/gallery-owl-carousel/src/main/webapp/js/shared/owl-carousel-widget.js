/**
 * Created by sajed on 3/19/14.
 */
$(function () {
    var _afterContentChange = app.widget.gallery.prototype.afterContentChange;
    app.widget.gallery.prototype.afterContentChange = function (widget, cache, config) {
        if (typeof cache == "string") {
            cache = JSON.parse(cache)
            config = JSON.parse(cache.params)
        }
        if (config.gallery == "owlCarousel") {
            var _$ = widget.editor.iframeWindow.$

            function initFlipBook() {
                var responsive = {}
                if(config.responsive.bool()) {
                    if(config.items_mobile == "true") {
                        responsive[config.items_mobile_max_width] =  { items: config.items_mobile_no_of_item}
                    }
                    if(config.items_tablet_small == "true") {
                        responsive[config.items_tablet_small_max_width] =  { items: config.items_tablet_small_no_of_item}
                    }
                    if(config.items_tablet == "ture") {
                        responsive[config.items_tablet_max_width] =  { items: config.items_tablet_no_of_item}
                    }
                    if(config.items_desktop_small == "true") {
                        responsive[config.items_desktop_small_max_width] =  { items: config.items_desktop_small_no_of_item}
                    }
                    if(config.items_desktop == "true") {
                        responsive[config.items_desktop_max_width] =  { items: config.items_desktop_no_of_item}
                    }
                } else {
                    responsive = false
                }
                _$(widget.elm).find("#owl-carousel-" + widget.uuid).owlCarousel({
                    items: config.items,
                    loop: true,
                    margin: (config.margin ? (+config.margin) : 0),
                    responsive: responsive,
                    autoplay: config.auto_play.bool(),
                    autoplayTimeout: (config.autoplayTimeout ? (+config.autoplayTimeout) : 1000),
                    autoplayHoverPause: config.stop_on_over.bool(),

                    nav: config.navigation.bool(),
                    navText: [config.pre_button_text, config.next_button_text],

                    dots: config.pagination.bool(),
                    dotsEach: config.pagination_numbers.bool(),
                    dotsSpeed: +config.pagination_speed,

                    responsiveRefreshRate: +config.responsive_refresh_rate,
                    lazyLoad: config.lazy_load.bool(),
                })
            }

            bm.onReady(_$.prototype, "owlCarousel", {
                ready: function () {
                    initFlipBook();
                },
                not: function () {
                    var head = widget.editor.iframeWindow.$("head");
                    head.append("<script src='" + app.systemResourceUrl + "plugins/gallery-owl-carousel/js/owl-carousel/owl.carousel.min.js' type='text/javascript'></script>");
                    head.append('<link rel="stylesheet" type="text/css" href="'+ app.systemResourceUrl +'plugins/gallery-owl-carousel/css/owl-carousel/owl.carousel.css">');
                    head.append('<link rel="stylesheet" type="text/css" href="'+ app.systemResourceUrl +'plugins/gallery-owl-carousel/css/owl-carousel/owl.theme.css">');
                }
            })
        } else {
            _afterContentChange(widget, cache, config)
        }
    }
})