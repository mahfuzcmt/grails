var _c = app.tabs.content.prototype;
var _a = app.tabs.content.article.prototype;
var _s = app.tabs.content.section.prototype;

_c.create_menu_entries.push({
    text: $.i18n.prop("create.snippet"),
    ui_class: "create-snippet",
    action: "create-snippet"
});

_c.action_menu_entries.push({
    text: $.i18n.prop("manage.snippet.owner.permissions"),
    ui_class: "manage-snippet-owner-permissions permission",
    action: "manage-snippet-owner-permissions"
});

app.tabs.content.ribbon_data.views.pushAll([
    {
        ui_class: "snippet",
        text: $.i18n.prop("snippet"),
        permission: "snippet.view.list"
    },
    {
        ui_class: "snippet_template",
        text: $.i18n.prop("snippet.template"),
        permission: "snippet_template.view.list"
    }
]);

app.tabs.content.ribbon_data.supers.snippet = "SingleTableTab";
app.tabs.content.ribbon_data.supers.snippet_template = "SingleTableTab";

_c.switch_menu_entries.pushAll([
    {
        text: $.i18n.prop("snippet.list"),
        ui_class: "view-switch snippet-list",
        action: "snippet"
    },
    {
        text: $.i18n.prop("snippet.template.list"),
        ui_class: "view-switch snippet-template-list",
        action: "snippet_template"
    }
]);

_a.switch_menu_entries.pushAll([
    {
        text: $.i18n.prop("snippet.list"),
        ui_class: "view-switch snippet-list",
        action: "snippet"
    },
    {
        text: $.i18n.prop("snippet.template.list"),
        ui_class: "view-switch snippet-template-list",
        action: "snippet_template"
    }
]);

_s.switch_menu_entries.pushAll([
    {
        text: $.i18n.prop("snippet.list"),
        ui_class: "view-switch snippet-list",
        action: "snippet"
    },
    {
        text: $.i18n.prop("snippet.template.list"),
        ui_class: "view-switch snippet-template-list",
        action: "snippet_template"
    }
]);


(function() {
    var _superOnMenuOpen = _c.onMenuOpen;
    _c.onMenuOpen = function(navigator, config) {
        var menu = [];
        var snippetItem = [
            {
                key: "snippet.edit",
                class: "edit-snippet",
                isEntity: true
            },
            {
                key: "snippet.remove",
                class: "delete",
                isEntity: true
            },
            {
                key: "snippet.edit.permission",
                class: "manage-permission.snippet-permission",
                isEntity: true
            }
        ];
        if(navigator == "snippet") {
            menu = this.explorer.menu[navigator];
            app.checkPermission(menu, snippetItem, config);
        } else {
            var conf = navigator.config ? navigator.config("entity") : {};
            if(conf.type == "snippet") {
                menu = this.tabulator.menu;
                app.checkPermission(menu, snippetItem, conf);
            } else {
                _superOnMenuOpen.apply(this, arguments);
            }
        }
    };

    var _superOnMenuClick = _c.onActionClick;
    _c.onActionClick = function (type, action, data) {
        _superOnMenuClick.apply(this, arguments);
        if (arguments.length == 3) {
            data = action;
            action = type;
        }
        switch (action) {
            case "editSnippet":
                this.editSnippet(data.id, data.name);
                break;
            case "deleteSnippet":
                this.deleteSnippet(data.id, data.name);
                break;
            case "edit-content":
                this.editSnippetContent(data.id, data.name)
                break;
            case "snippet-permission":
                this.manageSnippetEntityPermission(data.id, data.name);
                break;
            case "copySnippet":
                this.copySnippet(data.id, data.name);
                break;
        }
    };

    var _superActionMenuOpen = _c.onActionMenuOpen;
    _c.onActionMenuOpen = function(navigator) {
        _superActionMenuOpen.apply(this, arguments);
        app.checkPermission(navigator, [
            {
                key: "snippet.edit.permission",
                class: "manage-snippet-owner-permissions"
            }
        ]);
    }

    var _superOnActionMenuClick = _c.onActionMenuClick;
    _c.onActionMenuClick = function(action) {
        _superOnActionMenuClick.apply(this, arguments);
        switch (action) {
            case "manage-snippet-owner-permissions":
                this.manageSnippetOwnerPermission();
                break;
        }
    }

    var superOnCreateMenuClick = _c.onCreateMenuClick;
    _c.onCreateMenuClick = function(action) {
        superOnCreateMenuClick.apply(this, arguments);
        switch (action) {
            case "create-snippet":
                this.createSnippet();
                break;
        }
    };
    app.global_event.on('after-ribbon-render', function(e, ribbonBar) {
        if(ribbonBar.find(".menu-item.content").length) {
            if(app.isPermitted("section.view.list") && app.isPermitted("article.view.list") && app.isPermitted("snippet.view.list")) {
                ribbonBar.enable("content");
            } else {
                ribbonBar.disable("content");
            }
        }
    });
})();

