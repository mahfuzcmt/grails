app.tabs.trash = function() {
    this.text = $.i18n.prop("trash");
    this.tip = $.i18n.prop("manage.trash");
    this.ui_class = "trash";
    this.ajax_url = app.baseUrl + "trash/loadAppView";
    app.tabs.trash._super.constructor.apply(this, arguments);
}

app.ribbons.administration.push({
    text: $.i18n.prop("trash"),
    processor: app.tabs.trash,
    ui_class: "trash"
})

app.tabs.trash.inherit(app.SingleTableTab);

var _t = app.tabs.trash.prototype;
_t.advanceSearchUrl = app.baseUrl + "trash/advanceFilter";
_t.advanceSearchTitle = $.i18n.prop("trash");

_t.sortable = {
    list: {
        "2": "name",
        "3": "created"
    },
    sorted:   "2",
    dir:    "up"
}

_t.beforeReloadRequest = function (config) {
    app.tabs.trash._super.beforeReloadRequest.call(this, config)
    var selectedDomain = this.body.find("select.selected-domain");
    if(this.advanceSearchFilter) {
        $.extend(config, {advanceSearch: true})
    }
    if(config.advancedSearchDomain){
        $.extend(config, {selectedDomain: config.advancedSearchDomain})
        selectedDomain.val("")
            .chosen()
    }else {
        $.extend(config, {selectedDomain: selectedDomain.val()})
    }
};

(function() {
    var domain;
    function attachEvents() {
        var _self = this;
        domain = _self.body.find(".selected-domain").val();
        this.body.find(".selected-domain").change(function () {
            domain = _self.body.find(".selected-domain").val();
            _self.body.find(".tool-group.search-form").trigger("submit");
        });
        this.on_global("send-trash", function(evt, dom) {
            if(domain.equals(dom, false)|| domain == "") {
                _self.reload();
            }
        });
        this.on_global("trash-restore", function(evt, dom) {
            if(domain.equals(dom, false) || domain == "") {
                _self.reload();
            }
        });
        this.on_global("trash-delete", function(evt, dom) {
            if(!dom || domain.equals(dom, false) || domain == "") {
                _self.reload();
            }
        });
        var groupSelection = this.body.find(".action-header select.action-on-selection");
        ["restore", "remove"].every(function() {
            var key = "trash." +  this;
            if(!app.isPermitted(key, {})) {
                groupSelection.chosen("disable", this.toString(), true)
            }
        });
    }
    _t.init = function() {
        app.tabs.trash._super.init.call(this);
        attachEvents.call(this);
    }

    app.global_event.on('after-ribbon-render', function(e, ribbonBar) {
        if(app.isPermitted("trash.view.list")) {
            ribbonBar.enable("trash");
        } else {
            ribbonBar.disable("trash");
        }
    });
})();

_t.restore = function(id, name, type, hasParent) {
    var _self = this;
    bm.ajax({
        url: app.baseUrl + "trash/restore",
        data: {
            id: id,
            name: name,
            type: type,
            hasParent: hasParent
        },
        success: function() {
            app.global_event.trigger(type.toLowerCase() + "-restore", [id]);
            _self.reload()
        },
        error: function(resp) {
            bm.confirm($.i18n.prop("confirm.restore.category.has.parent"), function() {
                _self.restore(id, name, type, true)
            }, function() {
            })
        }
    })
}

_t.deleteEntity = function(id,name, type) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.from.trash", [bm.htmlEncode(name), type]), function () {
        bm.ajax({
            url: app.baseUrl + "trash/delete",
            data: { id: id,
                name: name,
                type: type
            },
            success: function () {
                _self.reload();
            }
        })
    }, function () {
    });
}

_t.onActionClick = function(action, data) {
    switch(action){
        case "restore":
            this.restore(data.id, data.name, data.type)
            break;
        case "delete":
            this.deleteEntity(data.id, data.name, data.type)
            break;
    }
};

_t.menu_entries = [
    {
        text: $.i18n.prop("restore"),
        ui_class: "restore",
        action: "restore"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "delete",
        action: "delete"
    }
];

_t.onMenuOpen = function() {
    var menu = this.tabulator.menu;
    var item = [
        {
            key: "trash.restore",
            class: "restore"
        },
        {
            key: "trash.remove",
            class: "delete"
        }
    ];
    app.checkPermission(menu, item);
}

_t.restoreSelectedItems = function(selecteds) {
    var _self = this;
    bm.ajax({
        url: app.baseUrl + "trash/restoreSelectedItems",
        data: {
            ids: selecteds.collect("id"),
            types: selecteds.collect("type")
        },
        success: function() {
            var typeBased = {};
            $.each(selecteds, function() {
                var ids = typeBased[this.type.toLowerCase()] || (typeBased[this.type.toLowerCase()] = [])
                ids.push(this.id)
            })
            $.each(typeBased, function(type) {
                app.global_event.trigger(type + "-restore", [this]);
            })
            _self.reload();
            _self.body.find(".action-header").hide();
        }
    })
}

_t.deleteSelectedItems = function (selecteds) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.trash"), function () {
        bm.ajax({
            url: app.baseUrl + "trash/deleteSelectedItems",
            data: {
                ids: selecteds.collect("id"),
                types: selecteds.collect("type")
            },
            success: function () {
                var typeBased = {};
                $.each(selecteds, function () {
                    var ids = typeBased[this.type.toLowerCase()] || (typeBased[this.type.toLowerCase()] = [])
                    ids.push(this.id)
                })
                $.each(typeBased, function (type) {
                    app.global_event.trigger(type + "-delete", [this]);
                })
                _self.reload();
                _self.body.find(".action-header").hide();
            }
        })
    }, function () {

    })
}

_t.onSelectedActionClick = function(action, selecteds) {
    var _self = this;
    switch (action){
        case "restore":
            _self.restoreSelectedItems(selecteds)
            break;
        case "delete":
            _self.deleteSelectedItems(selecteds)
            break;
    }
}