 bm.onReady(app, "editProduct", function() {
    app.editProduct.tabInitFunctions.customfields = function(panel) {
        var editor = new app.productCustomField(panel, this, "product");
        editor.init();
    }
})

 bm.onReady(app, "editCategory", function() {
     app.editCategory.tabInitFuncs.customfields = function(panel) {
         var editor = new app.productCustomField(panel, this, "category")
         editor.init()
     }
 })

app.productCustomField = function(panel, appTab, type) {
    this.tool = panel.tool
    this.body = panel
    this.appTab = appTab
    this.type = type
    this.ajax_url = app.baseUrl + "productCustomField/" + type + "EditorTabView?id=" + appTab[type].id
    app.productCustomField._super.constructor.call(this, arguments)
};

(function() {
    var _c = app.productCustomField.inherit(app.SingleTableView)
    var _super = app.productCustomField._super

    _c.init = function() {
        _super.init.apply(this, arguments)
        var _self = this;

        this.tool.find(".reload").click(function() {
            _self.reload()
        })
        bm.menu(_self.action_menu_entries, _self.appTab.body.find(".action-tool.action-menu"), null, {
            click: $.proxy(_self, "onActionMenuClick")
        }, "click", ["right bottom+7", "right top"]);
    }

    _c.createField = function(id, label) {
        var _self = this;
        var title = id ? $.i18n.prop("edit.field") : $.i18n.prop("create.field")
        var data = {id: id, type: this.type}
        data[this.type + "Id"] = this.appTab[this.type].id
        bm.editPopup(app.baseUrl + "productCustomField/createField", title, label, data, {
            beforeSubmit: function(form) {
                var validation = form.find(".validation-fields-group").serializeObject()["r-validation"];
                if($.isArray(validation)) {
                    validation = validation.filter("!!(''+this)")
                }
                if($.isArray(validation)) {
                    validation = validation.join(" ");
                }
                form.find("[name='validation']").val("" + validation);
            },
            events: {
                content_loaded: function(popup, form) {
                    var maxField = form.find(".maxlength-limit")
                    maxField.on("change", function() {
                        $(this).siblings("input").val("maxlength[" + $(this).val() + "]")
                    })
                    var custom = form.find(".custom-validation")
                    custom.on("change", function() {
                        $(this).siblings("input").val($(this).val())
                    })
                    var cacheField = form.find("[name='validation']");
                    var cacheVal = cacheField.val();
                    if(cacheVal) {
                        cacheVal.split(" ").every(function() {
                            var keyWord = this.toString();
                            if(keyWord.startsWith("maxlength")) {
                                var length = keyWord.substring(10, keyWord.length - 1)
                                form.find(".validation-maxlength").prop("checked", true).trigger("change").siblings("input").val(length)
                                maxField.siblings("input").val("maxlength[" + length + "]")
                            } else if(keyWord.equals("required")) {
                                form.find(".validation-required").prop("checked", true)
                            } else if(form.find(".validation-" + keyWord).length) {
                                form.find(".validation-" + keyWord).radio("val", "" + keyWord)
                            } else if("" + keyWord != "") {
                                var val = form.find(".custom-validation").val()
                                form.find(".validation-custom").prop("checked", true).trigger("change").siblings("input").val(val + keyWord + " ")
                                custom.siblings("input").val(val + keyWord + " ")
                            }
                        })
                    }
                }
            },
            width: 1000,
            success: function() {
                _self.reload()
            }
        })
    }

    _c.editFieldLabel = function() {
        var title = $.i18n.prop("set.group.title")
        var data = {type: this.type}
        data[this.type + "Id"] = this.appTab[this.type].id
        bm.editPopup(app.baseUrl + "productCustomField/edit" + (this.type == 'product' ? 'Product' : 'Category') + "FieldLabel", title, "", data, {})
    }

    _c.sortable = {
        list: {
            "0": "label"
        },
        sorted: "0",
        dir: "up"
    }

    _c.menu_entries = [
        {
            text: $.i18n.prop("edit"),
            ui_class: "edit"
        },
        {
            text: $.i18n.prop("delete"),
            ui_class: "delete"
        }
    ]

    _c.action_menu_entries = [
        {
            text: $.i18n.prop("edit.group.label"),
            ui_class: "edit-label",
            action: "edit-label"
        },
        {
            text: $.i18n.prop("create.field"),
            ui_class: "create-custom-field",
            action: "create-custom-field"
        }
    ];

    _c.onActionMenuClick = function(action) {
        switch (action) {
            case "edit-label":
                this.editFieldLabel();
                break;
            case "create-custom-field":
                this.createField();
                break;
        }
    }

    _c.deleteField = function(id, label) {
        var _self = this;
        bm.remove(this.type + "_custom_field", $.i18n.prop(this.type + ".custom.field"), $.i18n.prop("confirm.delete.custom.field", [label]), app.baseUrl + "productCustomField/" + this.type + "FieldDelete", id, {
            is_final: true,
            success: function () {
                _self.reload();
            }
        })
    }

    _c.onActionClick = function (action, data) {
        switch (action) {
            case "edit":
                this.createField(data.id, data.label);
                break;
            case "delete":
                this.deleteField(data.id, data.label);
                break;
        }
    }
})()