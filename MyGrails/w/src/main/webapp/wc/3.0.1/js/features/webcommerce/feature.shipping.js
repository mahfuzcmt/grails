//region shipping tab
app.tabs.shipping = function () {
    var _self = this;
    _self.selected = null;
    _self.text = $.i18n.prop("shipping");
    _self.tip = $.i18n.prop("manage.shipping");
    _self.ui_class = "shipping";
    app.tabs.shipping._super.constructor.apply(this, arguments);
    _self.left_panel_url = "shippingAdmin/loadLeftPanel";
    _self.ajax_url = app.baseUrl + "shippingAdmin/loadAppView";
    _self.right_panel_views = {
        "default": {
            ajax_url: "shippingAdmin/explorerView"
        },
        "rule_list": {
            ajax_url: "shippingAdmin/allRuleList",
            processor: app.tabs.shipping.ruleView
        },
        "rate_list": {
            ajax_url: "shippingAdmin/allRateList",
            processor: app.tabs.shipping.rateView
        }
    };
};

var _s = app.tabs.shipping.inherit(app.TwoPanelExplorerTab);

app.ribbons.web_commerce.push({
    text: $.i18n.prop("shipping"),
    ui_class: "shipping",
    processor: app.tabs.shipping,
    permission: "shipping.view.list",
    ecommerce: true
});

_s.menu_entries = [
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

_s.root_selector_menu_entries = [
    {
        ui_class: "edit",
        action: "edit"
    } ,
    {
        ui_class: "remove",
        action: "remove"
    }
];

_s.onRootSelectorActionClick = function (action, data) {
    switch (action) {
        case "edit" :
            this.createProfileForm(data.value, data.text);
            break;
        case "remove":
            this.deleteProfile(data.value, data.text);
            break;
    }
};

_s.deleteProfile = function(id, name) {
    var _self = this;
    bm.remove("taxProfile", "TaxProfile", $.i18n.prop("confirm.delete.shipping.profile", [name]), app.baseUrl + "shippingAdmin/deleteShippingProfile", id, {
        success: function () {
            _self.reload(false);
        },
        is_final: true
    })
};

_s.onActionClick = function (action, data) {
    switch (action) {
        case "edit" :
            this.createRuleForm(data.id, data.name);
            break;
        case "delete":
            this.detachRule(data.id, data.name);
            break;
    }
};

(function () {
    var _super = app.tabs.shipping._super;
    function attachEvents() {
        var _self = this;
        this.body.find(".create-rule-top").on("click", function () {
            _self.addRule()
        })
    }
    _s.init = function () {
        _super.init.call(this);
        attachEvents.call(this);
        this.initRightPanel();
    };
    _s.initLeftPanel = function() {
        var _self = this;
        _super.initLeftPanel.apply(this, arguments);
        this.attachLeftPanel();
    };
})();

_s.attachLeftPanel = function () {
    var _self = this;
    var body = _self.body;
    _self.profileId = null;
    _self.shippingClass = null;
    _self.initSortable();
    var shippingProfileSelector = body.find(".shipping-profile-selector");
    body.find(".create-profile").on("click", function () {
        _self.createProfilePopup()
    });
    shippingProfileSelector.on("change", function () {
        _self.profileId = shippingProfileSelector.val();
        _self.current_right_panel_view = "default";
        _self.reload()
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
            case "rate":
                _self.changeRightPanelView("rate_list");
                break;
            default:
                break;
        }
    });
    body.find(".class-filter-selector").on("change", function () {
        var selected = $(this).val();
        _self.profileId = shippingProfileSelector.val();
        _self.shippingClass = selected;
        _self.current_right_panel_view = "default";
        _self.reload()
    })
};

_s.initSortable = function () {
    var _self = this;
    var body = _self.body;
    var shippingProfileSelector = body.find(".shipping-profile-selector");
    body.find(".sortable-container").sortable({
        containment: 'parent',
        sort: function(ui) {
            var itemList = body.find(".explorer-items .explorer-item");
            var ruleIdList = "";
            $.each(itemList, function (idx, item) {
                ruleIdList += $(item).attr("entity-id") + ","
            });
            bm.ajax({
                url: app.baseUrl + "shippingAdmin/sortRules",
                data: {id: shippingProfileSelector.val(), ruleIdList: ruleIdList}
            })
        }
    });
};

