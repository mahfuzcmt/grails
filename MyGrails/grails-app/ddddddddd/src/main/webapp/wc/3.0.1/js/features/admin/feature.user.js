app.tabs.user = function (configs) {
    this.text = $.i18n.prop("operators");
    this.tip = $.i18n.prop("manage.operators");
    this.ui_class = "operator";
    this.ajax_url = app.baseUrl + "user/loadAppView";
    app.tabs.user._super.constructor.apply(this, arguments);
};

app.ribbons.administration.push({
    text: $.i18n.prop("operator"),
    processor: app.tabs.user,
    ui_class: "operator"
});

app.tabs.user.inherit(app.SingleTableTab);

var _u = app.tabs.user.prototype;

_u.menu_entries = [
    {
        text: $.i18n.prop("edit"),
        ui_class: "edit"
    },
    {
        text: $.i18n.prop("assign.roles"),
        ui_class: "manage-role",
        license: CONSTANTS.LICENSES.ACL
    },
    {
        text: $.i18n.prop("manage.permissions"),
        ui_class: "manage-permissions",
        action: "manage",
        license: CONSTANTS.LICENSES.ACL
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "remove"
    }
];



_u.advanceSearchUrl = app.baseUrl + "user/advanceFilter";
_u.advanceSearchTitle = $.i18n.prop("operator");

(function () {
    function attachEvent() {
        var _self = this;
        this.on_global("operator-restore", function() {
            _self.reload();
        })
        this.body.find(".toolbar .create").on("click", function() {
            _self.createUser();
        });
    }

    _u.init = function () {
        app.tabs.user._super.init.call(this);
        attachEvent.call(this);
    };
    app.global_event.on('after-ribbon-render', function(e, ribbonBar) {
        if(app.isPermitted("operator.view.list", {})) {
            ribbonBar.enable("operator");
        } else {
            ribbonBar.disable("operator");
        }
    });

})();

if(!app.isProvisionActive) {
    _u.sortable = {
        list: {
            "1": "fullName",
            "2": "email",
            "3": "created",
            "4": "updated"
        },
        sorted: "1",
        dir: "up"
    };
}
_u.onActionClick = function (action, data) {
    switch (action) {
        case "edit":
            this.editUser(data.id, data.name)
            break;
        case "manage":
            this.managePermissions(data.id, data.name)
            break;
        case "manage-role":
            this.manageRoles(data.id, data.name);
            break;
        case "remove":
            this.deleteUser(data.id, data.name)
            break;
    }
};

_u.onSelectedActionClick = function(action, selecteds) {
    switch (action) {
        case "delete":
            this.deleteSelectedUsers(selecteds.collect("id"))
            break;
        case "status":
            this.changeStatus(selecteds)
            break;
        case "manage_permission":
            this.managePermissions(selecteds.collect("id"), $.i18n.prop("bulk.edit"))
            break;
        case "role_assign":
            this.bulkManageRoles(selecteds, $.i18n.prop("bulk.edit"));
            break;
        case "api_access":
            this.apiAccess(selecteds)
            break;
    }
}

_u.onMenuOpen = function(navigation){
    var menu = this.tabulator.menu;
    var item = [
        {
            key: "operator.remove",
            class: "remove"
        },
        {
            key: "operator.assign.roles",
            class: "manage-role"
        },
        {
            key: "operator.edit",
            class: "edit"
        },
        {
            key: "operator.edit.permission",
            class: "manage-permissions"
        }
    ];
    app.checkPermission(menu, item);

    var cuser = this.body.find("#session-owner-id").val();
    var navuser = navigation.attr("data-id");
    if (cuser == navuser) {
        menu.find(".menu-item.delete").addClass("disabled");
    } else {
        menu.find(".menu-item.delete").removeClass("disabled");
    }
};

_u.onActionMenuOpen = function(navigator) {
    var itemList = [
        {
            key: "operator.create",
            class: "create-user"
        }
    ];
    app.checkPermission(navigator, itemList);
}

