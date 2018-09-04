app.tabs.content = function () {
    this.constructor_args = arguments;
    this.text = $.i18n.prop("contents");
    this.tip = $.i18n.prop("manage.web.contents");
    this.ui_class = "contents";
    app.tabs.content._super.constructor.apply(this, arguments);
};

var _c = app.tabs.content.inherit(app.ExplorerPanelTab);

_c.switch_menu_entries = [
    {
        text: $.i18n.prop("article.list"),
        ui_class: "view-switch article-list",
        action: "article"
    },
    {
        text: $.i18n.prop("section.list"),
        ui_class: "view-switch section-list",
        action: "section"
    }
];

_c.create_menu_entries = [
    {
        text: $.i18n.prop("create.article"),
        ui_class: "create-article",
        action: "create-article"
    },
    {
        text: $.i18n.prop("create.section"),
        ui_class: "create-section",
        action: "create-section"
    }
];

_c.action_menu_entries = [
    {
        text: $.i18n.prop("manage.section.owner.permissions"),
        ui_class: "manage-section-owner-permissions permission",
        action: "manage-section-owner-permissions",
        license: CONSTANTS.LICENSES.ACL
    },
    {
        text: $.i18n.prop("manage.article.owner.permissions"),
        ui_class: "manage-article-owner-permissions permission",
        action: "manage-article-owner-permissions",
        license: CONSTANTS.LICENSES.ACL
    }
];

(function () {
    function attachEvents() {
        var _self = this;
        this.body.find(".section-selector").change(function () {
            _self.body.find(".search-form").trigger("submit");
        })
        this.on_global(["article-restore", "news-update", "content-update"], function() {
            _self.reload();
        })
        this.on("close", function () {
            app.tabs.content.tab = null;
        });
    }

    _c.init = function () {
        app.tabs.content._super.init.call(this);
        app.tabs.content.tab = this
        attachEvents.call(this);
    };

    app.global_event.on('after-ribbon-render', function(e, ribbonBar) {
        if(ribbonBar.find(".menu-item.content").length) {
            if(app.isPermitted("section.view.list") && app.isPermitted("article.view.list")) {
                ribbonBar.enable("content");
            } else {
                ribbonBar.disable("content");
            }
        }
    });
})();

_c.onSwitchMenuClick = function(type) {
    app.Tab.changeView(this, "content", type, type == "explorer" ? "ExplorerPanelTab" : "SingleTableTab");
}

_c.onActionMenuClick = function(action) {
    switch (action) {
        case "manage-section-owner-permissions":
            this.manageSectionOwnerPermissions();
            break;
        case "manage-article-owner-permissions":
            this.manageArticleOwnerPermissions();
            break;
    }
}

_c.onCreateMenuClick = function(action) {
    switch (action) {
        case "create-article":
            this.createArticle();
            break;
        case "create-section":
            this.createSection();
            break;
    }
};

_c.onMenuOpen = function(navigator, config) {
    var menu = [];
    var item;
    var sectionItem = [
        {
            key: "section.edit",
            class: "edit",
            isEntity: true
        },
        {
            key: "section.remove",
            class: "delete",
            isEntity: true
        },
        {
            key: "section.edit.permission",
            class: "manage-permission.section-permission",
            isEntity: true
        }
    ];
    var articleItem = [
        {
            key: "article.edit",
            class: "edit",
            isEntity: true
        },
        {
            key: "article.create",
            class: "copy",
            isEntity: true
        },
        {
            key: "article.remove",
            class: "delete",
            isEntity: true
        },
        {
            key: "article.edit.permission",
            class: "manage-permission.article-permission",
            isEntity: true
        }
    ];
    if(navigator == "section") {
        menu = this.explorer.menu[navigator];
        item = sectionItem;
    } else if(navigator == "article") {
        menu = this.explorer.menu[navigator];
        item = articleItem;
    } else {
        menu = this.tabulator.menu
        config = navigator.config("entity");
        if(config.type == "section") {
            item = sectionItem;
        } else if(config.type == "article"){
            item = articleItem;
        }
    }
    app.checkPermission(menu, item, config);
}