_s.initRightPanel = function() {
    app.tabs.shipping._super.initRightPanel.apply(this, arguments);
    if (this.current_right_panel_view === "default") {
        var _self = this;
        var body = _self.body;
        this.body.find(".create-rule-top").show();
        this.body.find(".create-rate-top").hide();
        var rateTable = body.find(".rate-table");
        var zoneTable = body.find(".zone-table");
        body.find(".add-rate").on("click", function () {
            _self.addRate($(this))
        });
        body.find(".add-zone").on("click", function () {
            _self.addZone($(this))
        });
        _self.rateTableEvent(rateTable);
        _self.zoneTableEvent(zoneTable);
    }
};

_s.beforeLeftPanelReload = function (data) {
    var _self = this;
    $.extend(data, {profileId: _self.profileId, shippingClass: _self.shippingClass});
    if(_self.ruleId && _self.current_right_panel_view == "default") {
        $.extend(data, {selected: _self.ruleId })
        _self.ruleId = null
    }
};

_s.beforeRightPanelReload = function (data) {
    var _self = this;
    $.extend(data, {profileId: _self.profileId});
    _self.profileId = null;
    _self.shippingClass = null
};
//endregion

//region profile section
_s.createProfilePopup = function () {
    var _self = this;
    bm.editPopup(app.baseUrl + 'shippingAdmin/createProfilePopup', $.i18n.prop("add.new.profile"), null , null,  {
        width: 600,
        events: {
            content_loaded: function() {
                var $this = $(this);
                $this.find(".profile-option").on("click", function() {
                    $this.find(".profile-option.selected").removeClass("selected");
                    $(this).addClass("selected")
                })
            }
        },
        beforeSubmit: function (form, data, popup) {
            var type = form.find(".profile-option.selected").attr("type");
            popup.close();
            if(type === "new") {
                _self.createProfileForm();
            } else if(type === "copy") {
                _self.selectExistingProfile()
            }
            return false
        }
    });
};

_s.selectExistingProfile = function () {
    var _self = this;
    bm.editPopup(app.baseUrl + 'shippingAdmin/useExistingProfilePopup', $.i18n.prop("select.shipping.profile"), null , null,  {
        width: 600,
        events: {
            content_loaded: function() {
                var $this = $(this);
                $this.find(".profile-listing").first().addClass("selected");
                $this.find(".profile-listing").on("click", function() {
                    $this.find(".profile-listing.selected").removeClass("selected");
                    $(this).addClass("selected")
                })
            }
        },
        beforeSubmit: function (form, data, popup) {
            var id = form.find(".profile-listing.selected").attr("entity-id");
            var copyType = form.find(".profile-copy-type").val()
            popup.close();
            _self.renameProfile(id, copyType);
            return false
        }
    });
};

_s.renameProfile = function (id, copyType) {
    var _self = this;
    bm.editPopup(app.baseUrl + 'shippingAdmin/renameProfile', $.i18n.prop("rename.profile"), null , {id: id, copyType: copyType},  {
        width: 600,
        success: function(resp) {
            _self.profileId = resp.id
            _self.current_right_panel_view = "default";
            _self.reload(false)
        }
    });
};

_s.createProfileForm = function (id, name) {
    var _self = this;
    var _self = this, title = id ? "edit.shipping.profile" : "add.new.profile";
    bm.editPopup(app.baseUrl + 'shippingAdmin/createProfileForm', $.i18n.prop(title), name , {id: id},  {
        width: 600,
        success: function(resp) {
            _self.profileId = resp.id
            _self.current_right_panel_view = "default";
            _self.reload()
        }
    });
};

_s.copyProfile = function(){
    var _self = this;
    bm.editPopup(app.baseUrl + 'shippingAdmin/createShippingProfileForm', $.i18n.prop("add.new.profile"), null , {},  {
        width: 600,
        success: function() {
            _self.reload(false)
        }
    });
};
//endregion

//region rule section

