app.tabs.gift_wrapper = function () {
    this.text = $.i18n.prop("gift.wrapper");
    this.tip = $.i18n.prop("manage.gift.wrapper");
    this.ui_class = "gift-wrapper-tab";
    this.ajax_url = app.baseUrl + "giftWrapperAdmin/loadAppView";
    app.tabs.gift_wrapper._super.constructor.apply(this, arguments);
};

app.ribbons.web_commerce.push({
    text: $.i18n.prop("gift.wrapper"),
    processor: app.tabs.gift_wrapper,
    ui_class: "gift-wrapper-tab",
    license: "allow_gift_wrapper_feature"
});

app.tabs.gift_wrapper.inherit(app.SingleTableTab);
var _f = app.tabs.gift_wrapper.prototype;

_f.menu_entries = [
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


_f.onSelectedActionClick = function (action, selecteds) {
    switch (action) {
        case "remove":
            this.deleteSelectedGiftWrappers(selecteds.collect("id"));
            break;
    }
};

_f.onActionClick = function (action, data) {
    switch (action) {
        case "edit" :
            this.editGiftWrapper(data.id, data.name);
            break;
        case "delete":
            this.deleteGiftWrapper(data.id, data.name);
            break;
    }
};

function attachEvents() {
    var _self = this;
    _self.body.find(".toolbar .create").on("click", function () {
        _self.createGiftWrapper();
    });
    this.on_global("giftWrapper-restore", function () {
        _self.reload();
    });
    this.on("close", function () {
        app.tabs.gift_wrapper.tab = null;
    });
}


_f.deleteSelectedGiftWrappers = function (selecteds) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.selected.gift.wrappers"), function () {
        bm.ajax({
            url: app.baseUrl + "giftWrapperAdmin/deleteSelected",
            data: {ids: selecteds},
            success: function () {
                _self.reload();
                _self.body.find(".action-header").hide();
            }
        })
    }, function () {
    })
};


_f.deleteGiftWrapper = function (id, name) {
    var _self = this;
    bm.remove("giftWrapper", "GiftWrapper", $.i18n.prop("confirm.delete.gift.wrapper", [name]), app.baseUrl + "giftWrapperAdmin/delete", id, {
        success: function () {
            _self.reload();
        }
    });
};

_f.editGiftWrapper = function (id, name) {
    var _self = this;
    var data = {id: id},
        title = $.i18n.prop("edit.gift.wrapper");
    if (typeof id == "undefined") {
        data = {};
        name = "";
        title = $.i18n.prop("create.gift.wrapper");
    }
    _self.renderCreatePanel(app.baseUrl + "giftWrapperAdmin/edit", title, name, data, {
        success: function () {
            _self.reload();
            if (id) {
                app.global_event.trigger("gift-wrapper-update", [id]);
            } else {
                app.global_event.trigger("gift-wrapper-create");
            }
        }
    });
}

app.tabs.gift_wrapper.editGiftWrapper = _f.createGiftWrapper = _f.editGiftWrapper;

_f.init = function () {
    var _self = this;
    app.tabs.gift_wrapper._super.init.call(_self);
    attachEvents.call(this);
};


