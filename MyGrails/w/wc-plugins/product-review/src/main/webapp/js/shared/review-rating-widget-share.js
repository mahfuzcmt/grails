$(function() {
    function loadRaty(widget, selector) {
        var _$ = widget.editor.iframeWindow.$;
        bm.onReady( _$.prototype, "raty", {
            ready: function() {
                widget.elm.find(selector).raty({
                    half: true,
                    path: app.systemResourceUrl + "plugins/product-review/images/raty",
                    score: function() {
                        return $(this).attr('score');
                    },
                    readOnly: function() {
                        return $(this).hasClass('read-only')
                    }
                });
            },
            not: function() {
                var head = _$("head");
                head.append("<script src='" + app.systemResourceUrl + "plugins/product-review/js/jquery/jquery.raty.min.js' type='text/javascript'></script>");
            }
        });
    }

    var _afterContentChange = app.widget.product.prototype.afterContentChange;
    app.widget.product.prototype.afterContentChange = function(widget) {
        _afterContentChange.apply(this, arguments);
        loadRaty(widget, ".review-rating")
    };

    bm.onReady(app.widget, "filter", function() {
        var _afterContentChange = app.widget.filter.prototype.afterContentChange;
        app.widget.filter.prototype.afterContentChange = function(widget) {
            if(_afterContentChange) {
                _afterContentChange.apply(this, arguments);
            }
            loadRaty(widget, ".rating")
        }
    });
});

