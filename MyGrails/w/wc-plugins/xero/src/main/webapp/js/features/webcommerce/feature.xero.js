/**
 * Created by sajed on 6/11/2014.
 */
app.tabs.xero = function (configs) {
    this.text = $.i18n.prop("xero");
    this.tip = $.i18n.prop("manage.xero");
    this.ui_class = "xero";
    this.ajax_url = app.baseUrl + "xero/loadAppView";
    app.tabs.xero._super.constructor.apply(this, arguments);
}

app.ribbons.web_commerce.push({
    text: $.i18n.prop("xero"),
    processor: app.tabs.xero,
    ui_class: "xero",
    license: "allow_xero_feature"
})

app.tabs.xero.inherit(app.MultiTab);

var _x = app.tabs.xero.prototype;
(function(){
    _x.init = function(){
        this.body.find(".tool-icon.xero-config").on("click", function () {
            ComponentManager.openTab(ComponentManager.getRibbonData(app.ribbons.administration, "setting"), {active: "xero"})
        });
        app.tabs.xero._super.init.call(this);
    }

}());

_x.onActionMenuClick = function(action) {
    switch (action) {
        case "import":
            app.tabs.xero.initImport();
            break;
        case "export":
            app.tabs.xero.initExport();
            break;
        case "xero-config":
            ComponentManager.openTab(ComponentManager.getRibbonData(app.ribbons.administration, "settings"), {active: "xero"});
            break;
    }
}

app.tabs.xero.initImport = function() {
    bm.editPopup(app.baseUrl + "xero/beforeImport", $.i18n.prop("xero.import"), "", {}, {
        success: function(resp) {
            var data = {token: resp.token, name: resp.name, detail_url: app.baseUrl + "xero/progressView", detail_status_url: app.baseUrl + "xero/progressStatus", detail_viewer: app.tabs.xero.status_viewer}
            TaskManager.createTask(data);
            bm.taskPopup(app.baseUrl + "xero/progressView", data, {width: 800});
        }
    })
};

app.tabs.xero.initExport = function() {
    bm.editPopup(app.baseUrl + "xero/beforeExport", $.i18n.prop("xero.export"), "", {}, {
        success: function(resp){
            var data = {token: resp.token, name: resp.name, detail_url: app.baseUrl + "xero/progressView", detail_status_url: app.baseUrl + "xero/progressStatus", detail_viewer: app.tabs.xero.status_viewer}
            bm.taskPopup(app.baseUrl + "xero/progressView", data, {width: 800});
        }
    })
};

_x.action_menu_entries = [
    {
        text: $.i18n.prop("import"),
        ui_class: "import",
        action: "import"
    },
    {
        text: $.i18n.prop("export"),
        ui_class: "export",
        action: "export"
    },
    {
        text: $.i18n.prop("xero.config"),
        ui_class: "xero-config config",
        action: "xero-config"
    }
];

_x.initProduct = function(data) {
    var _self = this
    data.panel.find(".tool-icon.import").on("click", function () {
        _self.initImportExport("product", "import");
    });
    data.panel.find(".tool-icon.export").on("click", function () {
        _self.initImportExport("product", "export");
    });
    data.panel.find(".tool-icon.xero-config").on("click", function () {
        ComponentManager.openTab(ComponentManager.getRibbonData(app.ribbons.administration, "setting"), {active: "xero"})
    });
}

_x.onContentLoad = function (data) {
    var index = data.index.capitalize();
    if (this["init" + index]) {
        this["init" + index](data);
    }
    data.panel.find("form").form({
        ajax: {
            success: function () {
                data.panel.clearDirty();
                if(data.success) {
                    data.success()
                }
            }
        }
    });
}

_x.afterTableReload = function(data) {
    var index = data.index.capitalize();
    if (this["after" + index + "Reload"]) {
        this["after" + index + "Reload"](data);
    }
}

/*_x.initImportExport = function(type, operation) {
    bm.ajax({
        url: app.baseUrl + "xero/initImportExport",
        dataType: 'json',
        data: {type: type, operation: operation},
        success: function(resp) {
            var data = {
                token: resp.token,
                name: resp.name,
                detail_url: app.baseUrl + "xero/progressView",
                detail_status_url: app.baseUrl + "xero/progressStatus",
                detail_viewer: app.tabs.xero.status_viewer
            }
            TaskManager.createTask(data);
            bm.taskPopup(app.baseUrl + "xero/progressView", data, {width: 800});
        }
    });
}*/

app.tabs.xero.status_viewer = {
    items: ["tax", "product", "customer", "order", "total"],
    init: function(_popup){
        var _self = this;
        var popup = _popup.getDom();
        this.items.every(function() {
            var it = this;
            _self[it + "ProgressBar"] = new ProgressBar(popup.find("." + it + "-progress"));
            _self[it + "ProgressBar"].render()
        })
    },
    update: function(_popup, resp){
        var _self = this;
        var popup = _popup.getDom();
        this.items.every(function(){
            var it = this;
            _self[it + "ProgressBar"].setPosition(resp[it + "Progress"]);
            popup.find("." + it + "-progress-count").text(resp[it + "Progress"] + "%");
            popup.find("." + it + "-record-complete").text(resp[it + "Complete"]);
            popup.find("." + it + "-record-total").text(resp["total" + it.capitalize() + "Count"]);
            popup.find("." + it + "-success-count").text(resp[it + "SuccessCount"]);
            popup.find("." + it + "-warning-count").text(resp[it + "WarningCount"]);
            popup.find("." + it + "-error-count").text(resp[it + "ErrorCount"]);
        })
        popup.find(".total-progress-count").text(resp.totalProgress + "%");
        popup.find(".total-record-complete").text(resp.recordComplete);
        popup.find(".total-record-total").text(resp.totalRecord);
        popup.find(".total-success-count").text(resp.totalSuccessCount);
        popup.find(".total-warning-count").text(resp.totalWarningCount);
        popup.find(".total-error-count").text(resp.totalErrorCount);
        if(resp.status == "aborted") {
            popup.find(".operation-aborted").show();
            popup.find(".total-progress .progress-bar .completed").addClass(resp.status);
        }
        if (resp.totalProgress == 100) {
            this.activeLogSummaryLink(popup, resp.token)
            if(popup.find(".button.download-button").length == 0) {
                popup.find('.content').append('<div class="button-line">' +
                    '<button type="button" class="button close-button">' + $.i18n.prop('close') + '</button>' + ' &nbsp; ' +
                    '<a type="button" target="_blank" class="button download-button" href="' + app.baseUrl + 'xero/download?token=' + resp.token + '">' + $.i18n.prop('download') + '</a>' +
                    '</div>');
            }

            popup.find(".close-button").click(function() {
                _popup.close();
            });
        }

    },
    activeLogSummaryLink: function(_popup, token){
        var _self = this;
        _popup.find(".total-success-count").addClass('link').click(function () {
            _self.fetchLogSummary(_popup, token, "success")
        });
        _popup.find(".total-warning-count").addClass('link').click(function () {
            _self.fetchLogSummary(_popup, token, "warning")
        });
        _popup.find(".total-error-count").addClass('link').click(function () {
            _self.fetchLogSummary(_popup, token, "error")
        })

    },
    fetchLogSummary: function(_popup, token, type){
        bm.ajax({
            url: app.baseUrl + "xero/" + type + "LogSummary",
            dataType: "html",
            data: {token: token},
            success: function (resp) {
                _popup.find(".log-summary").html(resp).updateUi();
            }
        });
    }
};