_c.onActionMenuOpen = function(navigator) {
    var itemList = [
        {
            key: "section.create",
            class: "create-section"
        },
        {
            key: "article.create",
            class: "create-article"
        },
        {
            key: "section.edit.permission",
            class: "manage-section-owner-permissions"
        },
        {
            key: "article.edit.permission",
            class: "manage-article-owner-permissions"
        }
    ];
    app.checkPermission(navigator, itemList);
}

_c.beforeReloadRequest = function (param) {
    app.tabs.content._super.beforeReloadRequest.call(this, param);
    var sectionFilter = this.body.find(".section-selector");
    if(this.advanceSearchFilter){
        sectionFilter.chosen("val", param.section);
    }
    $.extend(param, {section: sectionFilter.val()})
};

_c.onActionClick = function (type, action, data) {
    if(arguments.length == 3) {
        data = action;
        action = type;
    }
    switch (action) {
        case "editArticle":
            this.editArticle(data.id, data.name);
            break;
        case "copyArticle":
            this.copyArticle(data.id, data.name)
            break;
        case "deleteArticle":
            this.deleteArticle(data.id, data.name);
            break
        case "editSection":
            this.editSection(data.id, data.name);
            break;
        case "deleteSection":
            this.deleteSection(data.id, data.name);
            break;
        case "section-permission":
            this.manageSectionEntityPermission(data.id, data.name);
            break;
        case "article-permission":
            this.manageArticleEntityPermission(data.id, data.name);
            break;
    }
};

_c.onSelectedActionClick = function (action, selecteds) {
    switch (action) {
        case "copy":
            this.copySelectedArticles(selecteds.collect("id"));
            break;
        case "remove":
            this.deleteSelectedArticle(selecteds.collect("id"));
            break;
        case "removeSections":
            this.deleteSelectedSections(selecteds.collect("id"));
            break;
    }
};

_c.deleteSelectedSections = function(selecteds) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.selected.section"), function() {
        bm.ajax({
            url: app.baseUrl + "content/deleteSelectedSections",
            data: {ids: selecteds},
            success: function () {
                _self.reload();
                _self.body.find(".action-header").hide();
            }
        })
    },function(){
    });
}

_c.copySelectedArticles = function (selecteds) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.copy.selected.article"), function() {
        bm.ajax({
            url: app.baseUrl + "content/copySelectedArticles",
            data: {ids: selecteds},
            success: function () {
                _self.reload(true);
                _self.body.find(".action-header").hide();
            }
        })
    }, function() {
    });
};

_c.deleteSelectedArticle = function (selecteds) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.selected.article"), function() {
        bm.ajax({
            url: app.baseUrl + "content/deleteSelectedArticles",
            data: {ids: selecteds},
            success: function () {
                app.global_event.trigger("send-trash", ["article", selecteds])
                app.global_event.trigger("article-delete", [selecteds])
                _self.body.find(".action-header").hide();
                _self.reload();
            }
        })
    }, function () {
    });
}

_c.manageSectionOwnerPermissions = function() {
    bm.permissionPopup($.i18n.prop("manage.owner.permissions"), $.i18n.prop("section"), {for: "owner", type: "section"})
};

_c.manageArticleOwnerPermissions = function() {
    bm.permissionPopup($.i18n.prop("manage.owner.permissions"), $.i18n.prop("article"), {for: "owner", type: "article"})
};

