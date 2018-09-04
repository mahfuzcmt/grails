 bm.onReady(app.tabs, "setting", function() {
    app.tabs.setting.store = function(panel, appTab, ajaxUrl) {
        this.tool = panel.tool
        this.body = panel
        this.appTab = appTab
        this.ajax_url = ajaxUrl
    }

    var _st = app.tabs.setting.store.inherit(app.SingleTableView);
    var _super = app.tabs.setting.store._super;
    _st.init = function() {
        var _self = this;
        _super.init.call(this);
        this.tool.find(".reload").click(function() {
            _self.reload()
        });

        this.tool.find(".create").on("click", function() {
            _self.edit();
        });

    }

    _st.sortable = {};

    _st.menu_entries = [
        {
            text: $.i18n.prop("edit"),
            ui_class: "edit"
        },{
            text: $.i18n.prop("delete"),
            ui_class: "delete"
        }
    ];

    _st.onActionClick = function(action, data) {
        switch (action) {
            case "edit":
                this.edit(data.id)
                break;
            case "delete":
                this.delete(data.id, data.name)
        }
    };

    _st.edit = function(id) {
        var _self = this
        var title = id ? $.i18n.prop("edit.store") : $.i18n.prop("create.store");
        this.appTab.renderCreatePanel(app.baseUrl + "store/edit", title, null, {id: id}, {
            success: function() {
                _self.reload();
            }
        })
    }

    _st.delete = function(id, name) {
        var _self = this;
        bm.remove("store", "Store", $.i18n.prop("confirm.delete.store", [name]), app.baseUrl + "store/delete", id, {
            success: function () {
                _self.reload();
            }
        });
    }

});
