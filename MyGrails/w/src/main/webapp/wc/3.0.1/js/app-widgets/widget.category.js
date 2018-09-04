app.widget.category = function(config) {
    app.widget.category._super.constructor.apply(this, arguments);
}

var _c = app.widget.category.inherit(app.widget.base);

_c.init = function(form) {
    app.widget.category._super.init.call(this);
    var selector = bm.initCategorySelection( this.content, "category", {
        tab: this.editor,
        edit: true
    });
    var namespace = bm.getUUID();
    app.global_event.on("category-create." + namespace + " category-update." + namespace +  " category-delete." + namespace, function() {
        selector.reload();
    })
    form.on("close", function() {
        app.global_event.off("." + namespace)
    })
}

_c.afterContentChange = function(widget) {
    var paginator = widget.editor.iframeWindow.$(widget.elm).find("paginator").paginator()
    paginator.showPages = paginator.showPages = window.innerWidth > 800 ? app.config.number_block_in_paginator_count : app.config.number_block_in_paginator_count_800
}