_c.createArticle = _c.editArticle = function (id, name, baseTab) {
    var _self = this;
    var data = {id: id}, title = $.i18n.prop("edit.article");
    baseTab = baseTab ? baseTab : _self
    if (typeof id == "undefined") {
        data = {};
        name = "";
        title = $.i18n.prop("create.article");
    }
    baseTab.renderCreatePanel(app.baseUrl + "content/editArticle", title, name, data, {
        width: 920,
        success: function () {
            if(id) {
                app.global_event.trigger("article-update", [id])
            } else {
                app.global_event.trigger("article-create")
            }
        }
    });
};

app.tabs.content.editArticle = _c.editArticle;

_c.deleteArticle = function (id, name) {
    var _self = this;
    bm.remove("article", $.i18n.prop("article"), $.i18n.prop("confirm.delete.article", [name]), app.baseUrl + "content/deleteArticle", id, {
        success: function () {
            app.global_event.trigger("article-delete")
            _self.reload(true);
        }
    })
};

_c.copyArticle = function(id) {
    var _self = this;
    bm.ajax({
        url: app.baseUrl + "content/copyArticle",
        data: {id: id},
        success: function () {
            _self.reload(true);
        }
    });
};

app.tabs.content.viewArticle = function(id) {
    bm.viewPopup(app.baseUrl + "content/viewArticle", {id: id}, {width: 600});
};

_c.editSection = function(id, name) {
    var _self = this;
    var data = {id: id},
        title = $.i18n.prop("edit.section");
    if (typeof id == "undefined") {
        data = {};
        name = "";
        title = $.i18n.prop("create.section");
    }
    this.renderCreatePanel(app.baseUrl + "content/editSection", title, name, data, {
        success: function() {
            _self.reload();
            if(id) {
                app.global_event.trigger("section-update", [id])
            } else {
                app.global_event.trigger("section-create")
            }
        }
    });
}

_c.createSection = _c.editSection;

_c.deleteSection = function (id, name) {
    var _self = this;

    bm.remove("section", $.i18n.prop("section"), $.i18n.prop("confirm.delete.section", [name]), app.baseUrl + "content/confirmDeleteSection", id, {
        success: function () {
            _self.reload();
        }
    })
};

_c.manageSectionEntityPermission = function(id, name) {
    bm.permissionPopup($.i18n.prop("manage.section.permissions"), name, {id: id, for: "entity", type: "section"})
}

_c.manageArticleEntityPermission = function(id, name) {
    bm.permissionPopup($.i18n.prop("manage.article.permissions"), name, {id: id, for: "entity", type: "article"})
}

app.tabs.content.article = function () {
    app.tabs.content.article._super.constructor.apply(this, arguments);
};

var _a = app.tabs.content.article.inherit(app.tabs.content);
(function() {
    var _super = app.tabs.content.article._super;
    _a.init = function() {
        var _self = this;
        this.body.find(".toolbar .create").on("click", function() {
           _self.editArticle();
        });
        this.on_global(["article-create", "article-update"], function() {
            _self.reload();
        })
        _super.init.apply(this, arguments)
    }
})();
_a.beforeReloadRequest = function (param) {
    app.tabs.content._super.beforeReloadRequest.call(this, param);
    var sectionFilter = this.body.find(".section-selector");
    if(this.advanceSearchFilter){
        sectionFilter.parent().find("select").chosen("disable", true)
        $.extend(param, {section: this.advanceSearchFilter.section})
    }else {
        sectionFilter.parent().find("select").chosen("disable", false)
        $.extend(param, {section: sectionFilter.val()})
    }
};

_a.switch_menu_entries = [
    {
        text: $.i18n.prop("section.list"),
        ui_class: "view-switch section-list",
        action: "section"
    },
    {
        text: $.i18n.prop("explorer.view"),
        ui_class: "view-switch explorer",
        action: "explorer"
    }
];

_a.action_menu_entries = [
    {
        text: $.i18n.prop("manage.article.owner.permissions"),
        ui_class: "manage-article-owner-permissions manage-permission",
        action: "manage-article-owner-permissions",
        license: CONSTANTS.LICENSES.ACL
    }
];

