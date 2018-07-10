app.tabs.currency = function () {
    this.text = $.i18n.prop("currencies");
    this.tip = $.i18n.prop("manage.currencies");
    this.ui_class = "currencies";
    this.ajax_url = app.baseUrl + "currencyAdmin/loadAppView";
    app.tabs.currency._super.constructor.apply(this, arguments);
};

app.ribbons.web_commerce.push({
    text: $.i18n.prop("currency"),
    processor: app.tabs.currency,
    ui_class: "currency",
    ecommerce: true
});

app.tabs.currency.inherit(app.SingleTableTab);

var _c = app.tabs.currency.prototype;

_c.sortable = {
    list: {
        "1": "name",
        "2": "code"
    },
    sorted: "1",
    dir: "up"
}

_c.menu_entries = [
    {
        text: $.i18n.prop("edit"),
        ui_class: "edit",
        action: "edit"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "delete",
        action: "delete"
    },
    {
        text: $.i18n.prop("set.base.currency"),
        ui_class: "set-base-currency",
        action: "set-base-currency"
    }
];

_c.onMenuOpen = function (navigator) {
    if (navigator.is("tr.base-currency span")) {
        this.tabulator.menu.find(".menu-item.set-base-currency").addClass("disabled");
        this.tabulator.menu.find(".menu-item.delete").addClass("disabled")
    } else {
        this.tabulator.menu.find(".menu-item.set-base-currency").removeClass("disabled");
        this.tabulator.menu.find(".menu-item.delete").removeClass("disabled")
    }
    if(navigator.is("tr.inactive span")) {
        this.tabulator.menu.find(".menu-item.set-base-currency").addClass("disabled");
    }
};

_c.onActionClick = function (action, data) {
    var _self = this
    switch (action) {
        case "edit":
            _self.editCurrency(data.id, data.name);
            break;
        case "delete":
            this.deleteCurrency(data.id, data.name);
            break;
        case "set-base-currency":
            this.setBaseCurrency(data.id, data.name);
            break;
    }
};

(function () {
    function attachEvents() {
        var _self = this;

        function reload() {
            _self.reload();
        }

        _self.body.find(".toolbar .create").on("mousedown", function () {
            _self.editCurrency()
        })
        this.on_global("currency-update", reload);
        this.on_global("currency-create", reload);
        this.on_global("currency-delete", reload);
        this.on("close", function () {
            app.tabs.currency.tab = null;
        })
    }

    _c.init = function () {
        app.tabs.currency._super.init.call(this);
        app.tabs.currency.tab = this;
        attachEvents.call(this);
    }
    app.global_event.on('after-ribbon-render', function (e, ribbonBar) {
        if (app.isPermitted("currency.view.list")) {
            ribbonBar.enable("currency");
        } else {
            ribbonBar.disable("currency");
        }
    });
})();

_c.editCurrency = function (id, name) {
    var data = {id: id},
        title = id ? $.i18n.prop("edit.currency") : $.i18n.prop("create.currency"),
        name = id ? name : "";
    this.renderCreatePanel(app.baseUrl + "currencyAdmin/edit", title, name, data, {
        success: function () {
            if (id) {
                app.global_event.trigger("currency-update", [id])
            } else {
                app.global_event.trigger("currency-create");
            }
        }
    })
};

_c.deleteCurrency = function (id, name) {
    var _self = this;
    bm.remove("currency", "Currency", $.i18n.prop("confirm.delete.currency", [name]), app.baseUrl + "currencyAdmin/delete", id, {
        success: function () {
            app.global_event.trigger("currency-delete", [id]);
        }
    });
}

_c.onSelectedActionClick = function (action, selecteds) {
    switch (action) {
        case "remove":
            this.deleteSelectedCurrencies(selecteds.collect("id"));
            break;
    }
};

_c.deleteSelectedCurrencies = function (ids) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.selected.currencies"), function () {
        bm.ajax({
            url: app.baseUrl + "currencyAdmin/delete",
            data: {id: ids},
            success: function () {
                app.global_event.trigger("currency-delete", ids);
                _self.body.find(".action-header").hide();
            }
        })
    }, function () {
    });
}

_c.setBaseCurrency = function (id, name) {
    var _self = this;
    bm.ajax({
        url: app.baseUrl + "currencyAdmin/setBaseCurrency",
        data: {id: id, name: name},
        success: function () {
            app.global_event.trigger("currency-update", [id]);
        }
    })
}

