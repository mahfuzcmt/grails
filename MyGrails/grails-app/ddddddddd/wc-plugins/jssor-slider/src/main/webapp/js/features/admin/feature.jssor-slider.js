bm.onReady(app.tabs, "album", function() {
    app.global_event.on("album-image-property-loaded", function(evt, albumProto, popup, imageId) {
        var tabTitle = popup.find("[data-tabify-tab-id=jssor-slider]");
        var uuid = bm.getUUID();
        tabTitle.on("click." + uuid, function() {
            var tab = new app.tabs.jssorCaption(popup.find("#bmui-tab-jssor-slider"), albumProto, app.baseUrl + "jssorSlider/loadCaption", imageId)
            tab.init();
            tabTitle.off("click." + uuid);
        })
    });
    app.tabs.jssorCaption = function(panel, appTab, ajaxUrl, imageId) {
        this.toolbar = panel.find(".toolbar");
        this.imageId = imageId;
        this.body = panel;
        this.appTab = appTab;
        this.ajax_url = ajaxUrl;
        app.tabs.jssorCaption._super.constructor.call(this, arguments);
    };
});

$(function() {
    var _jt = app.tabs.jssorCaption.inherit(app.SingleTableTab);
    var _super = app.tabs.jssorCaption._super;

    function attachEvent() {
        var _self = this;
        _self.body.find(".toolbar .create").on("click", function() {
            _self.edit();
        })
    }

    _jt.init = function() {
        var _self = this;
        _super.init.call(this);
        app.tabs.jssorCaption.tab = this;
        attachEvent.call(this);
    }

    _jt.menu_entries = [
        {
            text: $.i18n.prop("edit"),
            ui_class: "edit"
        },
        {
            text: $.i18n.prop("remove"),
            ui_class: "delete"
        }
    ];

    _jt.onActionClick = function(action, data) {
        switch (action) {
            case "edit":
                this.edit(data.id)
                break;
            case "delete":
                this.remove(data.id)
                break;
        }
    };

    _jt.edit = function(id) {
        var _self = this;
        var title = id ? "edit.caption" : "create.caption";
        _self.body.loader();
        _self.appTab.renderCreatePanel(app.baseUrl + "jssorSlider/edit", $.i18n.prop(title), undefined, {id: id, imageId: _self.imageId}, {
            success: function () {
                _self.reload();
            },
            content_loaded: function(form) {
                _self.body.loader(false);
            }
        });
    }

    _jt.remove = function(id) {
        var _self = this;
        bm.remove("jssorSliderCaption", "JssorSliderCaption", $.i18n.prop("confirm.remove.caption", undefined), app.baseUrl + "jssorSlider/remove", id, {
            success: function () {
                _self.reload();
            }
        });
    }

    _jt.beforeReloadRequest = function (param) {
        $.extend(param, {imageId: this.imageId})
        _super.beforeReloadRequest.call(this, param);
    }

});