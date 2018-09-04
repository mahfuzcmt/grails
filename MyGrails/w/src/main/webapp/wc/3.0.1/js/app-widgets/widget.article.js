app.widget.article = function(config) {
    app.widget.article._super.constructor.apply(this, arguments);
}

var _p = app.widget.article.inherit(app.widget.base);

_p.init = function(form) {
    var _self = this;
    app.widget.article._super.init.call(this);
    _self.content.find(".create-article").click(function(){
        app.tabs.content.editArticle();
    })
    var selector = bm.initArticleSelection(_self.content, "article", {
        tab: this.editor,
        edit: true
    });

    var namespace = bm.getUUID();
    app.global_event.on("article-create." + namespace + " article-update." + namespace +  " article-delete." + namespace, function() {
        selector.reload();
    })
    form.on("close", function() {
        app.global_event.off("." + namespace)
    })
}