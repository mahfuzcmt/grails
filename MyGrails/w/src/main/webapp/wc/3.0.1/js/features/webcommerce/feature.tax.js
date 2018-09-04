//region tax tab
app.tabs.tax = function () {
    var _self = this;
    _self.selected = null;
    _self.text = $.i18n.prop("tax");
    _self.tip = $.i18n.prop("manage.tax");
    _self.ui_class = "tax";
    app.tabs.tax._super.constructor.apply(this, arguments);
    _self.left_panel_url = "taxAdmin/loadLeftPanel";
    _self.ajax_url = app.baseUrl + "taxAdmin/loadAppView";
    _self.right_panel_views = {
        "default": {
            processor: app.tabs.tax.rule_editor,
            ajax_url: "taxAdmin/explorerView"
        },
        "rule_list": {
            ajax_url: "taxAdmin/ruleAppView",
            processor: app.tabs.tax.ruleView
        },
        "code_list": {
            ajax_url: "taxAdmin/codeAppView",
            processor: app.tabs.tax.codeView
        }
    };
};

var _t = app.tabs.tax.inherit(app.TwoPanelExplorerTab);

app.ribbons.web_commerce.push({
    text: $.i18n.prop("tax"),
    ui_class: "tax",
    processor: app.tabs.tax,
    permission: "tax.view.list",
    ecommerce: true
});

app.global_event.on('after-ribbon-render', function(e, ribbonBar) {
    if(app.taxConfigType == "manual") {
        ribbonBar.show("tax")
    } else {
        ribbonBar.hide("tax")
    }
});

_t.menu_entries = [
    {
        text: $.i18n.prop("edit"),
        ui_class: "edit",
        action: "edit"
    } ,
    {
        text: $.i18n.prop("remove"),
        ui_class: "delete",
        action: "delete"
    }

];

_t.root_selector_menu_entries = [
    {
        ui_class: "edit",
        action: "edit"
    } ,
    {
        ui_class: "remove",
        action: "remove"
    }
];

_t.onActionClick = function (action, data) {
    switch (action) {
        case "edit" :
            this.ruleInfoEdit(data.id, data.name);
            break;
        case "delete":
            this.detachRule(data);
            break;
    }
};

_t.onRootSelectorActionClick = function (action, data) {
    switch (action) {
        case "edit" :
            this.profileInfoEdit(data.value, data.text);
            break;
        case "remove":
            this.deleteProfile(data.value, data.text);
            break;
    }
};

(function () {
    var _super = app.tabs.tax._super;
    _t.init = function () {
        _super.init.call(this);
        var _self = this;
        _self.body.find(".create-rule-top").on("click", function () {
            _self.addRule()
        });
        this.initRightPanel(_self.body.find(".right-panel"));
    };

    _t.initLeftPanel = function() {
        var _self = this;
        _super.initLeftPanel.apply(this, arguments);
        this.attachLeftPanel();
    };
})();

_t.attachLeftPanel = function () {
    var _self = this;
    var body = _self.body;
    _self.profileId = null;
    var profileSelector = body.find("select.tax-profile-selector");
    profileSelector.on("change", function () {
        _self.profileId = profileSelector.val();
        _self.current_right_panel_view = "default";
        _self.reload()
    });
    body.find(".create-profile").on("click", function () {
        _self.profileInfoEdit()
    });
    body.find(".navigation-button").click(function () {
        var type = this.jqObject.attr("item-type");
        switch (type) {
            case "zone":
                ComponentManager.openTab(ComponentManager.getRibbonData(app.ribbons.administration, "zone"));
                break;
            case "rule":
                _self.changeRightPanelView("rule_list");
                break;
            case "code":
                _self.changeRightPanelView("code_list");
                break;
            default:
                break;
        }
    });
};

_t.initRightPanel = function() {
    app.tabs.tax._super.initRightPanel.apply(this, arguments);
    if (this.current_right_panel_view === "default") {
        var _self = this;
        var body = _self.body;
        this.body.find(".create-rule-top").show();
        this.body.find(".create-code-top").hide();
    }
};

_t.beforeLeftPanelReload = function (data) {
    var _self = this;
    $.extend(data, {profileId: _self.profileId});
    if(_self.ruleId && _self.current_right_panel_view == "default") {
        $.extend(data, {selected: _self.ruleId });
        _self.ruleId = null
    }
};

