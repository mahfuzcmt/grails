 bm.onReady(app.tabs, "productBulkEditor", function () {
    var _panel
    var appTab

    app.tabs.productBulkEditor.Advanced = function (_this, parentTab) {
        _panel = _this
        appTab = parentTab
    }

    var _ea = app.tabs.productBulkEditor.Advanced.prototype;

    _ea.reload = function () {
        this.tabulator.reload();
    }

    _ea.afterTableReload = function () {
        _panel.clearDirty();
    }

    _ea.init = function () {
        var _self = this;
        appTab.body.find(".toolbar-item.reload").on("click", function () {
            _self.reload();
        });
        app.global_event.on("product-bulk-updated-advanced", function () {
            _self.reload();
        });
        this.tabulator = bm.table($(_panel.find(".product-bulk-edit-tab.advanced-table")), {
            url: app.baseUrl + "productAdmin/loadProductBulkProperties",
            beforeReloadRequest: function () {
                _self.beforeReloadRequest(this.param)
            },
            afterLoad: function () {
                _panel.tool.filter(".action-header:visible").hide();
                _self.table.tscrollbar("content");
                if (_self.afterTableReload) {
                    _self.afterTableReload()
                }
            },
            selectableCellSelect: app.tabs.productBulkEditor.selectableCellSelect,
            afterCellSelect: app.tabs.productBulkEditor.afterCellSelect,
            afterCellEdit: app.tabs.productBulkEditor.afterCellEdit
        });

        _self.beforeReloadRequest = function (param) {
            $.extend(param, {property: "advanced", ids: this.ids});
        };

        this.table = _panel.find(".body table");
    }

});
