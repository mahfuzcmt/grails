app.tabs.favouriteReport = function(config) {
    this.text = $.i18n.prop("favourite.reports");
    this.tip = $.i18n.prop("list.manage.favourite.reports");
    this.ui_class = "favourite-reports";
    this.ajax_url = app.baseUrl + "commanderReporting/loadFavouriteAppView";
    arguments.callee._super.constructor.apply(this, arguments);
}

var _f = app.tabs.favouriteReport.inherit(app.SingleTableTab)

_f.menu_entries = [
    {
        text: $.i18n.prop("edit"),
        ui_class: "edit"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "delete"
    },
    {
        text: $.i18n.prop("load"),
        ui_class: "load"
    }
];

_f.init = function() {
    var _self = this
    this.on_global("favourite-report-created", function() {
        _self.reload();
    })
    app.tabs.favouriteReport._super.init.apply(this, arguments)
}

_f.onActionClick = function(action, data) {
    switch (action) {
        case "edit":
            this.rename(data.id, data.name)
            break;
        case "delete":
            this.deleteReport(data.id, data.name)
            break;
        case "load":
            this.load(data.filters, data.type);
            break;
    }
}

_f.rename = function(reportId, reportName) {
    var _self = this
    bm.editPopup(app.baseUrl + "commanderReporting/addToFavourite", $.i18n.prop("rename.report"), reportName, {id: reportId}, {
        success: function() {
            _self.reload();
        },
        width: 850
    });
}

_f.deleteReport = function(reportId, reportName) {
    var _self = this;
    bm.remove("report", $.i18n.prop("favourite.report"), $.i18n.prop("confirm.delete.report", [reportName]), app.baseUrl + "commanderReporting/deleteReport", reportId, {
        success: function () {
            _self.reload();
        }
    })
}

_f.load = function(filters, type) {
    filters = JSON.parse(filters)
    var processor
    if(type == "tax") {
        processor = "taxes"
    } else {
        processor = type + "s"
    }
    var tab = app.Tab.getTab("tab-advanced-analytics")
    if(tab) {
        tab.setActive()
        tab.ajax_data = filters
        if(tab instanceof app.tabs.enterpriseReporting[processor]) {
            tab.reload()
        } else {
            tab.constructor_args = [{ajax_data: filters}]
            app.Tab.changeView(tab, "enterpriseReporting", processor);
        }
    } else {
        if(!filters.type) {
            tab = new app.tabs.enterpriseReporting[processor]({
                ajax_data: filters
            })
            tab.render()
        }
        tab.setActive()
    }
}