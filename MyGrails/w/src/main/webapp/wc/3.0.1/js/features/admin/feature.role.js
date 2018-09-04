app.tabs.role = function (configs) {
    this.text = $.i18n.prop("roles");
    this.tip = $.i18n.prop("manage.roles");
    this.ui_class = "role";
    this.ajax_url = app.baseUrl + "role/loadAppView";
    app.tabs.role._super.constructor.apply(this, arguments);
}

app.ribbons.administration.push({
    text: $.i18n.prop("role"),
    processor: app.tabs.role,
    ui_class: "role",
    license: CONSTANTS.LICENSES.ACL
})

app.tabs.role.inherit(app.SingleTableTab);

var _r = app.tabs.role.prototype;

_r.advanceSearchUrl = app.baseUrl + "role/advanceFilter";
_r.advanceSearchTitle = $.i18n.prop("role");

_r.menu_entries = [
    {
        text: $.i18n.prop("edit"),
        ui_class: "edit"
    },
    {
        text: $.i18n.prop("manage.operators"),
        ui_class: "manage-operators"
    },
    {
        text: $.i18n.prop("manage.permissions"),
        ui_class: "manage-permissions",
        license: CONSTANTS.LICENSES.ACL
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "remove"
    }
];

(function () {
    function attachEvents() {
        var _self = this;
        this.on("close", function () {
            app.tabs.role.tab = null;
        });
        this.body.find(".toolbar .create").on("click", function() {
            _self.createRole();
        });
    }

    _r.init = function () {
        app.tabs.role._super.init.call(this);
        app.tabs.role.tab = this
        attachEvents.call(this);
    }
    app.global_event.on('after-ribbon-render', function(e, ribbonBar) {
        if(app.isPermitted("role.view.list", {})) {
            ribbonBar.enable("role");
        } else {
            ribbonBar.disable("role");
        }
    });
})();

_r.sortable = {
    list: {
        "1": "name",
        "3": "created",
        "4": "updated"
    },
    sorted: "1",
    dir: "up"
}

_r.onMenuOpen = function (navigation) {
    var menu = this.tabulator.menu;
    var item = [
        {
            key: "role.edit.permission",
            class: "manage-permissions"
        }
    ];

    var roleName = ("" + navigation.config("entity").name).toLowerCase()
    var menuItem = menu.find(".menu-item.edit, .menu-item.remove");
    if (roleName.match(/^(admin|moderator|basic operator)$/)) {
        menuItem.addClass("disabled")
    } else {
        menuItem.removeClass("disabled")
        app.checkPermission(menu, item);
    }
}

_r.onActionMenuOpen = function(navigator) {
    var itemList = [
        {
            key: "role.create",
            class: "create-role"
        }
    ];
    app.checkPermission(navigator, itemList);
}

_r.onActionClick = function (action, data) {
    switch (action) {
        case "edit":
            this.editRole(data.id, data.name);
            break;
        case "remove":
            this.deleteRole(data.id, data.name);
            break
        case "manage-operators":
            this.manageUsers(data.id, data.name);
            break;
        case "manage-permissions":
            this.managePermissions(data.id, data.name);
            break;
    }
};

_r.viewRole = function (id) {
    bm.viewPopup(app.baseUrl + "role/view", {id: id});
};

_r.deleteRole = function (id, name) {
    var _self = this;
    bm.remove("role", "role", $.i18n.prop("confirm.delete.role", [name]), app.baseUrl + "role/delete", id, {
        is_final: true,
        success: function () {
            _self.reload();
        }
    })
}

_r.editRole = function(id, name) {
    var _self = this, title = id ? $.i18n.prop("edit.role") : $.i18n.prop("create.role") ;
    this.renderCreatePanel(app.baseUrl + "role/edit", title, name, {id: id}, {
        success: function () {
            _self.reload();
            app.global_event.trigger("role-update", [id]);
        }
    });
}

_r.manageUsers = function(id, name) {
    bm.editPopup(app.baseUrl + "role/manageUsers", $.i18n.prop("manage.operators"), name, {id: id}, {})
}

_r.managePermissions = function(id, name) {
    bm.permissionPopup($.i18n.prop("manage.permissions"), name, {id: id, for: "role"});
}

_r.onSelectedActionClick = function(action, selecteds){
    switch (action){
        case "manage_permission":
            this.managePermissions(selecteds.collect("id"));
            break;
        case "delete":
            this.deleteSelectedRoles(selecteds.collect("id"));
            break;
    }
}

_r.deleteSelectedRoles = function (ids) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.roles"), function () {
        bm.ajax({
            url: app.baseUrl + "role/deleteSelected",
            data: {ids: ids},
            success: function () {
                _self.reload()

            }
        })
    }, function () {
    })
}

_r.createRole =  function() {
    var _self = this
    this.renderCreatePanel(app.baseUrl + "role/create", $.i18n.prop("create.role"), "", {}, {
        success: function() {
            _self.reload()
            app.global_event.trigger("role-create");
        }
    });
}