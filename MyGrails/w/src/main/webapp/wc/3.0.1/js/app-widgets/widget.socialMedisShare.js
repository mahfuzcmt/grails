app.widget.socialMediaShare = function(config) {
    app.widget.socialMediaShare._super.constructor.apply(this, arguments);
}

var _s = app.widget.socialMediaShare.inherit(app.widget.base);

_s.afterContentChange = function(widget) {
    bm.onReady(widget.editor.iframeWindow, "addthis", {
        ready: function() {
            var as = widget.elm.find(".social-media-share a")
            if(as.length) {
                var addThis = widget.editor.iframeWindow.addthis
                addThis.toolbox("#" + widget.elm.attr("id"))
            }
        },
        not: function() {
            var head = widget.editor.iframeWindow.$("head");
            head.append('<script type="text/javascript" src="//s7.addthis.com/js/300/addthis_widget.js"></script>');
        }
    })
}