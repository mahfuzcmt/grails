app.tabs.brand = function () {
    this.text = $.i18n.prop("brands");
    this.tip = $.i18n.prop("manage.brands");
    this.ui_class = "brands";
    this.ajax_url = app.baseUrl + "brandAdmin/loadAppView";
    app.tabs.brand._super.constructor.apply(this, arguments);
};

// hide brand from web commerce menu
/*app.ribbons.web_commerce.push({
    text: $.i18n.prop("brands"),
    processor: app.tabs.brand,
    ui_class: "brand"
});*/

app.tabs.brand.inherit(app.SingleTableTab)

var _b = app.tabs.brand.prototype;
_b.advanceSearchUrl = app.baseUrl + "brandAdmin/advanceFilter";
_b.advanceSearchTitle = $.i18n.prop("brand");

_b.sortable = {
    list: {
        "1": "name",
        "3": "url"
    },
    sorted: "1",
    dir: "up"
};

_b.menu_entries = [
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

_b.action_menu_entries = [
    {
        text: $.i18n.prop("create.brand"),
        ui_class: "create-brand",
        action: "create-brand"
    }
];

_b.onActionClick = function (action, data) {
    switch (action) {
        case "edit":
            this.editBrand(data.id, data.name);
            break;
        case "delete":
            this.deleteBrand(data.id, data.name);
            break
    }
};

_b.onSelectedActionClick = function (action, selecteds) {
    switch (action) {
        case "remove":
            this.deleteSelectedBrands(selecteds.collect("id"));
            break;
    }
};

(function () {
    function attachEvents() {
        var _self = this;
        this.on_global("product-updated", function (evt) {
            _self.reload();
        });
        this.on_global("brand-restore", function() {
            _self.reload();
        });
        this.on("close", function () {
            app.tabs.brand.tab = null;
        });
        app.global_event.on('default-image-update', function() {
            app.Tab.getTab('tab-brand').reload();
        });
        _self.body.find(".toolbar .create").on("click", function() {
            _self.createBrand();
        })
    }

    _b.init = function () {
        app.tabs.brand._super.init.call(this);
        app.tabs.brand.tab = this
        attachEvents.call(this);
    }
    app.global_event.on('after-ribbon-render', function (e, ribbonBar) {
        if (app.isPermitted("brand.view.list")) {
            ribbonBar.enable("brand");
        } else {
            ribbonBar.disable("brand");
        }
    });
})();

_b.editBrand = function (id, name, tab) {
    var data = {id: id},
        title = $.i18n.prop("edit.brand"),
        _self = this;
    if (typeof id == "undefined") {
        data = {};
        name = "";
        title = $.i18n.prop("create.brand");
    }
    if(!tab) {
        tab = _self;
    }
    tab.renderCreatePanel(app.baseUrl + "brandAdmin/edit", title, name, data, {
        success: function () {
            if(app.tabs.brand.tab){
                app.tabs.brand.tab.reload()
            }
            if(id){
                app.global_event.trigger("brand-update", [id]);
            }else{
                app.global_event.trigger("brand-create");
            }
        },
        content_loaded: function(form) {
            app.tabs.brand.editContentLoaded(form, id);
        }
    });
}

app.navigation_item_ref_create_func.brand = app.tabs.brand.editBrand = _b.createBrand = _b.editBrand;

app.tabs.brand.editContentLoaded = function(form, id) {
    bm.metaTagEditor(form.find("#bmui-tab-metatag"));
}

_b.deleteBrand = function (id, name) {
    var _self = this;
    bm.remove("brand", "Brand", $.i18n.prop("confirm.delete.brand", [name]), app.baseUrl + "brandAdmin/delete", id, {
        success: function () {
            _self.reload();
        }
    });
};

app.tabs.brand.viewProducts = function (id) {
    bm.viewPopup(app.baseUrl + "brandAdmin/viewProducts", {id: id}, {width: 550});
}

_b.deleteSelectedBrands = function (selecteds) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.selected.brand"), function() {
        bm.ajax({
            url: app.baseUrl + "brandAdmin/deleteSelected",
            data: {ids: selecteds},
            success: function () {
                _self.reload();
                app.global_event.trigger("send-trash", ["page", selecteds]);
                _self.body.find(".action-header").hide();
            }
        })
    }, function () {
    })
}