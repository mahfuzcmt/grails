 bm.onReady(app.tabs, "productBulkEditor", function () {
    var _panel
    var appTab

    app.tabs.productBulkEditor.PriceStock = function (_this, parentTab) {
        _panel = _this
        appTab = parentTab
    }

    var _ep = app.tabs.productBulkEditor.PriceStock.prototype;

    _ep.reload = function () {
        this.tabulator.reload();
    }

    _ep.afterTableReload = function () {
        _panel.clearDirty();
        this.attachEvent();
    }

    _ep.attachEvent = function () {
        var _self = this;
        _panel.find(".change-all.on-sale .fake-link").on("click", function () {
            _self.changeAllOnSale($(this));
        })
    }

    _ep.init = function () {
        var _self = this;
        appTab.body.find(".toolbar-item.reload").on("click", function () {
            _self.reload();
        });
        app.global_event.on("product-bulk-updated-priceStock", function () {
            _self.reload();
        });
        this.tabulator = bm.table($(_panel.find(".product-bulk-edit-tab.price-stock-table")), {
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
            $.extend(param, {property: "price-stock", ids: this.ids});
        };

        this.table = _panel.find(".body table");
        _self.attachEvent();
    }

    _ep.changeAllOnSale = function (span) {
        var _self = this;
        var data = _self.saleFormData || {};
        bm.editPopup(app.baseUrl + "productAdmin/loadChangeAllOnSale", $.i18n.prop("change.all.on.sale"), undefined, data, {
            beforeSubmit: function(form, ajaxSettings, popup) {
                data = _self.saleFormData = form.serializeObject();
                var isOnSale = data.isOnSale;
                var salePrice = data.salePrice;
                _self.table.find(".data-row .on-sale").each(function () {
                    var td = $(this);
                    var dispValue = td.find(".disp-value")
                    td.find("input[type=hidden]").val(isOnSale);
                    td.find(".value").html(isOnSale);
                    if (isOnSale == "true") {
                        td.find(".sale-price").val(parseFloat(salePrice).toFixed(2))
                        dispValue.removeClass("false").addClass("true").html(salePrice);
                    } else {
                        dispValue.removeClass("true").addClass("false").html($.i18n.prop("no"));
                    }
                });
                popup.close();
                _panel.setDirty()
                return false
            }
        });
    }

});
