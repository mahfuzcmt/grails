app.tabs.news = function () {
    this.text = $.i18n.prop("news");
    this.tip = $.i18n.prop("manage.news");
    this.ui_class = "news";
    this.ajax_url = app.baseUrl + "news/loadAppView";
    app.tabs.news._super.constructor.apply(this, arguments);
};

app.ribbons.web_content.push({
    text: $.i18n.prop("news"),
    processor: app.tabs.news,
    ui_class: "news"

});

app.tabs.news.inherit(app.SingleTableTab);

var _n = app.tabs.news.prototype;


_n.sortable = {
    list: {
        "1": "title",
        "3": "newsDate"
    },
    sorted: "1",
    dir: "up"
};

_n.menu_entries = [
    {
        text: $.i18n.prop("edit"),
        ui_class: "edit",
        action: "edit"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "remove",
        action: "remove"
    }

];

_n.onMenuOpen = function(navigator) {
    var menu = this.tabulator.menu;
    var itemList = [
        {
            key: "news.edit",
            class: "edit"
        },
        {
            key: "news.remove",
            class: "remove"
        }
    ];
    app.checkPermission(menu, itemList);
}

_n.onActionClick = function (action, data) {
    switch (action) {
        case "edit" :
            this.createNews(data.id, data.name);
            break;
        case "remove" :
            this.deleteNews(data.id, data.name)
            break;
    }
};
(function () {
    function attachEvents() {
        var _self = this;
        this.body.find(".toolbar .create").on("mousedown",function () {
            _self.createNews();
        })
        this.on_global("news-create", function () {
            _self.reload();
        });
        this.on_global("news-update", function () {
            _self.reload();
        });
    }

    _n.init = function () {
        app.tabs.news._super.init.call(this);
        attachEvents.call(this);
    };
    app.global_event.on('after-ribbon-render', function (e, ribbonBar) {
        if (app.isPermitted("news.view.list")) {
            ribbonBar.enable("news");
        } else {
            ribbonBar.disable("news");
        }
    });
})();

_n.onSelectedActionClick = function (action, selecteds) {
    switch (action) {
        case "remove":
            this.deleteSelectedNewses(selecteds.collect("id"));
            break;
    }
};

_n.createNews = function (id, name) {

    var data = {id: id},
        title = $.i18n.prop("edit.news"),
        _self = this;
    if (typeof id == "undefined") {
        data = {};
        name = "";
        title = $.i18n.prop("create.news");
    }
    this.renderCreatePanel(app.baseUrl + "news/edit", title, name, data, {
        success: function () {
            _self.reload();
            if (id) {
                app.global_event.trigger("news-update", [id]);
            } else {
                app.global_event.trigger("news-create");
            }
        }
    });

}

_n.viewNews = function (id) {
    bm.viewPopup(app.baseUrl + "news/view", {id: id}, {width: 600})
}

_n.deleteNews = function (id, name) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.news", [bm.htmlEncode(name)]), function () {
        bm.ajax({
           url: app.baseUrl + "news/delete",
           data : {id:id},
           success : function() {
               _self.reload();
           }
        });
    }, function () {

    });
}

_n.deleteSelectedNewses = function(selecteds) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.selected.news"), function() {
        bm.ajax({
            url: app.baseUrl + "news/deleteSelectedNewses",
            data: {ids: selecteds},
            success: function () {
                app.global_event.trigger("send-trash", ["news", selecteds])
                _self.reload(true);
                _self.body.find(".action-header").hide();
            }
        })
    }, function () {
    });
}
_n.advanceSearchUrl = app.baseUrl + "news/advanceFilter";
_n.advanceSearchTitle = $.i18n.prop("news");