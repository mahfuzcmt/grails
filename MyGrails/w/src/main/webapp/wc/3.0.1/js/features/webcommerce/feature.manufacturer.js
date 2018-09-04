app.tabs.manufacturer = function () {
    this.text = $.i18n.prop("manufacturers");
    this.tip = $.i18n.prop("manage.manufacturers");
    this.ui_class = "manufacturers";
    this.ajax_url = app.baseUrl + "manufacturerAdmin/loadAppView";
    app.tabs.manufacturer._super.constructor.apply(this, arguments);
};

/*app.ribbons.web_commerce.push({
    text: $.i18n.prop("manufacturers"),
    processor: app.tabs.manufacturer,
    ui_class: "manufacturer"
});*/

app.tabs.manufacturer.inherit(app.SingleTableTab);

var _m = app.tabs.manufacturer.prototype;
_m.advanceSearchUrl = app.baseUrl + "manufacturerAdmin/advanceFilter";
_m.advanceSearchTitle = $.i18n.prop("manufacturer");

_m.sortable = {
    list: {
        "1": "name",
        "3": "url"
    },
    sorted: "1",
    dir: "up"
};

_m.menu_entries = [
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

_m.onActionClick = function (action, data) {
    switch (action) {
        case "edit":
            this.editManufacturer(data.id, data.name);
            break;
        case "delete":
            this.deleteManufacturer(data.id, data.name);
            break;
    }
};

_m.onSelectedActionClick = function (action, selecteds) {
    switch (action) {
        case "remove":
            this.deleteSelectedManufacturers(selecteds.collect("id"));
            break;
    }
};

(function () {
    function attachEvents() {
        var _self = this;
        _self.body.find(".toolbar .create").on("click", function () {
            _self.createManufacturer();
        });
        this.on_global("product-update", function () {
            _self.reload();
        });
        this.on_global("manufacturer-restore", function() {
            _self.reload();
        });
        this.on("close", function () {
            app.tabs.manufacturer.tab = null;
        });
        app.global_event.on('default-image-update', function() {
            app.Tab.getTab('tab-manufacturer').reload();
        });
    }

    _m.init = function () {
        app.tabs.manufacturer._super.init.call(this);
        app.tabs.manufacturer.tab = this
        attachEvents.call(this);
    }

    app.global_event.on('after-ribbon-render', function (e, ribbonBar) {
        if (app.isPermitted("manufacturer.view.list")) {
            ribbonBar.enable("manufacturer");
        } else {
            ribbonBar.disable("manufacturer");
        }
    });
})();

_m.editManufacturer = function (id, name, tab) {
    var _self = this;
    var data = {id: id},
        title = $.i18n.prop("edit.manufacturer");
    if (typeof id == "undefined") {
        data = {};
        name = "";
        title = $.i18n.prop("create.manufacturer");
    }
    if(!tab) {
        tab = _self;
    }
    tab.renderCreatePanel(app.baseUrl + "manufacturerAdmin/edit", title, name, data, {
        success: function () {
            if(app.tabs.manufacturer.tab){
                app.tabs.manufacturer.tab.reload();
            }
            if(id){
                app.global_event.trigger("manufacturer-update", [id]);
            }else{
                app.global_event.trigger("manufacturer-create");
            }
        },
        content_loaded: function(form) {
            app.tabs.manufacturer.editContentLoaded(form, id);
        }
    });
}

app.navigation_item_ref_create_func.manufacturer = app.tabs.manufacturer.editManufacturer = _m.createManufacturer = _m.editManufacturer;

app.tabs.manufacturer.editContentLoaded = function(form, id) {
    bm.metaTagEditor(form.find("#bmui-tab-metatag"));
}

app.tabs.manufacturer.viewProducts = function (id) {
    bm.viewPopup(app.baseUrl + "manufacturerAdmin/viewProducts", {id: id}, {width: 500});
}

_m.deleteManufacturer = function (id, name) {
    var _self = this;
    bm.remove("manufacturer", "Manufacturer", $.i18n.prop("confirm.delete.manufacturer", [name]), app.baseUrl + "manufacturerAdmin/delete", id, {
        success: function () {
            _self.reload();
        }
    });
};

_m.deleteSelectedManufacturers = function (selecteds) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.selected.manufacturer"), function () {
        bm.ajax({
            url: app.baseUrl + "manufacturerAdmin/deleteSelected",
            data: {ids: selecteds},
            success: function () {
                _self.reload();
                app.global_event.trigger("send-trash", ["manufacturer", selecteds]);
                _self.body.find(".action-header").hide();
            }
        })
    }, function () {
    })
};


