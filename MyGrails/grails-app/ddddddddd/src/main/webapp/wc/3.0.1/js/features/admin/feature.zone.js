app.tabs.zone = function(configs) {
    this.text = $.i18n.prop("zone");
    this.tip = $.i18n.prop("manage.zone");
    this.ui_class = "zone";
    this.ajax_url = app.baseUrl + "zone/loadAppView";
    app.tabs.zone._super.constructor.apply(this, arguments);
}

app.ribbons.administration.push({
    text: $.i18n.prop("zone"),
    processor: app.tabs.zone,
    ui_class: "zone"
})

app.tabs.zone.inherit(app.SingleTableTab);

var _z = app.tabs.zone.prototype;
_z.advanceSearchUrl = app.baseUrl + "zone/advanceFilter";
_z.advanceSearchTitle = $.i18n.prop("zone");
_z.advance_search_popup_width = 450;
_z.menu_entries = [
    {
        text: $.i18n.prop("edit"),
        ui_class: "edit"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "remove"
    }
];
_z.sortable = {
    list: {
        "1": "name"
    },
    sorted: "1",
    dir: "up"
};

(function(){
    function attachEvent() {
        var _self = this;
        this.body.find(".toolbar .create").on("click", function() {
            _self.createZone();
        });
    }

    _z.init = function () {
        app.tabs.zone._super.init.call(this);
        attachEvent.call(this);
    };
    app.global_event.on('after-ribbon-render', function (e, ribbonBar) {
        if (app.isPermitted("zone.view.list")) {
            ribbonBar.enable("zone");
        } else {
            ribbonBar.disable("zone");
        }
    });
})();

_z.onActionClick = function (action, data) {
    var _self = this;
    switch (action) {
        case "edit":
            this.editZone(data.id, data.name)
            break;
        case "remove":
            app.tabs.zone.deleteZone(data.id, data.name, _self)
            break;
    }
};
_z.onSelectedActionClick = function (action, selecteds) {
    switch (action) {
        case  "remove":
            this.deleteSelectedZone(selecteds.collect("id"));
            break;
    }
};
_z.onMenuOpen = function (navigator, menu) {
    var isSystemGenerated = navigator.attr('entity-system-generated');
    if(isSystemGenerated == "true") {
        menu.find('.menu-item.remove').addClass('disabled');
    } else {
        menu.find('.menu-item.remove').removeClass('disabled');
    }
};

_z.createZone = function() {
    var _self = this;
    this.renderCreatePanel(app.baseUrl + "zone/create", $.i18n.prop("create.zone"), "", {}, {
        content_loaded: function (popup) {
            bm.countryChangeSelection(popup.find("select[name=zone\\.country\\.id]"), true, {isMultiple: "true", stateName: 'zone.state.id'});
        },
        success: function () {
            _self.reload();
        }
    });
}

app.tabs.zone.deleteZone = function(id, name, _self, callback) {
    bm.remove("zone", "Zone", $.i18n.prop("confirm.delete.zone", [name]), app.baseUrl + "zone/delete", id, {
        is_final: true,
        success: function() {
            if(_self) {
                _self.reload();
            } else {
                callback();
            }
        }
    })
}

_z.deleteSelectedZone = function(selecteds) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.selected.zone"), function () {
        bm.ajax({
            url: app.baseUrl + "zone/deleteSelected",
            data: {ids: selecteds},
            success: function () {
                _self.reload()
            }
        })
    }, function () {
    });
}

_z.viewTaxShippingRuleAndPaymentGateway = function(id) {
    bm.viewPopup(app.baseUrl + "zone/viewTaxShippingRuleAndPaymentGateway", {id: id}, {});
}

_z.editZone = function(id, name) {
    var _self = this;
    this.renderCreatePanel(app.baseUrl + "zone/edit", $.i18n.prop("edit.zone"), name, {id: id}, {
        content_loaded: function (popup) {
            bm.countryChangeSelection(popup.find("select[name=zone\\.country\\.id]"), true, {isMultiple: "true", stateName: 'zone.state.id'});
        },
        success: function () {
            _self.reload();
        }
    });
}

_z.viewZone = function(id, name) {
    bm.viewPopup(app.baseUrl + "zone/viewZone", {id: id}, {width: 450, clazz: "view-popup zone-view-popup"});
}