_u.viewUser = function (id) {
    bm.viewPopup(app.baseUrl + "user/viewInfo", {
        id: id
    })
};

_u.managePermissions = function (id, name) {
    bm.permissionPopup($.i18n.prop("manage.permissions"), name, {id: id, for: "operator"});
};

_u.editUser = function (id, name) {
    var _self = this;
    this.renderCreatePanel(app.baseUrl + "user/editInfo", $.i18n.prop("edit.operator"), name, { id: id }, {
        content_loaded: function (popup) {
            var _this = $(this)
            _this.find(".change-password .link").click(function () {
                var passwordPanel = _this.find(".change-password-panel");
                var passwordLink = _this.find(".change-password");
                passwordLink.after(passwordPanel.show());
                passwordLink.remove();
                passwordPanel.find(".new-password").change(function () {
                    passwordPanel.find(".match-password").trigger("validate");
                });
                _this.find("input[name='isChangePassword']").val("true");
                popup.trigger("content-change", [passwordPanel])
            });
        },
        success: function () {
            _self.reload()
            app.global_event.trigger("operator-update", [id])
        }
    })
};

_u.deleteUser = function (id, name) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.operator",[name]), function () {
        bm.ajax({
            url: app.baseUrl + "user/delete",
            data: {id: id},
            success: function () {
                app.global_event.trigger("send-trash", ["operator", id])
                _self.reload()
            }
        })
    }, function () {
    });
};

_u.deleteSelectedUsers = function(ids, emails){
    var _self = this
    bm.confirm($.i18n.prop("confirm.delete.operators"), function(){
        bm.ajax({
            url: app.baseUrl + "user/deleteSelected",
            data: {ids: ids,
                   emails: emails},
            success: function() {
                _self.reload();
                app.global_event.trigger("send-trash", ["operator", ids]);
                _self.body.find(".action-header").hide();
            }
        })
    }, function(){
    })
}

_u.manageRoles = function (id, name) {
    bm.editPopup(app.baseUrl + "user/manageRoles", $.i18n.prop("manage.roles"), name, {id: id})
}

_u.bulkManageRoles = function (selecteds, name) {
    bm.editPopup(app.baseUrl + "user/bulkManageRoles", $.i18n.prop("manage.roles"), name, {}, {
        width: 600,
        events: {
            content_loaded: function() {
                var $this = $(this);
                var ids = selecteds.collect("id");
                $.each(ids, function (index, value) {
                    $this.find(".edit-popup-form").append('<input type="hidden" name="id" value="' + value + '">');
                });
            }
        },
        success: function () {
            _self.reload();
        }
    })
}

_u.createUser = function () {
    var _self = this;
    this.renderCreatePanel(app.baseUrl + "user/create", $.i18n.prop("create.operator"), "", {}, {
        success: function () {
            _self.reload();
        }
    });
};

_u.changeStatus = function(selecteds) {
    var _self = this;
    bm.editPopup(app.baseUrl + 'user/loadStatusOption', $.i18n.prop('status'), null , {},  {
        width: 600,
        events: {
            content_loaded: function() {
                var $this = $(this);
                var ids = selecteds.collect("id");
                $.each(ids, function (index, value) {
                    $this.find(".edit-popup-form").append('<input type="hidden" name="id" value="' + value + '">');
                });
            }
        },
        success: function () {
            _self.reload();
        }
    });
};

_u.apiAccess = function(selecteds) {
    var _self = this;
    bm.editPopup(app.baseUrl + 'user/loadApiOption', $.i18n.prop('api.access'), null , {},  {
        width: 600,
        events: {
            content_loaded: function() {
                var $this = $(this);
                var ids = selecteds.collect("id");
                $.each(ids, function (index, value) {
                    $this.find(".edit-popup-form").append('<input type="hidden" name="id" value="' + value + '">');
                });
            }
        },
        success: function () {
            _self.reload();
        }
    });
};