_s.renameAndAssignRule = function (id, profileId) {
    var _self = this;
    bm.editPopup(app.baseUrl + 'shippingAdmin/renameRule', $.i18n.prop("rename.rule"), null , {id: id, profileId: profileId},  {
        width: 600,
        success: function() {
            _self.reload()
        }
    });
};

_s.addRule = function () {
    var _self = this;
    bm.editPopup(app.baseUrl + 'shippingAdmin/addRulePopup', $.i18n.prop("add.rule"), null , null,  {
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
                _self.createRuleForm();
            } else if (type === "copy") {
                _self.selectExistingRule()
            }
            return false
        }
    });
};

_s.detachRule = function (id, name) {
    var _self = this;
    var selectedId = _self.body.find(".shipping-profile-selector").val();
    bm.confirm($.i18n.prop("confirm.delete", [name]), function() {
        bm.ajax({
            url: app.baseUrl + "shippingAdmin/detachShippingRule",
            data: {ruleId: id, profileId: selectedId},
            success: function () {
                _self.profileId = selectedId;
                _self.reload();
            }
        })
    });
};

_s.createRuleForm = function (id, name) {
    var _self = this;
    var currentView = _self.current_right_panel_view;
    var body = _self.body;
    var title = name? name :  $.i18n.prop("create.shipping.rule");
    var profileId = body.find(".shipping-profile-selector").val();
    var selectedId = currentView === "default" ? profileId: null;
    bm.editPopup(app.baseUrl + 'shippingAdmin/createShippingRuleForm', title, null , {profileId: selectedId, ruleId: id},  {
        width: 600,
        success: function() {
            _self.profileId = profileId;
            _self.ruleId = id
            _self.reload()
        }
    });
};

