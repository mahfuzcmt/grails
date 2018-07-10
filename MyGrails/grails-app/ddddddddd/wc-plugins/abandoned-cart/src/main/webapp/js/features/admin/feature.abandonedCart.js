app.tabs.abandonedCart = function (configs) {
    this.text = $.i18n.prop("abandoned.cart");
    this.tip = $.i18n.prop("manage.abandoned.cart");
    this.ui_class = "abandoned-cart";
    this.ajax_url = app.baseUrl + "abandonedCartAdmin/loadAppView";
    app.tabs.abandonedCart._super.constructor.apply(this, arguments);
};

app.ribbons.report.push({
    text: $.i18n.prop("abandoned.cart"),
    processor: app.tabs.abandonedCart,
    ui_class: "abandoned-cart",
    ecommerce: true
});

app.tabs.abandonedCart.inherit(app.SingleTableTab);

var _ac = app.tabs.abandonedCart.prototype;
_ac.advanceSearchUrl = app.baseUrl + "abandonedCartAdmin/advanceFilter";

(function () {
    function attachEvent() {
        var _self = this;
        _self.on_global("customer-delete", function() {
            _self.reload();
        });
    }

    _ac.init = function () {
        app.tabs.abandonedCart._super.init.call(this);
        attachEvent.call(this);
    };

})();

_ac.sortable = {
    list: {
        "1": "id"
    },
    sorted: "1",
    dir: "down"
};

_ac.menu_entries = [
    {
        text: $.i18n.prop("send.notification"),
        ui_class: "send-notification",
        action: "send-notification"
    },
    {
        text: $.i18n.prop("disable.notification"),
        ui_class: "notification-status",
        action: "notification"
    },
    {
        text: $.i18n.prop("view.cart"),
        ui_class: "view",
        action: "view-cart"
    }
];

_ac.onMenuOpen = function(navigator, config) {
    var menu = this.tabulator.menu
    var config = navigator.config("entity");
    var notify = menu.find(".notification-status");
    if(config.notification == "disabled") {
        notify.removeClass("disable-notify").addClass("enable-notify");
        notify.find(".label").html($.i18n.prop("enable.notification"));
    } else {
        notify.removeClass("enable-notify").addClass("disable-notify");
        notify.find(".label").html($.i18n.prop("disable.notification"));
    }
}

_ac.onActionClick = function (action, data) {
    switch (action) {
        case "send-notification":
            this.sendNotification(data.id)
            break;
        case "notification":
            this.disableNotification(data.id, data.notification);
            break
        case "view-cart":
            this.view(data.id);
            break
    }
};

_ac.onSelectedActionClick = function (action, selecteds) {
    switch (action) {
        case "send_notification":
            this.sendBatchNotification(selecteds.collect("id"));
            break;
    }
};

_ac.sendNotification = function(id) {
    var _self = this;
    bm.ajax({
        url: app.baseUrl + "abandonedCartAdmin/sendNotification",
        data: {id: id, force: true},
        success: function() {
            _self.reload();
        }
    })
}

_ac.sendBatchNotification = function(ids) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.send.notification", [ids.length]), function() {
        bm.ajax({
            url: app.baseUrl + "abandonedCartAdmin/sendBatchNotification",
            data: {ids: ids, force: true},
            success: function() {
                _self.reload();
            }
        })
    }, function () {
    })
}

_ac.disableNotification = function(id, notification) {
    var _self = this;
    var data = {id: id};
    if(notification != "disabled") {
        data.disable = true
    }
    bm.ajax({
        url: app.baseUrl + "abandonedCartAdmin/disableNotification",
        data: data,
        success: function() {
            _self.reload();
        }
    })
}

_ac.view = function (id) {
    bm.viewPopup(app.baseUrl + "abandonedCartAdmin/viewCart", {id: id}, {width: 700, clazz: "view-popup abandoned-cart-view"});
}