app.tabs.myob = function () {
    this.text = $.i18n.prop("myob");
    this.tip = $.i18n.prop("manage.myob");
    this.ui_class = "myob";
    this.ajax_url = app.baseUrl + "myob/loadAppView";
    this.tab_objs = {}
    app.tabs.myob._super.constructor.apply(this, arguments);
};

app.ribbons.web_commerce.push({
    text: $.i18n.prop("myob"),
    processor: app.tabs.myob,
    ui_class: "myob",
    license: "allow_myob_feature",
    ecommerce: true
});

var _m = app.tabs.myob.inherit(app.MultiTab);

app.tabs.myob.status_viewer = {
    items: ["tax", "product", "customer", "paymentAccount", "order", "total"],
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
            this.items.each(function() {
                var it = this
                popup.find("." + it + "-progress .progress-bar .completed").addClass(resp.status);
            })

            popup.find(".product-progress .progress-bar .completed").addClass(resp.status);
            popup.find(".total-progress .progress-bar .completed").addClass(resp.status);
        }
        if (resp.totalProgress == 100) {
            this.activeLogSummaryLink(popup, resp.token)
            if(popup.find(".button.download-button").length == 0) {
                popup.find('.content').append('<div class="button-line">' +
                    '<button type="button" class="button close-button">' + $.i18n.prop('close') + '</button>' + ' &nbsp; ' +
                    '<a type="button" target="_blank" class="button download-button" href="' + app.baseUrl + 'myob/download?token=' + resp.token + '">' + $.i18n.prop('download') + '</a>' +
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
            url: app.baseUrl + "myob/" + type + "LogSummary",
            dataType: "html",
            data: {token: token},
            success: function (resp) {
                _popup.find(".log-summary").html(resp).updateUi();
            }
        });
    }
};

_m.action_menu_entries = [
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
        text: $.i18n.prop("myob.config"),
        ui_class: "myob-config config",
        action: "myob-config"
    }
];

_m.onActionMenuClick = function(action) {
    switch (action) {
        case "import":
            app.tabs.myob.initImport();
            break;
        case "export":
            app.tabs.myob.initExport();
            break;
        case "myob-config":
            ComponentManager.openTab(ComponentManager.getRibbonData(app.ribbons.administration, "settings"), {active: "myob"});
            break;
    }
}

app.tabs.myob.initImport = function() {
    bm.editPopup(app.baseUrl + "myob/beforeImport", $.i18n.prop("myob.import"), "", {}, {
        success: function(resp) {
            var data = {token: resp.token, name: resp.name, detail_url: app.baseUrl + "myob/progressView", detail_status_url: app.baseUrl + "myob/progressStatus", detail_viewer: app.tabs.myob.status_viewer}
            TaskManager.createTask(data);
            bm.taskPopup(app.baseUrl + "myob/progressView", data, {width: 800});
        }
    })
};

app.tabs.myob.initExport = function() {
    bm.editPopup(app.baseUrl + "myob/beforeExport", $.i18n.prop("myob.export"), "", {}, {
        success: function(resp){
            var data = {token: resp.token, name: resp.name, detail_url: app.baseUrl + "myob/progressView", detail_status_url: app.baseUrl + "myob/progressStatus", detail_viewer: app.tabs.myob.status_viewer}
            bm.taskPopup(app.baseUrl + "myob/progressView", data, {width: 800});
        }
    })
};

_m.onContentLoad = function (data) {
    var index = data.index.capitalize();
    if (this["init" + index + "Settings"]) {
        this["init" + index + "Settings"](data);
    }
    data.panel.find("form").form({
        ajax: {
            success: function () {
                data.panel.clearDirty();
                if(data.success) {
                    data.success()
                }
                data.panel.reload()
            }
        }
    });
}

var _s = app.tabs.setting.prototype;

(function() {
    var width = 750,
        height = 550;
    var options = {
        width: width,
        height: height,
        left: screen.width/2 - width/2,
        top: screen.height/2 -height/2,
        channelmode: 0,
        location: 0,
        menubar: 0,
        status: 0,
        toolbar: 0
    };

    function authorizeMyob() {
        var invoker = $(this),
            auth_url = invoker.attr("url"),
            opts = "";
        for(var entry in options){
            if(opts!=""){
                opts += ","
            }
            opts += entry + "=" + options[entry]
        }
        bm.ajax({
            url: "https://secure.myob.com/oauth2/Account/LogOff",
            complete: function() {
                bm.ajax({
                    url: auth_url,
                    success : function(resp) {
                        window.open(resp.url, $.i18n.prop("authorize.myob.access"), opts)
                    }
                });
            }
        });
    }

    _s.initMyobSettings = function(data) {
        this.on_global("myob-auth-state-change", function() {
            data.panel.reload();
        })
        data.panel.find(".authorize").on("click", function() {
            authorizeMyob.call(this);
        })
        data.panel.find(".authorize").on("click", function() {
            authorizeMyob.call(this);
        })
    }
}())