_s.selectExistingRule = function () {
    var _self = this, body = _self.body, currentView = _self.current_right_panel_view;
    var profileId = body.find(".shipping-profile-selector").val();
    _self.selectedRuleId = null;
    _self.profileId = profileId;
    var tableView;
    bm.editPopup(app.baseUrl + 'shippingAdmin/useExistingRulePopup', $.i18n.prop("select.rule"), null , {},  {
        width: 600,
        events: {
            content_loaded: function() {
                var resp = $(this);
                tableView = new app.tabs.shipping.table(resp, _self, "shippingAdmin/useExistingRulePopup");
                tableView.init();
                tableView.beforeReloadRequest = function (param) {
                    var shippingClass = this.body.find("[name='shippingClass']").val();
                    var policyType = this.body.find("[name='policyType']").val();
                    var searchText = this.body.find("input.search-text").val();
                    $.extend(param, {shippingClass: shippingClass, policyType: policyType, searchText: searchText});
                }
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

_s.selectedRule = function (id) {
    var _self = this;
    bm.editPopup(app.baseUrl + 'shippingAdmin/selectedRule', $.i18n.prop("use.existing.rule"), null , {id: id},  {
        width: 600,
        beforeSubmit: function (form, data, popup) {
            popup.close();
            _self.ruleCopyType();
            return false
        }
    });
};

_s.ruleCopyType = function () {
    var _self = this;
    var body = _self.body;
    var currentView = _self.current_right_panel_view;

    bm.editPopup(app.baseUrl + 'shippingAdmin/ruleCopyType', $.i18n.prop("use.existing.rule"), null , {view: currentView},  {
        width: 600,
        events: {
            content_loaded: function() {
                var resp = $(this);
                resp.find(".copy-type").on("click", function() {
                    resp.find(".copy-type.selected").removeClass("selected");
                    $(this).addClass("selected")
                })
            }
        },
        beforeSubmit: function (form, data, popup) {
            var copyType = form.find(".copy-type.selected").attr("type");
            var profileId = currentView === "default" ? body.find(".shipping-profile-selector").val(): null;
            popup.close();
            if(copyType === "use") {
                bm.ajax({
                    url: app.baseUrl + "shippingAdmin/assignRuleToProfile",
                    data: {id: _self.selectedRuleId, profileId: profileId},
                    success: function () {
                        _self.reload();
                    }
                })
            } else {
                _self.renameAndAssignRule(_self.selectedRuleId, profileId)
            }
            return false
        }
    });
};
//endregion

//region rate section
_s.rateTableEvent = function(rateTable) {
    var _self = this;
    bm.menu([
        {
            text: $.i18n.prop("edit"),
            ui_class: "edit",
            action: "edit-rate"
        },
        {
            text: $.i18n.prop("remove"),
            ui_class: "remove",
            action: "remove"
        }
    ], rateTable, ".action-navigator", {
        click: function (action, entity, navigator) {
            var data = entity.config("entity");
            switch (action) {
                case "edit-rate":
                    var appendBeforeRow = rateTable.find(".row.rate-row");
                    appendBeforeRow = appendBeforeRow.length ? appendBeforeRow :  navigator.closest("tr");
                    _self.editRate(data, appendBeforeRow);
                    break;
                case "remove":
                    _self.detachRate(data["rule_id"], data["name"]);
                    break;
            }
        }
    }, "click");
};

_s.rateSelectionFloatingPanelEvent = function (popup, ruleId) {
    var _self = this, body = _self.body, element = popup.el, tableBody = body.find(".rate-table"), newCreateBtn = element.find(".create-new-rate");
    newCreateBtn.on("click", function() {
        var appendBeforeRow = tableBody.find(".row.rate-row");
        appendBeforeRow = appendBeforeRow.length ? appendBeforeRow :  tableBody.find("tr.add-rate-row");
        _self.editRate({"shipping-rule-id": ruleId}, appendBeforeRow);
        body.find(".add-rate-row").addClass("hidden");
        popup.close();
    });
};

_s.detachRate = function (id, name) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.shipping.rate", [name]), function() {
        bm.ajax({
            url: app.baseUrl + "shippingAdmin/detachRate",
            data: {ruleId: id},
            success: function () {
                _self.reload(true);
            }
        })
    });
};

_s.addRate = function($this) {
    var _self = this, body = _self.body, panel = body.find(".right-panel"), ruleDetails = body.find(".rule-details");
    var ruleId = ruleDetails.find("input.rule-id").val(), url = "shippingAdmin/addRate";
    bm.selectionFloatingPanel($this, app.baseUrl + url, {}, {
        width: 400,
        clazz: "add-rate-popup shipping",
        events: {
            content_loaded: function(popup) {
                _self.rateSelectionFloatingPanelEvent(popup, ruleId)
            }
        },
        onSelect: function (data) {
            bm.ajax({
                url: app.baseUrl + "shippingAdmin/selectRate",
                data: {rateId: data["rate_id"], ruleId: ruleId},
                dataType: "html",
                success: function(resp) {
                    _self.reload(true);
                }
            })
        }
    })
};

_s.editRate = function(data, rateRow) {
    var _self = this;
    var body = _self.body;
    body.find(".add-rate-form .cancel-button").trigger("click");
    var detailsPanel = body.find(".rule-details");
    body.loader();
    data = data ? data : {};
    bm.ajax({
        url: app.baseUrl + "shippingAdmin/loadNewRate",
        dataType: "html",
        data: data,
        response: function() {
            body.loader(false);
        },
        success: function(resp) {
            resp = $(resp);
            var form = resp.find("form");
            form.find(".cancel-button").on("click", function() {
                if(rateRow) {
                    rateRow.removeClass("hidden")
                }
                $(this).closest(".new-rate-row").remove()
            });
            if(rateRow){
                rateRow.addClass("hidden");
                resp.insertBefore(rateRow);
            } else {
                resp.insertAfter(_self.body.find("table tr:last"))
            }
            form.form({
                ajax: true,
                preSubmit: function(ajaxSetting) {
                    $.extend(ajaxSetting, {
                        success: function(resp) {
                            _self.reload(true);
                        }
                    })
                }
            });
            _self.attachRateFormEvents(resp)
        }
    })
};

_s.addShippingConditionRow = function(val, form, tr) {
    var _self = this;
    var precision = app.maxPricePrecision;
    var isWeight = form.find("select.shipping-policy-type").val() == "sbw";
    var lastRow = form.find(".shipping-condition-table .last-row");
    var conditionRow = form.find(".condition-row");
    var newConditionRow;
    if(conditionRow.length > 0) {
        newConditionRow = $(conditionRow[0]).clone();
        setValue()
    } else {
        $.extend(val, {isWeight: isWeight, rateId: form.find("input[name='id']").val(), bulkEdit: form.is(".rate-bulk-edit-form")});
        bm.ajax({
            url: app.baseUrl + "shippingAdmin/conditionRowTemplate",
            data: val,
            dataType: "html",
            success: function(resp) {
                newConditionRow = $(resp);
                setValue()
            }
        })
    }

    function setValue() {
        newConditionRow.find("input[name='from']").val(val.from);
        newConditionRow.find("input[name='to']").val(val.to);
        newConditionRow.find("input[name='shippingCost']").val(val.shippingCost);
        newConditionRow.find("input[name='handlingCost']").val(val.handlingCost);
        newConditionRow.find("input[name='packetWeight']").val(val.packetWeight);
        newConditionRow.find("span.from .value").html(val.from);
        newConditionRow.find("span.to .value").html(val.to);
        newConditionRow.find("span.shippingCost .value").html(val.shippingCost);
        newConditionRow.find("span.handlingCost .value").html(val.handlingCost);
        newConditionRow.find("span.packetWeight .value").html(val.packetWeight);
        if(!isWeight) {
            newConditionRow.find(".weight_only").hide();
        }
        if(tr) {
            tr.replaceWith(newConditionRow);
            lastRow.find(".add-condition").removeClass("edit-mode").html("+ "+$.i18n.prop("add"));
        } else {
            newConditionRow.insertBefore(lastRow)
        }
    }
};

_s.rateFromValidation = function (form) {
    var _self = this
    var multiCondition = form.find(".multi-conditions");
    var panel = multiCondition.find(".shipping-condition-editor");
    var fromField, toField, additional;
    fromField = panel.find("input.from");
    toField = panel.find("input.to");
    additional = multiCondition.find(".additional-amount");
    panel.attachValidator();
    form.find("select.shipping-policy-type").on("change", function() {
        var select = $(this);
        $.each([fromField, toField, additional], function(k, fields) {
            $.each(fields , function (idx, v) {
                var field = $(v)
                var attr = "condition-validation";
                if(field.is(".additional-amount")) {
                    attr = "validation";
                }
                if(select.val() == "sbq") {
                    field.attr("restrict", "numeric");
                    field.addAttrProp(attr, "number", "digits");
                    field.addAttrProp(attr, "price", "");
                } else {
                    field.attr("restrict", "decimal");
                    field.addAttrProp(attr, "digits", "number");
                    field.addAttrProp(attr, "price");
                }
            })
        });

        panel.clearValidationMessage()

    });
};

_s.attachRateFormEvents = function (resp) {
    var _self = this;
    var body = _self.body;
    resp =  $(resp);
    var form = resp.find("form");
    if(form.length == 0)
        form = resp;
    var multiCondition = form.find(".multi-conditions");
    var panel = multiCondition.find(".shipping-condition-editor");
    var fromField, toField, shippingCostField, handlingCostField, packetWeightField, addBtn, editTr, additional;
    fromField = panel.find("input.from");
    toField = panel.find("input.to");
    shippingCostField = panel.find(".shipping-cost");
    handlingCostField = panel.find(".handling-cost");
    packetWeightField = panel.find(".packet-weight");
    addBtn = panel.find(".add-condition");
    additional = multiCondition.find(".additional-amount");

    body.find(".add-rate-row").addClass("hidden");
    resp.updateUi();
    _self.rateFromValidation(form);

    function editCondition($this) {
        editTr = $this.parents("tr.condition-row");
        fromField.val(editTr.find("[name=from]").val());
        toField.val(editTr.find("[name=to]").val());
        shippingCostField.val(editTr.find("[name=shippingCost]").val());
        handlingCostField.val(editTr.find("[name=handlingCost]").val());
        packetWeightField.val(editTr.find("[name=packetWeight]").val());
        addBtn.addClass("edit-mode").html($.i18n.prop("update"));
    }
    addBtn.on("click", function () {
        if (!panel.valid()) {
            var errorObj = ValidationField.validateAs(shippingCostField, shippingCostField.attr("condition-validation")) ||
                ValidationField.validateAs(handlingCostField, shippingCostField.attr("condition-validation"));
            var errorObj2 = ValidationField.validateAs(packetWeightField, shippingCostField.attr("condition-validation"));
            if(errorObj) {
                bm.notify($.i18n.prop(errorObj.msg_template, errorObj.msg_params), "alert");
            } else if(packetWeightField.is(":visible") && errorObj2) {
                bm.notify($.i18n.prop(errorObj2.msg_template, errorObj2.msg_params), "alert");
            }
            return
        }
        var values = {
            from: fromField.val(),
            to: toField.val(),
            shippingCost: shippingCostField.val(),
            handlingCost: handlingCostField.val(),
            packetWeight: packetWeightField.val()
        };
        if ($(this).is(".edit-mode")) {
            _self.addShippingConditionRow(values, form, editTr);
        } else {
            _self.addShippingConditionRow(values, form);
        }
        panel.find(".last-row input").val("");
        //addBtn.trigger("validate"); todo check why this code is here
    });
    if(form.length > 0) {
        bm.menu([
            {
                text: $.i18n.prop("edit"),
                ui_class: "edit",
                action: "edit"
            },
            {
                text: $.i18n.prop("remove"),
                ui_class: "delete",
                action: "delete"
            }
        ], panel, ".action-navigator", {
            hide: function(entity) {
                entity.parent().removeClass("float-menu-opened");
            },
            click: function(action, navigator) {
                switch(action) {
                    case "edit":
                        editCondition(navigator);
                        break;
                    case "delete":
                        navigator.parents(".condition-row").remove();
                        break;
                }
            }
        }, "click", ["center bottom", "right+22 top+7"]);
    }

};
//endregion

//region zone region
_s.zoneTableEvent = function(zoneTable) {
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

_s.detachZone = function (data) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.zone", data["zone_name"]), function() {
        bm.ajax({
            url: app.baseUrl + "shippingAdmin/detachZone",
            data: data,
            success: function () {
                _self.reload(true);
            }
        })
    })
};

