app.widget.product = function(config) {
    app.widget.product._super.constructor.apply(this, arguments);
}

var _p = app.widget.product.inherit(app.widget.base);

_p.init = function(form) {
    app.widget.product._super.init.call(this);
    var selector = bm.initProductSelection( this.content, "product", {
        tab: this.editor,
        edit: true
    });
    var namespace = bm.getUUID();
    app.global_event.on("product-create." + namespace + " product-update." + namespace +  " product-delete." + namespace, function() {
        selector.reload();
    })
    form.on("close", function() {
        app.global_event.off("." + namespace)
    })
}

_p.afterContentChange = function(widget) {
    var paginator = widget.editor.iframeWindow.$(widget.elm).find("paginator").paginator()
    paginator.showPages = paginator.showPages = window.innerWidth > 800 ? app.config.number_block_in_paginator_count : app.config.number_block_in_paginator_count_800
}

$(function() {
    app.global_event.on("before-render-product-widget-config", function(evt, params, obj) {
        var filter = obj.settingBlock.find(".widget-specific-config select.filter-by");
        if(filter.length && filter.val() != "none") {
            bm.notify($.i18n.prop("set.filter.by.none.for.config"), "alert");
            params.status = false
        }
    })
})