_c.manageSnippetOwnerPermission = function() {
    bm.permissionPopup($.i18n.prop("manage.owner.permissions"), $.i18n.prop("snippet"), {for: "owner", type: "snippet"})
};

_c.manageSnippetEntityPermission = function(id, name) {
    bm.permissionPopup($.i18n.prop("manage.permissions"), name, {id: id, for: "entity", type: "snippet"})
}


_c.createSnippet = _c.editSnippet = function (id, name, baseTab) {
    var data = {id: id}, title = $.i18n.prop("edit.snippet");
    if (id == undefined) {
        data = {};
        name = "";
        title = $.i18n.prop("create.snippet");
    }
    baseTab = baseTab ? baseTab : this
    baseTab.renderCreatePanel(app.baseUrl + "snippetAdmin/editSnippet", title, name, data, {
        width: 920,
        success: function () {
            if(id) {
                app.global_event.trigger("content-update", [id]);
            } else {
                app.global_event.trigger("content-update");
            }
        }
    });
};

_c.deleteSnippet = function (id, name) {
    var _self = this;
    bm.remove("snippet", $.i18n.prop("snippet"), $.i18n.prop("confirm.delete.snippet", [name]), app.baseUrl + "snippetAdmin/deleteSnippet", id, {
        success: function () {
            _self.reload();
        }
    })
};

_c.copySnippet = function (id, name) {
    var _self = this;
    bm.ajax({
        url: app.baseUrl + "snippetAdmin/copySnippet",
        data: {
            id: id,
            name: name

        },
        success: function () {
            bm.notify($.i18n.prop("snippet.copy.success"), "success");
            _self.reload();
        }
    });
};

_c.editSnippetContent = function(id, name) {
    var tabId = "snippet-editor-tab-" + id;
    var tab = app.Tab.getTab(tabId)
    if(!tab) {
        tab = new app.tabs.edit_snippet({
            id: tabId,
            containerName: name,
            containerId: id
        });
        tab.render()
    }
    tab.setActive();
    return tab
}

app.tabs.content.snippet = function () {
    app.tabs.content.snippet._super.constructor.apply(this, arguments);
};

var _snp = app.tabs.content.snippet.inherit(app.tabs.content);

(function() {
    function attachEvents() {
        var _self = this;
        this.on_global(["snippet-create", "snippet-update"], function() {
            _self.reload();
        })
    };
    var _super = app.tabs.content.snippet._super;
    _snp.init = function() {
        var _self = this;
        app.tabs.content.snippet._super.init.call(this);
        app.tabs.content.tab = this
        attachEvents.call(this);
        this.body.find(".toolbar .create").on("click", function() {
            _self.createSnippet();
        });
    }
})();

_snp.switch_menu_entries = [
    {
        text: $.i18n.prop("article.list"),
        ui_class: "view-switch article-list",
        action: "article"
    },
    {
        text: $.i18n.prop("section.list"),
        ui_class: "view-switch section-list",
        action: "section"
    },
    {
        text: $.i18n.prop("snippet.template.list"),
        ui_class: "view-switch snippet-template-list",
        action: "snippet_template"
    },
    {
        text: $.i18n.prop("explorer.view"),
        ui_class: "view-switch explorer",
        action: "explorer"
    }
];