_s.addZone = function($this) {
    var _self = this, body = _self.body, ruleId = body.find(".rule-details input.rule-id").val();
    bm.selectionFloatingPanel($this, app.baseUrl + "shippingAdmin/addZone", {ruleId: ruleId}, {
        width: 350,
        clazz: "add-zone-popup shipping",
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
                url: app.baseUrl + "shippingAdmin/selectZone",
                data: {zoneId: data.id, ruleId: ruleId},
                dataType: "html",
                success: function(resp) {
                    _self.reload(true);
                }
            })
        }
    });
};

//endregion

//region table view
app.tabs.shipping.table = function(panel, appTab, ajaxUrl) {
    this.body = panel;
    this.appTab = appTab;
    this.ajax_url = ajaxUrl
};

var _t = app.tabs.shipping.table.inherit(app.SingleTableView);

_t.init = function() {
    var _self = this;
    _self.body.updateUi();
    app.tabs.shipping.table._super.init.call(this);
    _self.bindFilterEvent()
};

_t.bindFilterEvent = function () {
    var _self = this;
    _self.body.find(".filter-select select").on("change", function () {
        _self.reload()
    })
};

_t.advanceFilter = function (fromRate) {
    var _self = this;
    var body = this.body;
    body.find(".advance-filter-btn").on("click", function () {
        _self.advanceSearchFilter = _self.advanceSearchFilter ? _self.advanceSearchFilter : {};
        $.extend(_self.advanceSearchFilter, {ratePanel: fromRate});
        bm.floatingPanel(this, app.baseUrl + "shippingAdmin/advanceFilterPanel", _self.advanceSearchFilter, {
            width: 350,
            height: null,
            clazz: "add-zone-popup shipping",
            position_collison: "none",
            events: {
                content_loaded: function (popup) {
                    var element = popup.el;
                    element.updateUi()
                    var filterBtn = element.find(".filter");
                    element.find(".cancel-button").on("click", function () {
                        popup.close();
                    });
                    filterBtn.on("click", function () {
                        _self.advanceSearchFilter = element.serializeObject();
                        popup.close();
                        _self.reload()
                    });
                }
            }
        })
    })
};

