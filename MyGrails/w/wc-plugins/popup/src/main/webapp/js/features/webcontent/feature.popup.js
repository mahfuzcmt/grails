app.tabs.popup = function () {
    this.text = $.i18n.prop("popup");
    this.tip = $.i18n.prop("manage.popups");
    this.ui_class = "popups";
    this.ajax_url = app.baseUrl + "popupAdmin/loadAppView";
    app.tabs.popup._super.constructor.apply(this, arguments);
};

app.ribbons.web_content.push({
    text: $.i18n.prop("popup"),
    processor: app.tabs.popup,
    ui_class: "popup"
});

app.tabs.popup.inherit(app.SingleTableTab)

var _p = app.tabs.popup.prototype;
_p.advanceSearchUrl = app.baseUrl + "popupAdmin/advanceFilter";
_p.advanceSearchTitle = $.i18n.prop("popup");

_p.sortable = {
    list: {
        "1": "name",
        "3": "url"
    },
    sorted: "1",
    dir: "up"
};

_p.menu_entries = [
    {
        text: $.i18n.prop("edit"),
        ui_class: "edit",
        action: "edit"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "delete",
        action: "delete"
    }
];


_p.onActionClick = function (action, data) {
    switch (action) {
        case "edit":
            this.editPopup(data.id, data.name);
            break;
        case "delete":
            this.deletePopup(data.id, data.name);
            break
    }
};

_p.onSelectedActionClick = function (action, selecteds) {
    switch (action) {
        case "remove":
            this.deleteSelectedPopups(selecteds.collect("id"));
            break;
    }
};

(function () {
    _p.init = function () {
        var _self = this
        app.tabs.popup._super.init.call(this);
        _self.body.find(".toolbar .create").on("click", function() {
            _self.editPopup();
        })
    }
    app.global_event.on('after-ribbon-render', function (e, ribbonBar) {
        if (app.isPermitted("popup.view.list")) {
            ribbonBar.enable("popup");
        } else {
            ribbonBar.disable("popup");
        }
    });
})();

_p.editPopup = function (id, name) {
    var data = {id: id}, _self = this, title = id ? $.i18n.prop("edit.popup") : $.i18n.prop("create.popup");
    this.renderCreatePanel(app.baseUrl + "popupAdmin/infoEdit", title, name, data, {
        success: function () {
           _self.reload()
        }
    });
}

_p.deletePopup = function (id, name) {
    var _self = this;
    bm.remove("popup", "popup", $.i18n.prop("confirm.delete.popup", [name]), app.baseUrl + "popupAdmin/delete", id, {
        success: function () {
            _self.reload();
        }
    });
};

_p.deleteSelectedPopups = function (selecteds) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.selected.popup"), function() {
        bm.ajax({
            url: app.baseUrl + "popupAdmin/deleteSelected",
            data: {ids: selecteds},
            success: function () {
                _self.reload();
            }
        })
    }, function () {})
}