_t.beforeRightPanelReload = function (data) {
    var _self = this;
    $.extend(data, {profileId: _self.profileId});
    _self.profileId = null;
};

_t.profileInfoEdit = function (id, name) {
    var _self = this, title = id ? "edit.tax.profile" : "create.tax.profile";
    bm.editPopup(app.baseUrl + 'taxAdmin/profileInfoEdit', $.i18n.prop(title), name , {id: id},  {
        width: 600,
        success: function() {
            _self.current_right_panel_view = "default";
            _self.reload(false)
        }
    });
};

_t.deleteProfile = function(id, name) {
    var _self = this;
    bm.remove("taxProfile", "TaxProfile", $.i18n.prop("confirm.delete.tax.profile", [name]), app.baseUrl + "taxAdmin/deleteProfile", id, {
        success: function () {
            _self.reload(false);
        },
        is_final: true
    })
};

_t.ruleInfoEdit = function (id, name) {
    var _self = this, currentView = _self.current_right_panel_view, body = _self.body;
    var title = name ? name :  $.i18n.prop("create.tax.rule"), profileId = body.find(".tax-profile-selector").val();
    var selectedId = currentView === "default" ? profileId: null;
    bm.editPopup(app.baseUrl + 'taxAdmin/ruleInfoEdit', title, null , {profileId: selectedId, ruleId: id},  {
        width: 600,
        success: function() {
            _self.profileId = profileId;
            _self.ruleId = id;
            _self.reload()
        }
    });
};

_t.addRule = function () {
    var _self = this;
    bm.editPopup(app.baseUrl + 'taxAdmin/addRulePopup', $.i18n.prop("add.rule"), null , null,  {
        width: 600,
        events: {
            content_loaded: function() {
                var $this = $(this);
                $this.find(".rule-option").first().addClass("selected");
                $this.find(".rule-option").on("click", function() {
                    $this.find(".rule-option.selected").removeClass("selected");
                    $(this).addClass("selected")
                })
            }
        },
        beforeSubmit: function (form, data, popup) {
            var type = form.find(".rule-option.selected").attr("type");
            popup.close();
            if (type === "new") {
                _self.ruleInfoEdit();
            } else if (type === "copy") {
                _self.selectExistingRule()
            }
            return false
        }
    });
};

_t.selectExistingRule = function () {
    var _self = this, body = _self.body, currentView = _self.current_right_panel_view;
    var profileId = body.find(".tax-profile-selector").val();
    _self.selectedRuleId = null;
    _self.profileId = profileId;
    var tableView;
    bm.editPopup(app.baseUrl + 'taxAdmin/useExistingRulePopup', $.i18n.prop("select.rule"), null , {},  {
        width: 600,
        events: {
            content_loaded: function() {
                var resp = $(this);
                tableView = new app.tabs.tax.ruleView(resp, _self, "taxAdmin/useExistingRulePopup");
                tableView.init();
            }
        },
        beforeSubmit: function (form, data, popup) {
            var selected = tableView.getSelectedEntities();
            if(!selected.length) {
                bm.notify($.i18n.prop("select.rule"), "alert");
            } else {
                popup.close();
                var id = selected[0].id;
                _self.selectedRuleId = id;
                _self.selectedRule(id)
            }
            return false
        }
    });
};

_t.selectedRule = function (id) {
    var _self = this;
    var profileId = _self.body.find(".tax-profile-selector").val();
    bm.editPopup(app.baseUrl + 'taxAdmin/selectedRule', $.i18n.prop("use.existing.rule"), null , {ruleId: id, profileId: profileId},  {
        width: 600,
        success: function () {
            _self.reload()
        }
    });
};

_t.detachRule = function(data) {
    var _self = this;
    var profileId = _self.body.find(".tax-profile-selector").val();
    bm.confirm($.i18n.prop("confirm.delete.tax.rule.dissociate.profile", [data.name]), function () {
        bm.ajax({
            url: app.baseUrl + "taxAdmin/detachRule",
            data: {id: profileId, ruleId: data.id},
            success: function () {
                _self.reload()
            }
        })
    }, function () {});
};
//endregion