//endregion

//region all rule view
app.tabs.shipping.ruleView = function(panel, appTab, ajaxUrl) {
    this.body = panel;
    this.appTab = appTab;
    this.ajax_url = ajaxUrl;
    this.init();
    this.advanceFilter(false)
};

var _rl = app.tabs.shipping.ruleView.inherit(app.tabs.shipping.table);

_rl.advanceFilter = _t.advanceFilter;

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

_rl.action_menu_entries = [
    {
        text: $.i18n.prop("import"),
        ui_class: "import"
    },
    {
        text: $.i18n.prop("export"),
        ui_class: "export"
    }
];

_rl.init = function () {
    app.tabs.shipping.ruleView._super.init.call(this);
    var ruleCreateBtn = this.appTab.body.find(".create-rule-top");
    ruleCreateBtn.show();
    this.appTab.body.find(".create-rate-top").hide();
    this.bindImport()
};

_rl.onActionClick = function(action, data) {
    switch (action) {
        case "edit" :
            this.createRuleForm(data.id, data.name);
            break;
        case "remove":
            this.deleteRule(data.id, data.name);
            break;
    }
};

_rl.createRuleForm = _s.createRuleForm;

_rl.deleteRule = function(id, name) {
    var _self = this;
    bm.remove("shippingPolicy", $.i18n.prop("shipping.rate"), $.i18n.prop("confirm.delete.shipping.rule", [name]), app.baseUrl + "shippingAdmin/deleteShippingRule", id,{
        is_final: true,
        success: function () {
            _self.profileId = _self.appTab.body.find(".shipping-profile-selector").val();
            _self.appTab.reload()
        }
    })
};
//endregion

