app.tabs.enterpriseReporting = function(config) {
    this.text = $.i18n.prop("advanced.analytics");
    this.tip = $.i18n.prop("manage.advance.analytics");
    this.ui_class = "advanced-analytics";
    this.ajax_url = app.baseUrl + "commanderReporting/loadAppView";
    this.filter = {duration: "today"}
    arguments.callee._super.constructor.apply(this, arguments);
}

var _e = app.tabs.enterpriseReporting.inherit(app.Tab)

_e.onSwitchMenuClick = function(type) {
    app.Tab.changeView(this, "enterpriseReporting", type);
}

app.tabs.enterpriseReporting.landing = function() {
    arguments.callee._super.constructor.apply(this, arguments);
}

_e.reinit = function() {
    this.body.find(".app-tab-content-container").scrollbar({
        vertical: {
            offset: -5
        }
    })
    app.tabs.enterpriseReporting._super.reinit.apply(this, arguments)
}

var _l = app.tabs.enterpriseReporting.landing.inherit(app.tabs.enterpriseReporting)

_l.init = function() {
    var _self = this;
    this.body.find(".report-navigator .report-icon, .report-navigator .title").click(function() {
        app.Tab.changeView(_self, "enterpriseReporting", $(this).closest(".report-navigator").attr("view-type"))
    })
    app.tabs.enterpriseReporting.landing._super.init.apply(this, arguments)
}

app.ribbons.report.push({
    text: $.i18n.prop("advanced.analytics"),
    processor: app.tabs.enterpriseReporting.landing,
    ui_class: "advanced-analytics",
    ecommerce: true,
    views: [{ui_class: "real_time", text: $.i18n.prop("realtime.analytics")},
        {ui_class: "products", text: $.i18n.prop("products")},
        {ui_class: "orders", text: $.i18n.prop("orders")},
        {ui_class: "payments", text: $.i18n.prop("payments")},
        {ui_class: "taxes", text: $.i18n.prop("taxes")}]
});

app.tabs.enterpriseReporting.real_time = function() {
    arguments.callee._super.constructor.apply(this, arguments);
    this.ajax_url = app.baseUrl + "commanderReporting/loadRealTimeView";
}

var _r = app.tabs.enterpriseReporting.real_time.inherit(app.tabs.enterpriseReporting)

_r.reinit = function() {
    var _self = this;
    this.body.find(".header .toolbar .filter-report").change(function() {
        _self.ajax_data = {hour: this.value}
        _self.reload()
    })
    app.tabs.enterpriseReporting.real_time._super.reinit.apply(this, arguments)
}

_r.switch_menu_entries = [
    {
        text: $.i18n.prop("product.report"),
        ui_class: "view-switch product-report",
        action: "products"
    },
    {
        text: $.i18n.prop("order.report"),
        ui_class: "view-switch order-report",
        action: "orders"
    },
    {
        text: $.i18n.prop("payment.report"),
        ui_class: "view-switch payment-report",
        action: "payments"
    },
    {
        text: $.i18n.prop("tax.report"),
        ui_class: "view-switch tax-report",
        action: "taxes"
    }
];

app.tabs.enterpriseReporting.commonView = function() {
    arguments.callee._super.constructor.apply(this, arguments);
}

var _c = app.tabs.enterpriseReporting.commonView.inherit(app.tabs.enterpriseReporting)