//region Rule editor
app.tabs.tax.rule_editor = function (body, parentTab) {
    var _self = this;
    _self.parent = parentTab;
    _self.body = body;
    body.find(".add-code").on("click", function () {
        _self.addCode($(this))
    });
    body.find(".add-zone").on("click", function () {
        _self.addZone($(this))
    });
    var codeTable = body.find(".code-table");
    var zoneTable = body.find(".zone-table");
    _self.initCodeTable(codeTable);
    _self.initZoneTable(zoneTable);
};
var _e = app.tabs.tax.rule_editor.prototype;


_e.initCodeTable = function(codeTable) {
    var _self = this;
    bm.menu([
        {
            text: $.i18n.prop("remove"),
            ui_class: "remove",
            action: "remove"
        },
        {
            text: $.i18n.prop("edit"),
            ui_class: "edit",
            action: "edit"
        },
    ], codeTable, ".action-navigator", {
        click: function (action, entity) {
            var data = entity.config("entity");
            switch (action) {
                case "remove":
                    _self.detachCode(data.rule_id, data.name);
                    break;
                case "edit":
                    var appendBeforeRow = entity.closest("tr");
                    _self.editCode(data, appendBeforeRow);
                    break;
            }
        }
    }, "click");
};

_e.addCode = function($this) {
    var _self = this, body = _self.body, panel = body.find(".right-panel"), ruleDetails = body.find(".rule-details");
    var ruleId = ruleDetails.find("input.rule-id").val(), url = "taxAdmin/addCode";
    bm.selectionFloatingPanel($this, app.baseUrl + url, {}, {
        width: 400,
        clazz: "add-code-popup tax",
        events: {
            content_loaded: function(popup) {
                _self.codeSelectionFloatingPanelEvent(popup, ruleId)
            }
        },
        onSelect: function (data) {
            bm.ajax({
                url: app.baseUrl + "taxAdmin/selectCode",
                data: {codeId: data["id"], ruleId: ruleId},
                success: function(resp) {
                    _self.parent.reload(true);
                }
            })
        }
    })
};

_e.codeSelectionFloatingPanelEvent = function (popup, ruleId) {
    var _self = this, body = _self.body, element = popup.el, tableBody = body.find(".code-table"), newCreateBtn = element.find(".create-new-code");
    newCreateBtn.on("click", function() {
        var appendBeforeRow = tableBody.find(".row.rate-row");
        appendBeforeRow = appendBeforeRow.length ? appendBeforeRow :  tableBody.find("tr.add-code-row");
        _self.editCode({"tax-rule-id": ruleId}, appendBeforeRow);
        body.find(".add-code-row").addClass("hidden");
        popup.close();
    });
};

_e.editCode = function(data, codeRow) {
    var _self = this;
    var body = _self.body;
    body.find(".add-code-form .cancel-button").trigger("click");
    var detailsPanel = body.find(".rule-details");
    body.loader();
    data = data ? data : {};
    bm.ajax({
        url: app.baseUrl + "taxAdmin/editTaxCode",
        dataType: "html",
        data: data,
        response: function() {
            body.loader(false);
        },
        success: function(resp) {
            resp = $(resp);
            var form = resp.find("form");
            form.find(".cancel-button").on("click", function() {
                if(codeRow) {
                    codeRow.removeClass("hidden")
                }
                $(this).closest(".new-code-row").remove()
            });
            if(codeRow){
                codeRow.addClass("hidden");
                resp.insertBefore(codeRow);
            } else {
                resp.insertAfter(_self.body.find("table tr:last"))
            }
            form.form({
                ajax: true,
                preSubmit: function(ajaxSetting) {
                    $.extend(ajaxSetting, {
                        success: function(resp) {
                            _self.parent.reload(true);
                        }
                    })
                }
            });
        }
    })
};

_e.detachCode = function (ruleId, name) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.tax.code", [name]), function() {
        bm.ajax({
            url: app.baseUrl + "taxAdmin/detachCode",
            data: {ruleId: ruleId},
            success: function () {
                _self.parent.reload(true);
            }
        })
    });
};

_e.initZoneTable = function(zoneTable) {
    var _self = this;
    bm.menu([
        {
            text: $.i18n.prop("remove"),
            ui_class: "remove",
            action: "remove"
        }
    ], zoneTable, ".action-navigator", {
        click: function (action, entity) {
            var data = entity.config("entity");
            switch (action) {
                case "remove":
                    _self.detachZone(data);
                    break;
            }
        }
    }, "click");
};