// region rule import
_rl.bindImport = function () {
    var _self = this;
    bm.menu(this.action_menu_entries, this.body, ".action-menu", {
        click: function(action, navigator) {
            switch(action) {
                case "import":
                    _self.initImportWindow();
                    break;
                case "export":
                    _self.exportRule();
                    break;
            }
        }
    }, "click", ["center bottom", "right+22 top+7"]);
    app.global_event.on('shipping_import_success', function() {
        _self.reload()
    });
};
_rl.exportRule = function () {
    window.open(app.baseUrl + "shippingRuleImport/export")
};
_rl.initImportWindow = function() {
    var _self = this;
    var form;

    this.appTab.renderCreatePanel(app.baseUrl + "shippingRuleImport/uploadFileView", $.i18n.prop("import.shipping.rule"), '', undefined, {
        width: 220,
        success: function(resp) {
            _self.configImportedDataMap(resp.html)
        },
        content_loaded: function(popup, _form) {
            form = _form;
            var fileObject = form.find("[name=ruleImportFile]");
            fileObject.on("change", function () {
                form.submit();
            });
        },
        beforeSubmit: function(form) {
            form.loader()
        },
        error: function() {
            form.loader(false);
        }
    });
};

_rl.configImportedDataMap = function(resp) {
    var _self = this;
    this.appTab.renderCreatePanel(undefined, $.i18n.prop("import.shipping.rule"), '', undefined, {
        width: 495,
        draggable: false,
        content_loaded: function(popup){
        },
        success: function(resp) {
            app.tabs.setting.prototype.initTask.call(_self, resp.token, resp.name, "shipping_import_success")
        },
        content: resp
    });
};



//endregion

//region all rate view
app.tabs.shipping.rateView = function(panel, appTab, ajaxUrl) {
    var _self = this;
    this.body = panel;
    this.appTab = appTab;
    this.ajax_url = ajaxUrl;
    this.init()
};

var _rt = app.tabs.shipping.rateView.inherit(app.tabs.shipping.table);

_rt.menu_entries = [
    {
        text: $.i18n.prop("edit"),
        ui_class: "edit"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "remove"
    }
];

_rt.init = function () {
    app.tabs.shipping.rateView._super.init.call(this);
    this.bindEvents();
    this.advanceFilter(true)
};

