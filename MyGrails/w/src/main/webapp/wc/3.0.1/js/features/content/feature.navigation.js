app.tabs.navigation = function () {
    this.text = $.i18n.prop("navigations");
    this.tip = $.i18n.prop("manage.navigations");
    this.ui_class = "navigations";
    this.ajax_url = app.baseUrl + "navigation/loadAppView";
    app.tabs.navigation._super.constructor.apply(this, arguments);
};

app.ribbons.web_content.push({
    text: $.i18n.prop("navigation"),
    processor: app.tabs.navigation,
    ui_class: "navigation"
});

app.tabs.navigation.inherit(app.SingleTableTab)
var _m = app.tabs.navigation.prototype;

_m.advanceSearchUrl = app.baseUrl + "navigation/advanceFilter";
_m.advanceSearchTitle = $.i18n.prop("navigation");

_m.sortable = {
    list: {
        "1": "name"
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
        ui_class: "remove",
        action: "remove"
    }
];

_m.onSelectedActionClick = function (action, selecteds) {
    switch (action) {
        case "remove":
            this.deleteSelectedNavigations(selecteds.collect("id"));
            break;
        case "restricted":
            bm.editPopup(app.baseUrl + 'navigation/loadRestrictedItemOption', $.i18n.prop('restricted.item'), null , {},  {
                width: 600,
                events: {
                    content_loaded: function() {
                        var $this = $(this);
                        var ids = selecteds.collect("id");
                        $.each(ids, function (index, value) {
                            $this.find(".edit-popup-form").append('<input type="hidden" name="id" value="' + value + '">');
                        });
                    }
                }
            });
            break;
    }
};

(function () {
    function attachEvents() {
        var _self = this;
        this.on_global("navigation-restore", function () {
            _self.reload();
        })
        this.on("close", function () {
            app.tabs.navigation.tab = null;
        });
        this.body.find(".toolbar .create").on("click", function () {
            _self.createNavigation();
        });

        this.body.on("click", ".item-action", function () {
            var itemClicked = $(this);
            var id = itemClicked.attr("entity-id");
            var name = itemClicked.attr("entity-name");
            _self.editNavigationItems(id, name)
        });
    }

    _m.init = function () {
        app.tabs.navigation._super.init.call(this);
        app.tabs.navigation.tab = this
        attachEvents.call(this);
    }

    app.global_event.on('after-ribbon-render', function (e, ribbonBar) {
        if (app.isPermitted("navigation.view.list")) {
            ribbonBar.enable("navigation");
        } else {
            ribbonBar.disable("navigation");
        }
    });
})();

_m.onMenuOpen = function (navigator, config) {
    var menu = this.tabulator.menu;
    var menuItem = [
        {
            key: "navigation.edit",
            class: "edit"
        },
        {
            key: "navigation.edit.items",
            class: "manage-navigation-item, .manage-item-image"
        },
        {
            key: "navigation.remove",
            class: "remove"
        }
    ];
    app.checkPermission(menu, menuItem);
    if(navigator.is("[is-default='true']")) {
        menu.find(".remove").addClass("disabled")
    } else {
        menu.find(".remove").removeClass("disabled")
    }
}

_m.onActionMenuOpen = function (navigator) {
    var itemList = [
        {
            key: "navigation.create",
            class: "create-navigation"
        }
    ];
    app.checkPermission(navigator, itemList);
}

_m.advanceSearchUrl = app.baseUrl + "navigation/advanceFilter";
_m.advanceSearchTitle = $.i18n.prop("navigation");

_m.onActionClick = function (action, data) {
    switch (action) {
        case "edit":
            this.createNavigation(data.id, data.name);
            break;
        case "remove":
            this.deleteNavigation(data.id, data.name);
            break;
    }
};

_m.createNavigation = function (id, name) {
    var data = {id: id}
    var title = $.i18n.prop("edit.navigation")

    if (typeof id == "undefined") {
        title = $.i18n.prop("create.navigation");
        name = ""
        data = {}
    }
    this.renderCreatePanel(app.baseUrl + "navigation/create", title, name, data, {
        success: function () {
            if (app.tabs.navigation.tab) {
                app.tabs.navigation.tab.reload()
            }
        }
    });
}

_m.editNavigationItems = function (id, name) {
    if (app.Tab.getTab("navigation-item-image-editor-" + id)) {
        bm.notify($.i18n.prop("this.navigation.open.in.item.imageeditor.close.that.first"), "error");
        return;
    }
    var tab = app.Tab.getTab("tab-edit-navigation-" + id);
    if (!tab) {
        tab = new app.tabs.navigationItem({
            navigation: {id: id, name: name},
            id: "tab-edit-navigation-" + id
        });
        tab.render();
    }
    tab.setActive();
}

_m.deleteNavigation = function (id, name) {
    var _self = this;
    bm.remove("navigation", $.i18n.prop("Navigation"), $.i18n.prop("confirm.delete.navigation", [name]), app.baseUrl + "navigation/delete", id, {
        success: function () {
            _self.reload();
        }
    })
}

_m.deleteSelectedNavigations = function (ids) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.selected.navigation"), function () {
        bm.ajax({
            url: app.baseUrl + "navigation/deleteSelected",
            data: {ids: ids},
            success: function () {
                _self.reload();
                app.global_event.trigger("send-trash", ["navigation", ids]);
                _self.body.find(".action-header").hide();
            }
        })
    }, function () {
    });
}