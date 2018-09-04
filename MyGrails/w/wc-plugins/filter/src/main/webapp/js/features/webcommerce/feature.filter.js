
app.tabs.filter = function () {
    this.constructor_args = arguments;
    this.text = $.i18n.prop("filter");
    this.tip = $.i18n.prop("manage.filter");
    this.ui_class = "filters";
    app.tabs.filter._super.constructor.apply(this, arguments);
};

var _filter = app.tabs.filter.inherit(app.TwoPanelExplorerTab);

(function () {
    function attachEvents() {
        var _self = this;
    }

    _filter.init = function () {
        app.tabs.filter._super.init.call(this);
        attachEvents.call(this)
    };

})();

_filter.onSwitchMenuClick = function(type) {
    app.Tab.changeView(this, "filter", type);
}

app.tabs.filter.filters = function () {
    app.tabs.filter.filters._super.constructor.apply(this, arguments);
    this.ajax_url = app.baseUrl + "filterAdmin/loadAppView";
    this.left_panel_url = "filterAdmin/loadLeftPanel";
    this.right_panel_views = {
        "default": {
            ajax_url: "filterAdmin/explorerView"
        }
    };
};

var _f = app.tabs.filter.filters.inherit(app.tabs.filter);

(function () {
    function attachEvents() {
        var _self = this;
        this.body.find(".toolbar .create").on("mousedown", function () {
            _self.createNewsletter();
        });
        this.on("close", function () {
            app.tabs.filter.filters.tab = null;
        });
    }

    _f.init = function () {
        app.tabs.filter.filters._super.init.call(this);
        app.tabs.filter.filters.tab = this
        attachEvents.call(this);
    }
    app.global_event.on('after-ribbon-render', function (e, ribbonBar) {

    });
})();

app.ribbons.web_commerce.push({
    text: $.i18n.prop("filter"),
    processor: app.tabs.filter.filters,
    ui_class: "filter",
    views: [
        {ui_class: 'filters', text: $.i18n.prop("filter")},
        {ui_class: 'filterGroups', text: $.i18n.prop("advance.filter")}
    ]

});

_f.menu_entries = [
    {
        text: $.i18n.prop("edit"),
        ui_class: "edit",
        action: "edit"
    },
    {
        text: $.i18n.prop("set.as.default"),
        ui_class: "set-as-default",
        action: "set-as-default"
    },
    {
        text: $.i18n.prop("copy"),
        ui_class: "copy",
        action: "copy"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "delete",
        action: "delete"
    }
];

_f.onActionClick = function (action, data) {
    switch (action) {
        case "edit" :
            this.createProfilePopup(data.id, data.name);
            break;
        case "set-as-default" :
            this.setProfileAsDefault(data.id);
            break;
        case "copy" :
            this.copyProfile(data.id);
            break;
        case "delete":
            this.removeProfile(data.id);
            break;
    }
};

_f.initLeftPanel = function (leftPanel) {
    var _self = this;
    app.tabs.filter.filters._super.initLeftPanel.apply(this, arguments);
    this.body.find(".create-profile").on("click", function () {
        _self.createProfilePopup()
    });
};

_f.initRightPanel = function () {
    var _self = this;
    _self.body.find(".right-panel .panel-header .create-filter").on("click", function () {
        var _this = $(this);
        _self.addFilter(_this.parents(".right-panel").find(".body").attr("profile-id"));
    });
    _self.body.find(".right-panel .body .assigned-filter .remove").on("click", function () {
        var _this = $(this);
        _self.removeFilterFromProfile(_this.parents(".right-panel").find(".body").attr("profile-id"), _this.parents(".assigned-filter").attr("filter-id"));
    });
    _self.body.find(".right-panel .body .assigned-filter-group .remove").on("click", function () {
        var _this = $(this);
        _self.removeFilterGroupFromProfile(_this.parents(".right-panel").find(".body").attr("profile-id"), _this.parents(".assigned-filter-group").attr("filter-group-id"));
    });
    app.tabs.filter.filters._super.initRightPanel.apply(this, arguments);
};

//region profile selection
_f.createProfilePopup = function (id, name) {
    var _self = this, title = id ? "edit.filter.profile" : "add.new.profile";
    bm.editPopup(app.baseUrl + 'filterAdmin/createProfileForm', $.i18n.prop(title), name , {id: id},  {
        width: 600,
        success: function(resp) {
            _self.reload()
        }
    });
};

_f.setProfileAsDefault = function (id) {
    var _self = this;
    bm.ajax({
        url: app.baseUrl + "filterAdmin/setDefaultFilterProfile",
        data: { id: id },
        success: function () {
            _self.reload();
        }
    });
};

_f.removeProfile = function (id) {
    var _self = this;
    bm.ajax({
        url: app.baseUrl + "filterAdmin/deleteFilterProfile",
        data: { id: id },
        success: function () {
            _self.reload()
        }
    });
};

_f.copyProfile = function (id) {
    var _self = this;
    bm.ajax({
        url: app.baseUrl + "filterAdmin/copyFilterProfile",
        data: { id: id },
        success: function () {
            _self.reload()
        }
    });
};

_f.removeFilterFromProfile = function (profileId, filterId) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.filter.from.profile"), function() {
        bm.ajax({
            url: app.baseUrl + "filterAdmin/removeFilterFromProfile",
            data: {
                profileId: profileId,
                filterId: filterId
            },
            success: function () {
                _self.reload(true);
            }
        });
    }, function(){});
};