_c.reinit = function() {
    var _self = this;
    app.tabs.enterpriseReporting.commonView._super.reinit.call(_self)

    _self.body.find(".header .tool-group .add-favourite").click(function () {
        bm.editPopup(app.baseUrl + "commanderReporting/addToFavourite", $.i18n.prop("save.report.as"), undefined, undefined, {
            events: {
                content_loaded: function(popup, form) {
                    form.append($("<input type='hidden' name='filters'>").val(JSON.stringify(_self.ajax_data)))
                    form.append($("<input type='hidden' name='type'>").val(_self.type))
                }
            },
            success: function() {
                app.global_event.trigger("favourite-report-created")
            },
            width: 850
        });
    })

    _self.body.find(".header .tool-group .favourite").click(function () {
        var tabId = "tab-favourite-reports"
        var tab = app.Tab.getTab(tabId)
        if(!tab) {
            tab = new app.tabs.favouriteReport({
                id: tabId
            })
            tab.render()
        }
        tab.setActive()
    })

    _self.body.find(".header .tool-group .export").click(function() {
        bm.editPopup(app.baseUrl + "commanderReporting/export", $.i18n.prop("export.your.report"),name,{id: id}, {
            success: function() {
                _self.reload();
            },
            width: 850
        })
    })

    _self.body.find(".header .toolbar .date-filter").change(function() {
        _self.ajax_data = $.extend(_self.ajax_data, {duration: this.value, xyz: "dfgsdgfs", start: undefined, end: undefined})
        if(this.value == "custom.date.range") {
            _self.body.find(".header .toolbar .datefield-between").show()
        } else {
            _self.reload();
        }
    })

    _self.body.find(".header .toolbar .report-type").change(function() {
        _self.ajax_data = $.extend(_self.ajax_data, {reportCode: this.value, xaxis: null, yaxis: null})
        _self.reload();
    })

    _self.body.find(".header .toolbar .date-range-apply").click(function() {
        var start = _self.body.find(".header .toolbar .datefield-from").val()
        var end = _self.body.find(".header .toolbar .datefield-to").val()
        if(start && end && end > start) {
            _self.ajax_data = $.extend(_self.ajax_data, {duration: 'custom.date.range', start: start, end: end})
            _self.reload();
        }
    })

    _self.body.find(".app-tab-content-container .chart-option").click(function() {
        var popup = bm.floatingPanel($(this), app.baseUrl + _self.chartOptionUrl, _self.ajax_data, {
            width: 550,
            height: 290,
            clazz: "advanced-analytics-chart-options"
        });
        popup.getDom().change(function(ev) {
            var input = $(ev.target)
            var name = input.attr("name")
            var value = input.val()
            _self.ajax_data = _self.ajax_data || {}
            _self.ajax_data[name] = value
            popup.dirty = true
        })
        popup.on("close", function() {
            if(popup.dirty) {
                _self.reload();
            }
        })
    })

    _self.body.find(".app-tab-content-container .table-filter").click(function() {
        var popup = bm.floatingPanel($(this), app.baseUrl + _self.tableFilterUrl, _self.ajax_data, {
            width: 500,
            clazz: "advanced-analytics-filter-options",
            events: {
                content_loaded: function(popup) {
                    var popupEl = $(this);
                    popupEl.updateUi();
                    popupEl.change(function(ev) {
                        var input = $(ev.target)
                        var name = input.attr("name")
                        var value = input.val()
                        _self.ajax_data = _self.ajax_data || {}
                        _self.ajax_data[name] = value
                        popup.dirty = true
                    }).find(".submit-button")
                        .on("click", function() {
                            popup.close();
                        });
                    popup.on("close", function() {
                        if(popup.dirty) {
                            _self.reload();
                        }
                    })
                }
            },
            position_collison: "none"
        })
    })
}

app.tabs.enterpriseReporting.products = function() {
    arguments.callee._super.constructor.apply(this, arguments);
    this.ajax_url = app.baseUrl + "commanderReporting/loadProductReport";
    this.type = "product"
}

var _pr = app.tabs.enterpriseReporting.products.inherit(app.tabs.enterpriseReporting.commonView)

_pr.switch_menu_entries = [
    {
        text: $.i18n.prop("real.time.report"),
        ui_class: "view-switch real-time-report",
        action: "real_time"
    },
    {
        text: $.i18n.prop("order.report"),
        ui_class: "view-switch order-report",
        action: "orders"
    },
    {
        text: $.i18n.prop("tax.report"),
        ui_class: "view-switch tax-report",
        action: "taxes"
    },
    {
        text: $.i18n.prop("payment.report"),
        ui_class: "view-switch payment-report",
        action: "payments"
    }
];

_pr.chartOptionUrl = "commanderReporting/loadChartOptionForProduct"
_pr.tableFilterUrl = "commanderReporting/loadFilterForProduct"

app.tabs.enterpriseReporting.orders = function() {
    arguments.callee._super.constructor.apply(this, arguments);
    this.ajax_url = app.baseUrl + "commanderReporting/loadOrderReport";
    this.type = "order"
}