_a.menu_entries = [
    {
        text: $.i18n.prop("edit"),
        ui_class: "edit",
        action: "editArticle"
    },
    {
        text: $.i18n.prop("copy"),
        ui_class: "copy",
        action: "copyArticle"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "delete",
        action: "deleteArticle"
    },
    {
        text: $.i18n.prop("permission"),
        ui_class: "manage-permission article-permission permission",
        action: "article-permission"
    }
]

_a.sortable = {
    list: {
        "1": "isPublished",
        "2": "name",
        "4": "created",
        "5": "updated"
    },
    sorted: "2",
    dir: "up"
}

_a.ajax_url = app.baseUrl + "content/loadArticleView";
_a.advanceSearchUrl = app.baseUrl + "content/advanceFilter";
_a.advanceSearchTitle = $.i18n.prop("article");

app.tabs.content.section = function () {
    app.tabs.content.section._super.constructor.apply(this, arguments);
};

var _s = app.tabs.content.section.inherit(app.tabs.content);
(function() {
    var _super = app.tabs.content.section._super;
    _s.init = function() {
        var _self = this
        this.body.find(".toolbar .create").on("click", function() {
            _self.editSection();
        });
        this.on_global(["section-create", "section-update"], function() {
            _self.reload();
        })
        _super.init.apply(this, arguments)
    }
})();
_s.switch_menu_entries = [
    {
        text: $.i18n.prop("article.list"),
        ui_class: "view-switch article-list",
        action: "article"
    },
    {
        text: $.i18n.prop("explorer.view"),
        ui_class: "view-switch explorer",
        action: "explorer"
    }
];

_s.action_menu_entries = [
    {
        text: $.i18n.prop("manage.section.owner.permissions"),
        ui_class: "manage-section-owner-permissions manage-permission",
        action: "manage-section-owner-permissions",
        license: CONSTANTS.LICENSES.ACL
    }
];

_s.menu_entries = [
    {
        text: $.i18n.prop("edit"),
        ui_class: "edit",
        action: "editSection"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "delete",
        action: "deleteSection"
    },
    {
        text: $.i18n.prop("permission"),
        ui_class: "manage-permission section-permission permission",
        action: "section-permission"
    }
]

_s.sortable = {
    list: {
        "1": "name",
        "2": "parent"
    },
    sorted: "1",
    dir: "up"
}

_s.ajax_url = app.baseUrl + "content/loadSectionView";

app.tabs.content.explorer = function () {
    app.tabs.content.explorer._super.constructor.apply(this, arguments);
};

var _e = app.tabs.content.explorer.inherit(app.tabs.content);
_e.init = function () {
    var _self = this
    app.tabs.content.explorer._super.init.call(this);
    this.on_global(["article-update", "article-create"], function() {
        _self.reload()
    })
};
app.ribbons.web_content.push(app.tabs.content.ribbon_data = {
    text: $.i18n.prop("content"),
    ui_class: "content",
    processor: app.tabs.content.explorer,
    views: [
        {ui_class: "article", text: $.i18n.prop("article"), permission: "article.view.list"},
        {ui_class: "section", text: $.i18n.prop("section"), permission: "section.view.list"},
    ],
    supers: {
        explorer: "ExplorerPanelTab",
        article: "SingleTableTab",
        section: "SingleTableTab",
    }

});

_e.ajax_url = app.baseUrl + "content/loadExplorerView";
_e.explorer_url = app.baseUrl + "content/explorePanel";
_e.menu_entries = {
    section: _s.menu_entries,
    article: _a.menu_entries
};
_e.tree_node_load_url = app.baseUrl + "content/sectionTree";
_e.root_node_name = $.i18n.prop("root.section");
_e.tree_node_type = "section"

_e.onMoveTreeNode = function(node, sourceNode, hitMode, doMove) {
    //TODO Section move should be done here
}