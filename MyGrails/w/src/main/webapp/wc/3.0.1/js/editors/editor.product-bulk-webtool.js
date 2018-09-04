 bm.onReady(app.tabs, "productBulkEditor", function () {
    var _panel
    var appTab

    app.tabs.productBulkEditor.Webtool = function (_this, parentTab) {
        _panel = _this
        appTab = parentTab
    }

    var _es = app.tabs.productBulkEditor.Webtool.prototype;

    _es.reload = function () {
        this.tabulator.reload();
    }

    _es.afterTableReload = function () {
        _panel.clearDirty();
    }

    _es.init = function () {
        var _self = this;
        appTab.body.find(".toolbar-item.reload").on("click", function () {
            _self.reload();
        });
        app.global_event.on("product-bulk-updated-webtool", function () {
            _self.reload();
        });
        this.tabulator = bm.table($(_panel.find(".product-bulk-edit-tab.webtool-table")), {
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
            $.extend(param, {property: "webtool", ids: this.ids});
        };

        this.table = _panel.find(".body table");
    }

});
