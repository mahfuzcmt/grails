app.tabs.page = function () {
    this.text = $.i18n.prop("pages");
    this.tip = $.i18n.prop("manage.pages");
    this.ui_class = "pages";
    this.ajax_url = app.baseUrl + "pageAdmin/loadAppView";
    app.tabs.page._super.constructor.apply(this, arguments);
}

app.ribbons.web_content.push({
    text: $.i18n.prop("page"),
    processor: app.tabs.page,
    ui_class: "page"
})

app.tabs.page.inherit(app.SingleTableTab);

var _p = app.tabs.page.prototype;

_p.advanceSearchUrl = app.baseUrl + "pageAdmin/advanceFilter";
_p.advanceSearchTitle = $.i18n.prop("page");

_p.action_menu_entries = [
    {
        text: $.i18n.prop("manage.owner.permissions"),
        ui_class: "manage-owner-permissions",
        action: "manage-owner-permissions",
        license: CONSTANTS.LICENSES.ACL
    }
];

_p.menu_entries = [
    {
        text: $.i18n.prop("edit"),
        ui_class: "edit"
    },
    {
        text: $.i18n.prop("edit.content"),
        ui_class: "edit-content edit",
        action: "edit-content"
    },
    {
        text: $.i18n.prop("view.in.site"),
        ui_class: "preview view",
        action:"view-in-website"
    },
    {
        text: $.i18n.prop("copy"),
        ui_class: "copy"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "delete"
    },
    {
        text: $.i18n.prop("permission"),
        ui_class: "manage-permission page-permission",
        action: "permission"
    },
    {
        text: $.i18n.prop("set.landing.page"),
        ui_class: "set-landing-page page",
        action: "landing-page"
    }
];

(function () {
    /*Action menu push on multi store enabled*/
    bm.ajax({
        url: app.baseUrl + "store/isMultiModelEnabled",
        success: function (resp) {
            if(resp.enabled) {
                app.tabs.page.prototype.menu_entries.push({
                    text: $.i18n.prop("add.page.for.store"),
                    ui_class: "add-page-for-store",
                    action: "add-page-for-store"
                })
            }
        }
    })

    function attachEvents() {
        var _self = this;
        this.on_global("operator-update", function () {
            _self.reload()
        });
        this.on("close", function () {
            app.tabs.page.tab = null;
        });
        this.on_global("page-restore", function() {
            _self.reload();
        })
        this.body.find(".toolbar .create").on("click", function() {
            _self.createPage();
        })
    }

    _p.init = function () {
        var _self = this;
        app.tabs.page._super.init.call(this);
        app.tabs.page.tab = this;
        attachEvents.call(this);
        bm.tableToggleRow(_self.body);
    }
    app.global_event.on('after-ribbon-render', function(e, ribbonBar) {
        if(app.isPermitted("page.view.list", {})) {
            ribbonBar.enable("page");
        } else {
            ribbonBar.disable("page");
        }
    });
})();

_p.sortable = {
    list: {
        "2": "visibility",
        "3": "name",
        "4": "title",
        "5": "created",
        "6": "updated"
    },
    sorted: "3",
    dir: "up"
};

_p.afterTableReload = function() {
    var _self = this;
    this.body.find(".action-header").hide();
    bm.tableToggleRow(_self.body);
}

_p.onActionClick = function (action, data) {
    switch (action) {
        case "edit":
            this.editPage(data.id, data.name)
            break;
        case "edit-content":
            this.editContent(data.id, data.name);
            break;
        case "view-in-website":
            this.viewInWebsite(data.url)
            break;
        case "delete":
            this.deletePage(data.id, data.name, data.url)
            break;
        case "landing-page":
            this.setLandingPage(data.id, data.url, data.name);
            break;
        case "copy":
            this.copyPage(data.id, data.name);
            break;
        case "permission":
            this.manageEntityPermission(data.id, data.name);
            break;
        case "add-page-for-store":
            this.editPage(data.id, data.name, true, data.storeId, data.parentId)
            break;
    }
};

