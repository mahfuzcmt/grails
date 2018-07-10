 bm.onReady(app.tabs, "setting", function() {
    app.tabs.setting.application = function(panel, appTab, ajaxUrl) {
        this.tool = panel.tool
        this.body = panel
        this.appTab = appTab
        this.ajax_url = ajaxUrl
    }

    var _at = app.tabs.setting.application.inherit(app.SingleTableView);
    var _super = app.tabs.setting.application._super;
    _at.init = function() {
        var _self = this;
        _super.init.call(this);
        this.tool.find(".reload").click(function() {
            _self.reload()
        });

        this.tool.find(".create").on("click", function() {
            _self.edit();
        });

    }

    _at.sortable = {};

    _at.menu_entries = [
        {
            text: $.i18n.prop("edit"),
            ui_class: "edit"
        },{
            text: $.i18n.prop("delete"),
            ui_class: "delete"
        }
    ];

    _at.onActionClick = function(action, data) {
        switch (action) {
            case "edit":
                this.edit(data.id)
                break;
            case "delete":
                this.delete(data.id, data.name)
        }
    };

    _at.edit = function(id) {
        var _self = this
        var title = id ? $.i18n.prop("edit.application") : $.i18n.prop("create.application");
        this.appTab.renderCreatePanel(app.baseUrl + "application/edit", title, null, {id: id}, {
            success: function() {
                _self.reload();
            }
        })
    }

    _at.delete = function(id, name) {
        var _self = this;
        bm.remove("oAuthClient", "OAuthClient", $.i18n.prop("confirm.delete.application", [name]), app.baseUrl + "application/delete", id, {
            success: function () {
                _self.reload();
            }
        });
    }

    app.tabs.setting.prototype.initApplicationSettings = function(data) {
        var tab = new app.tabs.setting.application(data.panel, this, app.baseUrl + "application/list");
        tab.init();
    }

});
