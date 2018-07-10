$(function () {
    var _afterContentChange = app.widget.gallery.prototype.afterContentChange;
    app.widget.gallery.prototype.afterContentChange = function (widget, cache, config) {
        if (typeof cache == "string") {
            cache = JSON.parse(cache)
            config = JSON.parse(cache.params)
        }
        if (config.gallery == "galleriffic") {
            var _$ = widget.editor.iframeWindow.$
            bm.onReady(_$.prototype, "galleriffic", {
                ready: function () {
                    var uuid = "#wi-" + widget.uuid
                    var gallery = _$(widget.elm).find('.thumbs-container').galleriffic({
                        numThumbs:                parseInt(config.numThumbs),
                        enableTopPager:            config.layout == "rollover",
                        enableBottomPager:         config.layout == "rollover",
                        imageContainerSel:         uuid + ' .slideshow',
                        captionContainerSel:       uuid + ' .caption-container',
                        loadingContainerSel:       uuid + ' .loading',
                        renderSSControls:          false,
                        renderNavControls:         false,
                        nextPageLinkText:          'Next &rsaquo;',
                        prevPageLinkText:          '&lsaquo; Prev',
                        autoStart:                 config.autoStart.bool(),
                        syncTransitions:           true,
                        defaultTransitionDuration: 900
                    });
                },
                not: function () {
                    var head = widget.editor.iframeWindow.$("head");
                    head.append("<script src='" + app.systemResourceUrl + "plugins/gallery-galleriffic/js/galleriffiec/jquery.galleriffic.js'></script>");
                    head.append('<link rel="stylesheet" type="text/css" href="'+ app.systemResourceUrl +'plugins/gallery-galleriffic/css/galleriffiec/galleriffiec.css">');
                }
            })
        } else {
            _afterContentChange(widget, cache, config)
        }
    }
})