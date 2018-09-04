app.tabs.formData = function() {
    app.tabs.formData._super.constructor.apply(this, arguments);
    $.extend(this, {
        text: $.i18n.prop("submitted.data"),
        name: this.form.name,
        tip: $.i18n.prop("view.form.submitted.data"),
        ui_class: "form-data",
        ajax_url: app.baseUrl + "formAdmin/loadFormData?id=" + this.form.id,
        strict_layout: false
    });
}

app.tabs.formData.inherit(app.SingleTableTab);

var _fd = app.tabs.formData.prototype;

_fd.sortable = {
    list: {
        "1": "submitted"
    },
    sorted: "1",
    dir: "down"
};

_fd.menu_entries = [
    {
        text: $.i18n.prop("view.data"),
        ui_class: "view",
        action: "view"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "delete",
        action: "delete"
    }
];

_fd.onMenuOpen = function() {
    var menu = this.tabulator.menu;
    var item = [
        {
            key: "remove",
            class: "delete"
        }
    ];
};

_fd.onActionClick = function (action, data) {
    var _self = this;
    switch (action) {
        case "view":
            _self.viewData(data.id);
            break;
        case "delete":
            _self.delete(data.id);
            break;
    }
};

_fd.onSelectedActionClick = function (action, data) {
    var _self = this;
    switch (action) {
        case "delete":
            _self.deleteSelected(data);
            break;
    }
};

_fd.viewData = function(id) {
    bm.viewPopup(app.baseUrl + "formAdmin/view", {id: id}, { width: 700 });
};

_fd.deleteSelected = function (data) {
    var _self = this;
    var ids = [];
    data.forEach(function (element) {
        ids.push(element.id);
    });
    bm.confirm($.i18n.prop("confirm.delete.selected.submitted.data", [bm.htmlEncode(name)]), function () {
        bm.ajax({
            url: app.baseUrl + "formAdmin/deleteSubmittedData",
            data: {
                id: ids
            },
            success: function () {
                _self.reload();
            }
        })
    }, function () {
    });
};

_fd.delete = function(id) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.submitted.data", [bm.htmlEncode(name)]), function () {
        bm.ajax({
            url: app.baseUrl + "formAdmin/deleteSubmittedData",
            data: {
                id: id
            },
            success: function () {
                _self.reload();
            }
        })
    }, function () {
    });
};

_fd.advanceSearchUrl = app.baseUrl + "formAdmin/advanceFilterSubmittedForm";
_fd.advanceSearchTitle = $.i18n.prop("submitted.form");