_f.removeFilterGroupFromProfile = function (profileId, filterGroupId) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.filter.from.profile"), function() {
        bm.ajax({
            url: app.baseUrl + "filterAdmin/removeFilterGroupFromProfile",
            data: {
                profileId: profileId,
                filterGroupId: filterGroupId
            },
            success: function () {
                _self.reload(true);
            }
        });
    }, function(){});
};
//endregion


//region filter selection
_f.addFilter = function (id) {
    var _self = this;
    bm.editPopup(app.baseUrl + "filterAdmin/addFilterPopup", $.i18n.prop("assign.filter"), null , {profile_id: id}, {
        width: 600,
        events: {
            content_loaded: function () {
                var _this = this;
                _this.find(".filter-tile-view").on("click", ".filter-tile", function () {
                    var $this = $(this);
                    if($this.hasClass("selected")) {
                        $this.removeClass("selected");
                        $this.find("[name='selected_filter']").remove();
                    }
                    else {
                        $this.addClass("selected");
                        $this.append($("<input type='hidden' name='selected_filter'/>").val($this.attr("entity-id")));
                    }
                });

                _this.find(".filter-tile-view").on("click", ".filter-group-tile", function () {
                    var $this = $(this);
                    if($this.hasClass("selected")) {
                        $this.removeClass("selected");
                        $this.find("[name='selected_filter_group']").remove();
                    }
                    else {
                        $this.addClass("selected");
                        $this.append($("<input type='hidden' name='selected_filter_group'/>").val($this.attr("entity-id")));
                    }
                });
            }
        },
        success: function () {
            _self.reload(true);
        }
    });
};
//endregion

// customer groups [start]

app.tabs.filter.filterGroups = function () {
    this.id = "filterGroups";
    this.text = $.i18n.prop("advance.filter");
    this.tip = $.i18n.prop("manage.filter.group");
    this.ui_class = "filter-groups";
    app.tabs.filter.filterGroups._super.constructor.apply(this, arguments);
};

var _f_g = app.tabs.filter.filterGroups.inherit(app.SingleTableTab);
(function () {

    function attachEvents() {
        var _self = this;

        this.body.find(".toolbar .create").on("click", function () {
            _self.createFilterGroup();
        });

        this.body.on("click", ".item-action", function () {
            var itemClicked = $(this);
            var id = itemClicked.attr("entity-id");
            var name = itemClicked.attr("entity-name");
            _self.editFilterGroupItems(id, name)
        });
    }

    _f_g.init = function(){
        var _self = this;

        app.tabs.filter.filterGroups._super.init.call(this);
        attachEvents.call(this);
    }

})();

_f_g.ajax_url = app.baseUrl + "filterGroup/loadAppView";

_f_g.switch_menu_entries = [
    {
        text: $.i18n.prop("filter"),
        ui_class: "view-switch filters list-view",
        action: "filters"
    }
];

_f_g.onSwitchMenuClick = function(type) {
    app.Tab.changeView(this, "filter", type);
}

_f_g.menu_entries = [
    {
        text: $.i18n.prop("edit"),
        ui_class: "edit",
        action: "edit"
    },
    {
        text: $.i18n.prop("add.item"),
        ui_class: "addItem",
        action: "addItem"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "remove",
        action: "remove"
    }
];

_f_g.onSelectedActionClick = function (action, selecteds) {
    switch (action) {
        case "remove":
            this.deleteSelectedFilterGroups(selecteds.collect("id"));
            break;
    }
};

_f_g.onActionClick = function (action, data) {
    switch (action) {
        case "edit":
            this.editFilterGroup(data.id, data.name);
            break;
        case "addItem":
            this.editFilterGroupItems(data.id, data.name);
            break;
        case "remove":
            this.deleteFilterGroup(data.id, data.name);
            break;
    }
};

_f_g.editFilterGroup = function (id, name) {
    var _self = this, title = id ? $.i18n.prop("edit.filter.group") : $.i18n.prop("create.filter.group");
    this.renderCreatePanel(app.baseUrl + "filterGroup/edit", title, name, {id: id}, {
        width: 850,
        content_loaded: function () {
            var _self = this;

        },
        success: function () {
            _self.reload();
        }
    });
};

_f_g.createFilterGroup = _f_g.editFilterGroup;

_f_g.deleteFilterGroup = function (id, name) {
    var _self = this;
    bm.remove("filter-group", "Filter Group", $.i18n.prop("confirm.delete.filter.group", [name]), app.baseUrl + "filterGroup/deleteFilterGroup", id, {
        is_final: true,
        success: function () {
            _self.reload();
        }
    })
};

_f_g.deleteSelectedFilterGroups = function (ids) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.selected.filter.group"), function () {
        bm.ajax({
            url: app.baseUrl + "filterGroup/deleteSelectedFilterGroup",
            data: {ids: ids},
            success: function () {
                _self.reload();
                _self.body.find(".action-header").hide();
            }
        })
    }, function () {
    });
};

_f_g.editFilterGroupItems = function (id, name) {
    var tab = app.Tab.getTab("tab-edit-filterGroup-" + id);
    if (!tab) {
        tab = new app.tabs.filterGroupItem({
            filterGroup: {id: id, name: name},
            id: "tab-edit-filterGroup-" + id
        });
        tab.render();
    }
    tab.setActive();
}