_p.onActionMenuClick = function(action) {
    switch (action) {
        case "manage-owner-permissions":
            this.manageOwnerPermissions();
            break;
    }
}

_p.viewInWebsite = function(url){
    var url = app.siteBaseUrl + url +"?adminView=true"
    window.open(url,'_blank');
}

_p.onActionMenuOpen = function(navigator) {
    var itemList = [
        {
            key: "page.create",
            class: "create-page"
        },
        {
            key: "page.edit.permission",
            class: "manage-owner-permissions"
        }
    ];
    app.checkPermission(navigator, itemList);
}

_p.onMenuOpen = function(navigator) {
    var menu = this.tabulator.menu;
    if(navigator.is("tr.landing span")) {
        menu.find(".set-landing-page").addClass("disabled");
        menu.find(".delete").addClass("disabled")
    } else {
        menu.find(".set-landing-page").removeClass("disabled");
        menu.find(".delete").removeClass("disabled")
    }
    var config = navigator.config("entity");
    var itemList = [
        {
            key: "page.remove",
            class: "delete",
            isEntity: true
        },
        {
            key: "page.create",
            class: "copy"
        },
        {
            key: "page.edit.properties",
            class: "edit",
            isEntity: true
        },
        {
            key: "page.edit.content",
            class: "edit-content",
            isEntity: true
        },
        {
            key: "page.edit.permission",
            class: "page-permission"
        }
    ];
    app.checkPermission(menu, itemList, config);
}

_p.onSelectedActionClick = function(action, selecteds) {
    var _self = this;
    switch (action) {
        case "administrative_status":
            _self.statusSelectedPage(selecteds);
            break;
        case "visibility":
            //todo: need to implement
            break;
        case "copy":
            _self.copySelectedPage(selecteds.collect("id"));
            break;
        case "remove":
            _self.deleteSelectedPage(selecteds.collect("id"));
            break;
    }
};

app.tabs.page.viewPage = function(id) {
    bm.viewPopup(app.baseUrl + "pageAdmin/view", {
        id: id
    });
};

_p.manageCustomerRestriction = function(form, id) {
    if(form.find("[name='isCustomerSelectorDirty']").length) {
        bm.customerAndGroupSelectionPopup(form, {})
    } else {
        bm.customerAndGroupSelectionPopup(form, {url: app.baseUrl + "pageAdmin/listCustomerAndGroups", data: {id: id}})
    }
};

_p.editPage = function(id, name, addPageForStore, storeId, parentId) {
    var _self = this;
    var data = {id: id}
    var title = $.i18n.prop("edit.page")

    if (typeof id == "undefined") {
        title = $.i18n.prop("create.page")
        name = ""
        data = {}
    }
    if (addPageForStore == true) {
        data.addPageForStore = true
    }
    if(storeId && (storeId != "undefined")) {
        data.storeId = storeId
    }
    if(parentId && (parentId != "undefined")) {
        data.parentId = parentId
    }
    this.renderCreatePanel(app.baseUrl + "pageAdmin/edit", title, name, data, {
        success: function () {
            _self.editFormSuccess(id)
        },
        width: 850,
        content_loaded: function(form) {
            _self.editContentLoaded(form, id, addPageForStore);
        },
        beforeSubmit: function(form, settings, popup) {
            return _self.editFormBeforeSubmit(form);
        }
    });
};

_p.editFormSuccess = function(id) {
    if(id) {
        app.global_event.trigger("page-update", [id]);
    } else {
        app.global_event.trigger("page-create");
    }
    if(app.tabs.page.tab) {
        app.tabs.page.tab.reload()
    }
}

