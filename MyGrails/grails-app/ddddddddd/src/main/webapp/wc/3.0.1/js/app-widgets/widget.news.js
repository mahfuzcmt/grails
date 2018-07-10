app.widget.news = function (config) {
    app.widget.news._super.constructor.apply(this, arguments);
}

var _n = app.widget.news.inherit(app.widget.base);

app.widget.news.config_width = 440;

app.widget.news.initShortConfig = function(container) {
    container.find("[name='transition_speed']").slider({
        from: 5,
        to: 30,
        definition: 's',
        step: 1
    });
    container.find("[name='transition_speed']").parent().on("click", ".jslider-pointer:not(.jslider-pointer-to)", function () {
        $(this).closest(".widget-specific-config").trigger("change")
    })
};

_n.afterContentChange = function(widget) {
    var _$ = widget.editor.iframeWindow.$
    bm.onReady(_$.prototype, "newsticker", {
        ready: function() {
            _$(widget.elm).find(".news-list").newsticker();
        },
        not: function() {
            var head = _$("head");
            head.append(`<script src="${app.systemResourceUrl}plugins/news/js/ui-widgets/newsticker.js" type='text/javascript'></script>`);
        }
    })
}