_snp.menu_entries = [
    {
        text: $.i18n.prop("edit"),
        ui_class: "edit edit-snippet",
        action: "editSnippet"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "delete",
        action: "deleteSnippet"
    },
    {
        text: $.i18n.prop("copy"),
        ui_class: "copy",
        action: "copySnippet"
    },
    {
        text: $.i18n.prop("edit.content"),
        ui_class: "edit edit-snippet",
        action: "edit-content"
    },
    {
        text: $.i18n.prop("permission"),
        ui_class: "permission manage-permission snippet-permission",
        action: "snippet-permission"
    }
];

_snp.onSelectedActionClick = function (action, selecteds) {
    switch (action) {
        case "remove":
            this.deleteSelectedSnippets(selecteds.collect("id"));
            break;
    }
};

_snp.action_menu_entries = [
    {
        text: $.i18n.prop("manage.snippet.owner.permissions"),
        ui_class: "manage-snippet-owner-permissions manage-permission permission",
        action: "manage-snippet-owner-permissions"
    }
];

_snp.deleteSelectedSnippets = function (selecteds) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.selected.snippets"), function() {
        bm.ajax({
            url: app.baseUrl + "snippetAdmin/deleteSelectedSnippets",
            data: {ids: selecteds},
            success: function () {
                _self.reload();
            }
        })
    }, function () {
    });
};

_snp.sortable = {
    list: {
        "1": "name",
        "3": "created",
        "4": "updated"
    },
    sorted: "1",
    dir: "up"
}

_snp.ajax_url = app.baseUrl + "snippetAdmin/loadSnippetView";
_snp.advanceSearchUrl = app.baseUrl + "snippetAdmin/advanceFilter";
_snp.advanceSearchTitle = $.i18n.prop("snippet");

var _e = app.tabs.content.explorer.prototype;
_e.menu_entries.snippet = _snp.menu_entries;

/* Snippet Template*/

app.tabs.content.snippet_template = function () {
    app.tabs.content.snippet_template._super.constructor.apply(this, arguments);
};

var _st = app.tabs.content.snippet_template.inherit(app.tabs.content);

(function() {
    var _super = app.tabs.content.snippet._super;
    _st.init = function() {
        var _self = this;
        app.tabs.content.snippet_template._super.init.call(this);
        app.tabs.content.tab = this
        this.body.find(".toolbar .create").on("click", function() {
            _self.create();
        });
    }
})();

_st.switch_menu_entries = [
    {
        text: $.i18n.prop("article.list"),
        ui_class: "view-switch article-list",
        action: "article"
    },
    {
        text: $.i18n.prop("section.list"),
        ui_class: "view-switch section-list",
        action: "section"
    },
    {
        text: $.i18n.prop("snippet.list"),
        ui_class: "view-switch snippet-list",
        action: "snippet"
    },
    {
        text: $.i18n.prop("explorer.view"),
        ui_class: "view-switch explorer",
        action: "explorer"
    }
];

_st.menu_entries = [
    {
        text: $.i18n.prop("edit"),
        ui_class: "edit",
        action: "edit-template"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "delete",
        action: "delete-template"
    }
];

_st.create = function(uuid, name) {
    var _self = this,title = uuid ? "edit.snippet.template" : "create.snippet.template";
    this.renderCreatePanel(app.baseUrl + "snippetTemplate/create", $.i18n.prop(title), name, {uuid: uuid}, {width: 800,
        success: function(resp) {
            _self.reload();
        }
    });
};

_st.delete = function(id, name) {
    var _self = this
    bm.remove("snippet", $.i18n.prop("snippet.template"), $.i18n.prop("confirm.delete.snippet.template", [name]), app.baseUrl + "snippetTemplate/delete", id, {
        success: function () {
            _self.reload();
        }
    })
};
_st.ajax_url = app.baseUrl + "snippetTemplate/loadAppView";

_st.onActionClick = function(action, data) {
    switch (action) {
        case "edit-template":
            this.create(data.uuid, data.name)
            break;
        case "delete-template":
            this.delete(data.uuid, data.name)
            break

    }
};
_st.onMenuOpen = function(navigator, config) {
    var menu = [];
    var items = [
        {
            key: "snippet_template.edit",
            class: "edit-template",
        },
        {
            key: "snippet_template.remove",
            class: "delete-template",
        }
    ];
    app.checkPermission(this.tabulator.menu, items);
};