var _o = app.tabs.enterpriseReporting.orders.inherit(app.tabs.enterpriseReporting.commonView)

_o.switch_menu_entries = [
    {
        text: $.i18n.prop("real.time.report"),
        ui_class: "view-switch real-time-report",
        action: "real_time"
    },
    {
        text: $.i18n.prop("payment.report"),
        ui_class: "view-switch payment-report",
        action: "payments"
    },
    {
        text: $.i18n.prop("tax.report"),
        ui_class: "view-switch tax-report",
        action: "taxes"
    },
    {
        text: $.i18n.prop("product.report"),
        ui_class: "view-switch product-report",
        action: "products"
    }
];

_o.chartOptionUrl = "commanderReporting/loadChartOptionForOrder"
_o.tableFilterUrl = "commanderReporting/loadFilterForOrder"

_o.printOrder = function(id) {
    var data = {id: id};
    bm.editPopup(app.baseUrl + "commanderReporting/print", $.i18n.prop("order.report"), name, data,{
        width: 750,
        events: {
            content_loaded: function () {
                var printPopup = this;
                var previewBlock = $("<span class='print-order-image print-button'></span><iframe scrolling='no' style='width: 100%; border: none; overflow: hidden' class='printBody'></iframe>");
                var viewContent = printPopup.find(".content").html()
                printPopup.find(".content").html(previewBlock)
                var iframeWindow = previewBlock.filter(".printBody")[0].contentWindow;
                iframeWindow.document.open();
                iframeWindow.document.write(viewContent);
                iframeWindow.document.write("<script type='text/javascript'>function printMe() {window.print();}</script>");
                iframeWindow.document.close();

                var body = $(iframeWindow.document.body);
                var setHeight = body.outerHeight(true)
                previewBlock.filter(".printBody").css("height", setHeight);
                printPopup.find(".print-button").click(function() {
                    iframeWindow.focus();
                    iframeWindow.printMe();
                });

                setTimeout(function() {
                    var setHeight = body.outerHeight(true)
                    previewBlock.filter(".printBody").css("height", setHeight);
                }, 1000)
            }
        }
    });
}

app.tabs.enterpriseReporting.payments = function() {
    arguments.callee._super.constructor.apply(this, arguments);
    this.ajax_url = app.baseUrl + "commanderReporting/loadPaymentReport";
    this.type = "payment"
}

var _pa = app.tabs.enterpriseReporting.payments.inherit(app.tabs.enterpriseReporting.commonView)

_pa.switch_menu_entries = [
    {
        text: $.i18n.prop("real.time.report"),
        ui_class: "view-switch real-time-report",
        action: "real_time"
    },
    {
        text: $.i18n.prop("order.report"),
        ui_class: "view-switch order-report",
        action: "orders"
    },
    {
        text: $.i18n.prop("tax.report"),
        ui_class: "view-switch tax-report",
        action: "taxes"
    },
    {
        text: $.i18n.prop("product.report"),
        ui_class: "view-switch product-report",
        action: "products"
    }
];

_pa.chartOptionUrl = "commanderReporting/loadChartOptionForPayment"
_pa.tableFilterUrl = "commanderReporting/loadFilterForPayment"

app.tabs.enterpriseReporting.taxes = function() {
    arguments.callee._super.constructor.apply(this, arguments);
    this.ajax_url = app.baseUrl + "commanderReporting/loadTaxReport";
    this.type = "tax"
}

var _t = app.tabs.enterpriseReporting.taxes.inherit(app.tabs.enterpriseReporting.commonView)

_t.switch_menu_entries = [
    {
        text: $.i18n.prop("real.time.report"),
        ui_class: "view-switch real-time-report",
        action: "real_time"
    },
    {
        text: $.i18n.prop("order.report"),
        ui_class: "view-switch order-report",
        action: "orders"
    },
    {
        text: $.i18n.prop("payment.report"),
        ui_class: "view-switch payment-report",
        action: "payments"

    },
    {
        text: $.i18n.prop("product.report"),
        ui_class: "view-switch product-report",
        action: "products"
    }
];

_t.chartOptionUrl = "commanderReporting/loadChartOptionForTax"
_t.tableFilterUrl = "commanderReporting/loadFilterForTax"