_rt.bindEvents = function () {
    var _self = this;
    var body = this.body;
    this.appTab.body.find(".create-rule-top").hide();
    var rateCreateBtn = this.appTab.body.find(".create-rate-top");
    var bulkEditBtn = body.find(".rate-bulk-edit");
    rateCreateBtn.show();
    rateCreateBtn.on("click", function () {
        _self.editRate({})
    });
    bulkEditBtn.on("click", function () {
        _self.loadBulkEditForm()
    })
};

_rt.loadBulkEditForm = function () {
    var _self = this;
    var body = this.body;
    this.appTab.body.find(".create-rate-top").hide();
    this.appTab.body.find(".rate-table .footer").hide();
    this.appTab.body.find(".filter-group").hide();
    var bulkEditButtons = body.find(".bulk-edit-buttons");
    body.loader();
    var idList = [];
    $.map(_self.getSelectedEntities(), function (entity) {
        idList.push(entity.id)
    });
    bm.ajax({
        url: app.baseUrl + "shippingAdmin/rateBulkEditForm",
        data: {idList: idList},
        dataType: 'html',
        success: function (resp) {
            var dataTable = body.find(".rate-data-list").hide();
            bulkEditButtons.show();
            body.find(".rate-bulk-edit").closest(".tool-group").hide();
            $(resp).insertAfter(dataTable);
            var allRateForms = body.find(".rate-bulk-edit-form");
            _self.bindBulkEvents(bulkEditButtons, allRateForms)
        },
        response: function () {
            body.loader(false)
        }
    });
};

_rt.bindBulkEvents = function (bulkEditButtons, allRateForms) {
    var _self = this
    var body = _self.appTab.body
    var bulkSaveBtn = bulkEditButtons.find(".rate-bulk-save")
    var bulkCancelBtn = bulkEditButtons.find(".rate-bulk-cancel")
    $.each(allRateForms, function (idx, form) {
        bm.autoToggle($(form))
        _self.appTab.attachRateFormEvents($(form))
        $(form).updateValidator()
    });
    bulkSaveBtn.on("click", function () {
        var inputs = allRateForms.find("input:not(.last-row input)")
        var valid = true
        inputs.toArray().every(function (idx, input) {
            if(!$(input).valid()) {
                valid = false
                return false
            }
        })
        if(valid) {
            _self.bulkSave()
        }
    })
    bulkCancelBtn.on("click", function () {
        body.find(".navigation-button.rate-button").trigger("click")
    })
}

_rt.bulkSave = function () {
    var _self = this;
    var rates = [];
    var forms = _self.body.find(".rate-bulk-edit-form");
    var valid = true;
    var _shippingTab = this.appTab;
    $.each(forms, function (idx, form) {
        if (valid && $(form).find(".rate-condition-wrap .shipping-rate-selection").valid() == false) {
            valid = false
        }
        if (valid) {
            var serializeObject = $(form).serializeObject();
            var rateJson = JSON.stringify(serializeObject);
            rates.push(rateJson)
        }
    });
    if (valid) {
        bm.ajax({
            url: app.baseUrl + "shippingAdmin/bulkRateSave",
            data: {rates: rates},
            success: function () {
                _shippingTab.body.find(".navigation-button.rate-button").trigger("click")
            }
        })
    }
};

_rt.advanceFilter = _t.advanceFilter;

_rt.onActionClick = function(action, data, tableRow) {
    switch (action) {
        case "edit" :
            this.editRate(data, tableRow.closest("tr"));
            break;
        case "remove":
            this.deleteRate(data.id, data.name);
            break;
    }
};

_rt.editRate = _s.editRate;

_rt.addShippingConditionRow = _s.addShippingConditionRow;

_rt.deleteRate = function(id, name) {
    var _self = this;
    bm.remove("shippingPolicy", $.i18n.prop("shipping.rate"), $.i18n.prop("confirm.delete.shipping.rate", [name]), app.baseUrl + "shippingAdmin/deleteShippingPolicy", id,{
        is_final: true,
        success: function () {
            _self.reload()
        }
    })
};

_rt.attachRateFormEvents = _s.attachRateFormEvents;

_rt.rateFromValidation = _s.rateFromValidation;
//endregion