_p.editContentLoaded = function(form, id, addPageForStore) {
    var _self = this;
    form.find("input[name='name']").on("change", function () {
        var titleInp = form.find("input[name='title']")
        if (titleInp.val() == "") {
            titleInp.val($(this).val())
        }
    });

    if (addPageForStore == true) {
        var  storeSelectionField = form.find("select[name='store']")
        var storeIdentifier = storeSelectionField.find(':selected').data("storeidentifier")
        var url = form.find("input[name='url']"), urlValue = url.val()
        var pageName = form.find("input[name='name']"), nameValue = pageName.val()

        function updatePageName() {
            if (urlValue.contains("/")) {
                pageName.val(storeIdentifier + "/" + nameValue.split("/")[1])
            } else {
                pageName.val(nameValue + " (" + storeIdentifier + ")")
            }
        }
        function updatePageUrl() {
            if (urlValue.contains("/")) {
                url.val(storeIdentifier + "/" + urlValue.split("/")[1])
            } else {
                url.val(storeIdentifier + "/" + urlValue)
            }
        }
        updatePageUrl()
        updatePageName()
        storeSelectionField.on("change", function () {
            storeIdentifier = $(this).find(':selected').data("storeidentifier")
            updatePageUrl()
            updatePageName()
        });
    }
    form.find(".tool-icon.choose-customer").on("click", function() {
        _self.manageCustomerRestriction(form, id);
    })
    bm.metaTagEditor(form.find("#bmui-tab-metatag"));
}

_p.editFormBeforeSubmit = function(form) {
    var success = true;
    var  storeSelectionField = form.find("select[name='store']")
    if(form.find("[name='visibility']").val() == "restricted" && form.find("[name=visibleTo]").radio("val") == "selected") {
        if(!form.find("input[name='customer'], input[name='customerGroup']").length) {
            bm.notify($.i18n.prop("no.customer.or.group.selected"), "error");
            success = false;
            return success;
        }
    }
    if(storeSelectionField) {
        if(storeSelectionField.val() == "") {
            bm.notify($.i18n.prop("no.store.selected"), "error");
            success = false;
            return success;
        }
    }
    return success;
}

_p.createPage = _p.editPage;

_p.manageOwnerPermissions = function() {
    bm.permissionPopup($.i18n.prop("manage.owner.permissions"), $.i18n.prop("page"), {for: "owner", type: "page"})
};

_p.setLandingPage = function (id, url, name) {
    var _self = this
    bm.ajax({
        url: app.baseUrl + "pageAdmin/setLandingPage",
        data: {url: url, name: name},
        success: function () {
            app.global_event.trigger("page-update", [id]);
            _self.reload();
        }
    })
}

_p.deletePage = function (id, name) {
    var _self = this;
    bm.remove("page", $.i18n.prop("page"), $.i18n.prop("confirm.delete.page", [name]), app.baseUrl + "pageAdmin/delete", id, {
        success: function () {
            _self.reload();
        }
    })
}

_p.deleteSelectedPage = function(ids) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.selected.page"), function() {
        bm.ajax({
            url: app.baseUrl + "pageAdmin/deleteSelected",
            data: {ids: ids},
            success: function () {
                _self.reload();
                app.global_event.trigger("send-trash", ["page", ids]);
                _self.body.find(".action-header").hide();
            }
        })
    }, function () {
    });
}

_p.statusSelectedPage = function(selecteds) {
    var _self = this;
    bm.editPopup(app.baseUrl + 'pageAdmin/loadStatusOption', $.i18n.prop('status'), null , {},  {
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

_p.copyPage = function(id, name) {
    var _self = this;
    bm.ajax({
        url: app.baseUrl + "pageAdmin/copy",
        data: {id: id},
        success: function () {
            _self.reload();
        }
    })
}

_p.copySelectedPage = function(selecteds) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.copy.selected.page"), function () {
        bm.ajax({
            url: app.baseUrl + "pageAdmin/copySelected",
            data: {ids: selecteds},
            success: function () {
                _self.reload();
                _self.body.find(".action-header").hide();
            }
        })
    }, function () {
    });
}

_p.editContent = function (pageId, pageName) {
    var tabId = "tab-edit-page-" + pageId
    var tab = app.Tab.getTab(tabId)
    if(!tab) {
        tab = new app.tabs.edit_content.page({
            id: tabId,
            containerId : pageId,
            containerName: pageName,
            section: "body"
        });
        tab.render();
    }
    tab.setActive();
}

_p.manageEntityPermission = function(id, name) {
    bm.permissionPopup($.i18n.prop("manage.permissions"), name, {id: id, for: "entity", type: "page"})
};

