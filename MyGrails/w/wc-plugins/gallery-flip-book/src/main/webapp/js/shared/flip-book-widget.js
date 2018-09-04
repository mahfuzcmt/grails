/**
 * Created by sajed on 3/19/14.
 */
$(function() {
    var _afterContentChange = app.widget.gallery.prototype.afterContentChange;
    app.widget.gallery.prototype.afterContentChange = function(widget, cache, config) {
        if(typeof cache == "string") {
            cache = JSON.parse(cache)
            config = JSON.parse(cache.params)
        }
        if(config.gallery == "flip_book") {
            var _$ = widget.editor.iframeWindow.$
            function initFlipBook() {
                var options = {
                    width: config.width - 44,
                    height: +config.height,
                    elevation: 50,
                    inclination: 500,
                    gradients: config.showGradients || false,
                    autoCenter: config.autoCenter || false,
                    acceleration: true,
                    direction: config.flippingDirection,
                    page: +config.startPage || 1,
                    display: config.displayPage,
                    duration: +config.transDuration || 100
                };
                var flipbook = _$(widget.elm).find(".flipbookItem");
                flipbook.find(".flipbook-page").show();
                flipbook.turn(options).turn("peel", "tr");
                flipbook.find('.navigation-button').css('height', config.height);
                if(config.thumbnails == 'true') {
                    var thumbBlock = flipbook.closest(".flipbook-canvas").find(".thumb-container");
                    thumbBlock.find(".thumb-display-container").scrollbar({
                        show_vertical: false,
                        show_horizontal: true,
                        use_bar: false,
                        visible_on: "auto",
                        horizontal: {
                            handle: {
                                left: thumbBlock.find(".navigator.left-navigator"),
                                right: thumbBlock.find(".navigator.right-navigator")
                            }
                        }
                    })
                }
            }
            bm.onReady(_$.prototype, "turn", {
                ready: function() {
                    initFlipBook();
                },
                not: function() {
                    var head = widget.editor.iframeWindow.$("head");
                    head.append("<link href='" + app.systemResourceUrl + "plugins/gallery-flip-book/css/flipbook.css' type='text/css'>");
                    head.append("<script src='" + app.systemResourceUrl + "plugins/gallery-flip-book/js/turnjs/turn.min.js' type='text/javascript'></script>");
                }
            })
        } else {
            _afterContentChange(widget, cache, config)
        }
    }
})