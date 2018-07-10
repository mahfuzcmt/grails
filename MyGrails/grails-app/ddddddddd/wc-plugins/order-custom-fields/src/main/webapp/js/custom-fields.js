var oc_tab;
app.tabs.orderCustomFields = function () {
    oc_tab = this;
    this.text = $.i18n.prop("order.custom.field");
    this.tip = $.i18n.prop("manage.order.custom.field");
    this.ui_class = "order-custom-fields";
    this.ajax_url = app.baseUrl + "customFieldsAdmin/loadAppView";
    this.tab_objs = {}
    app.tabs.orderCustomFields._super.constructor.apply(this, arguments);
};

app.ribbons.web_commerce.push({
    text: $.i18n.prop("order.custom.field"),
    processor: app.tabs.orderCustomFields,
    ui_class: "order-custom-fields",
    license: "allow_order_custom_fields_feature",
    ecommerce: true
});

var _oc = app.tabs.orderCustomFields.inherit(app.SingleTableTab);

_oc.sortable = {
    list: {
        "1": "label"
    },
    sorted: "1",
    dir: "up"
}

_oc.menu_entries = [
    {
        text: $.i18n.prop("edit"),
        ui_class: "edit"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "delete"
    }
]

_oc.onActionClick = function (action, data) {
    switch (action) {
        case "edit":
            this.editField(data.id, data.label);
            break;
        case "delete":
            this.deleteField(data.id, data.label);
            break;
    }
}

_oc.action_menu_entries = [
    {
        text: $.i18n.prop("edit.group.label"),
        ui_class: "edit-label",
        action: "edit-label"
    }
];

_oc.onActionMenuClick = function(action) {
    switch (action) {
        case "edit-label":
            this.editFieldLabel();
            break;
    }
}

_oc.onSelectedActionClick = function (action, selecteds) {
    switch (action) {
        case "remove":
            this.deleteSelectedField(selecteds.collect("id"));
            break;
    }
};

(function () {
    function attachEvents() {
        var _self = this;
        this.on("close", function () {
            app.tabs.orderCustomFields.tab = null;
        });
        this.body.find(".toolbar .create").on("click", function() {
            _self.createField();
        });
    }

    _oc.init = function () {
        app.tabs.orderCustomFields._super.init.call(this);
        app.tabs.orderCustomFields.tab = this
        attachEvents.call(this);
    }
})();

_oc.createField = function(id, label) {
    var _self = this;
    var title = id ? $.i18n.prop("edit.field") : $.i18n.prop("create.field")
    var data = {id: id}
    this.renderCreatePanel(app.baseUrl + "customFieldsAdmin/createField", title, label, data, {
        beforeSubmit: function(form) {
            var validation = form.find(".validation-fields-group").serializeObject()["r-validation"];
            if($.isArray(validation)) {
                validation = validation.filter("!!(''+this)")
                validation = validation.join(" ");
            }
            form.find("[name='validation']").val("" + validation);
        },
        content_loaded: function(form) {
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
                        form.find(".validation-maxlength").prop("checked", true).trigger("change");
                        form.find(".maxlength-limit").val(length)
                        maxField.siblings("input").val("maxlength[" + length + "]")
                    } else if(keyWord.equals("required")) {
                        form.find(".validation-required").prop("checked", true)
                    } else if(form.find(".validation-" + keyWord).length) {
                        form.find(".validation-" + keyWord).radio("val", "" + keyWord)
                    } else if("" + keyWord != "") {
                        var val = form.find(".custom-validation").val()
                        form.find(".validation-custom").prop("checked", true).trigger("change");
                        form.find(".custom-validation").val(val + keyWord + " ")
                        custom.siblings("input").val(val + keyWord + " ")
                    }
                })
            }
        },
        success: function() {
            _self.reload()
        }
    })
}

_oc.editField = _oc.createField;

_oc.editFieldLabel = function() {
    var title = $.i18n.prop("set.group.title")
    bm.editPopup(app.baseUrl + "customFieldsAdmin/editOrderFieldLabel", title, "", {})
}

_oc.deleteField = function(id, label) {
    var _self = this;
    bm.remove("order_custom_fields", $.i18n.prop("order.custom.field"), $.i18n.prop("confirm.delete.custom.field", [label]), app.baseUrl + "customFieldsAdmin/delete", id, {
        is_final: true,
        success: function () {
            _self.reload();
        }
    })
}

_oc.deleteSelectedField = function (selecteds) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.selected.field"), function() {
        bm.ajax({
            url: app.baseUrl + "customFieldsAdmin/deleteSelected",
            data: {ids: selecteds},
            success: function () {
                _self.reload();
                _self.body.find(".action-header").hide();
            }
        })
    }, function () {
    })
}