_e.addZone = function($this) {
    var _self = this, body = _self.body, ruleId = body.find(".rule-details input.rule-id").val();
    bm.selectionFloatingPanel($this, app.baseUrl + "taxAdmin/addZonePop", {ruleId: ruleId}, {
        width: 350,
        clazz: "add-zone-popup tax",
        events: {
            content_loaded: function(popup) {
                popup.el.find(".create-zone").on("click", function() {
                    ComponentManager.openTab(ComponentManager.getRibbonData(app.ribbons.administration, "zone"));
                    popup.close()
                });
            }
        },
        beforeReload: function (data) {
            $.extend(data, {ruleId: ruleId})
        },
        onSelect: function (data) {
            bm.ajax({
                url: app.baseUrl + "taxAdmin/selectZone",
                data: {zoneId: data.id, ruleId: ruleId},
                dataType: "html",
                success: function(resp) {
                    _self.parent.reload(true);
                }
            })
        }
    });
};

_e.detachZone = function (data) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.zone", [data["zone_name"]]), function() {
        bm.ajax({
            url: app.baseUrl + "taxAdmin/detachZone",
            data: data,
            success: function () {
                _self.parent.reload(true);
            }
        })
    })
};

//endregion

//region all rule view
app.tabs.tax.ruleView = function(panel, appTab, ajaxUrl) {
    this.body = panel;
    this.appTab = appTab;
    this.ajax_url = ajaxUrl;
    this.init();
};

var _rl = app.tabs.tax.ruleView.inherit(app.SingleTableView);

_rl.menu_entries = [
    {
        text: $.i18n.prop("edit"),
        ui_class: "edit"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "remove"
    }
];

_rl.init = function () {
    app.tabs.tax.ruleView._super.init.call(this);
    var ruleCreateBtn = this.appTab.body.find(".create-rule-top");
    ruleCreateBtn.show();
    this.appTab.body.find(".create-code-top").hide();
};

_rl.onActionClick = function(action, data) {
    switch (action) {
        case "edit" :
            this.edit(data.id, data.name);
            break;
        case "remove":
            this.delete(data.id, data.name);
            break;
    }
};

_rl.edit = _t.ruleInfoEdit;

_rl.delete = function(id, name) {
    var _self = this;
    bm.remove("taxRule", $.i18n.prop("tax.code"), $.i18n.prop("confirm.delete", [name]), app.baseUrl + "taxAdmin/deleteRule", id,{
        is_final: true,
        success: function () {
            _self.profileId = _self.appTab.body.find(".tax-profile-selector").val();
            _self.appTab.reload()
        }
    })
};
//endregion

//region all code view
app.tabs.tax.codeView = function(panel, appTab, ajaxUrl) {
    this.body = panel;
    this.appTab = appTab;
    this.parent = appTab;
    this.ajax_url = ajaxUrl;
    this.init();
};

var _cd = app.tabs.tax.codeView.inherit(app.SingleTableView);

_cd.menu_entries = [
    {
        text: $.i18n.prop("edit"),
        ui_class: "edit"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "remove"
    }
];

_cd.init = function () {
    var _self = this
    app.tabs.tax.codeView._super.init.call(this);
    var codeCreateBtn = this.appTab.body.find(".create-code-top");
    codeCreateBtn.show();
    this.appTab.body.find(".create-rule-top").hide();
    codeCreateBtn.on("click", function () {
        _self.edit({})
    })
};

_cd.onActionClick = function(action, data, tableRow) {
    switch (action) {
        case "edit" :
            this.edit(data, tableRow.closest("tr"));
            break;
        case "remove":
            this.delete(data.id, data.name);
            break;
    }
};

_cd.edit = _e.editCode

_cd.delete = function(id, name) {
    var _self = this;
    bm.remove("taxCode", $.i18n.prop("tax.code"), $.i18n.prop("confirm.delete", [name]), app.baseUrl + "taxAdmin/deleteTaxCode", id,{
        is_final: true,
        success: function () {
            _self.profileId = _self.appTab.body.find(".tax-profile-selector").val();
            _self.appTab.reload()
        }
    })
};
//endregion
