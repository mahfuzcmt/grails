/**
 * Created by shahin on 03-Jul-17.
 */
$(function () {
    bm.onReady(app.tabs, "setting", function () {
        app.tabs.setting.prototype.initCurrencyExtension = function (_data) {
            var _self = this;
            var _currencyProto = app.tabs.currency.prototype;
            var form = _data.panel.find("form");
            var table = form.find(".currency-section");
            _self.on_global("currency-update", function () {
                _data.panel.reload();
            });

            var tabulator = bm.table(table, {
                url: app.baseUrl + "currencyAdmin/loadCurrencyForSettings",
                sortable: {"0": "name"},
                sorted: "0",
                sortedDir: "up",
                menu_entries: [
                    {
                        text: $.i18n.prop("edit"),
                        ui_class: "edit",
                        action: "edit"
                    },
                    {
                        text: $.i18n.prop("remove"),
                        ui_class: "remove",
                        action: "remove"
                    },
                    {
                        text: $.i18n.prop("set.base.currency"),
                        ui_class: "set-base-currency",
                        action: "set-base-currency"
                    }
                ]
            });

            tabulator.onActionClick = function (action, data, nav) {
                switch (action) {
                    case "edit":
                        _currencyProto.editCurrency.call(_self, data.id, data.name);
                        break;
                    case "remove":
                        removeCurrency(tabulator, data)
                        break;
                    case "set-base-currency":
                        _currencyProto.setBaseCurrency.call(_self, data.id, data.name);
                        break;
                }
            }

            table.on("click", ".add-currency", function () {
                addCurrency(tabulator, this.jqObject);
            });

            bm.menu([
                {
                    text: $.i18n.prop("edit"),
                    ui_class: "edit",
                    action: "edit"
                }
            ], form.find(".base-currency-section"), ".action-navigator", {
                click: function (action, navigator) {
                    var data = navigator.config("entity");
                    _currencyProto.editCurrency.call(_self, data.id, data.name);
                }
            }, "click", ["right+10 bottom+5", "right top"]);

        }
    })

    function addCurrency(tabulator, relative) {
        bm.floatingPanel(relative, app.baseUrl + 'currencyAdmin/addCurrencyPopup', {}, {
            width: 350,
            position_collison: "none",
            events: {
                content_loaded: function (popup) {
                    var element = popup.el;
                    element.updateUi();
                    element.on("click", ".select-currency", function () {
                        var data = $.extend({invoke: true}, $(this).data())
                        bm.ajax({
                            url: app.baseUrl + "currencyAdmin/changeInvocation",
                            data: data,
                            success: function () {
                                popup.close();
                                tabulator.reload();
                            }
                        });
                    });

                    element.find(".search-currency .icon-search").click(function() {
                        reload();
                    }).prev("input").keypress(function (e) {
                        if(e.which == 13) {
                            e.preventDefault();
                            reload();
                        }
                    });

                    function reload() {
                        element.loader();
                        bm.ajax({
                            url: app.baseUrl + "currencyAdmin/addCurrencyPopup",
                            data: {searchText: element.find(".search-currency .search-text").val()},
                            dataType: "html",
                            response: function () {
                                element.loader(false)
                            },
                            success: function (resp) {
                                element.find("table").replaceWith(resp.jqObject.find("table"));
                            }
                        });
                    }

                    element.on("click", ".cancel-button", function() {
                        popup.close();
                    });
                }
            }
        });
    }

    function removeCurrency(tabulator, data) {
        bm.confirm($.i18n.prop("confirm.remove", ["currency", data.name]), function() {
            bm.ajax({
                url: app.baseUrl + "currencyAdmin/changeInvocation",
                data: data,
                success: function () {
                    tabulator.reload();
                }
            });
        }, function () {
        });
    }
})