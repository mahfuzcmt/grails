app.tabs.managePayment = function() {
    app.tabs.managePayment._super.constructor.apply(this, arguments);
    $.extend(this, {
        text: $.i18n.prop("manage.payment"),
        name: $.i18n.prop("order.hash.number", this.order.id),
        tip: $.i18n.prop("order.hash.number", this.order.id),
        ui_class: "edit-payment edit-tab",
        ui_body_class: "manage-payment-info",
        ajax_url: app.baseUrl + "order/managePayment?orderId=" + this.order.id
    });
}

app.tabs.managePayment.table = function(panel, appTab, ajaxUrl) {
    this.body = panel;
    this.appTab = appTab;
    this.ajax_url = ajaxUrl
};

var _t = app.tabs.managePayment.table.inherit(app.SingleTableView);

_t.init = function() {
    var _self = this;
    _self.body.updateUi();
    app.tabs.managePayment.table._super.init.call(this);
    _self.tableEvent(this.body.find(".log-table"))
};

_t.tableEvent = function (detailsTable) {
    var _self = this
    bm.menu(_self.menus, detailsTable, ".action-navigator", {
        click: function(action, entity) {
            var data = entity.config("entity");
            switch (action) {
                case "pay" :
                    _self.pay(data.id, data.orderid)
                    break;
                case "refund" :
                    _self.refund(data.id)
                    break;
            }
        },
        open: _self.onOpen
    }, "click");
}

_t.menus = [
    {
        text: $.i18n.prop("pay"),
        ui_class: "pay",
        action: "pay"
    },
    {
        text: $.i18n.prop("refund"),
        ui_class: "refund",
        action: "refund"
    }
]

_t.onOpen = function(navigator) {
    var menu = $(this)
    if(navigator.is("tr.success span")) {
        menu.find(".menu-item.pay").addClass("disabled");
        menu.find(".menu-item.refund").removeClass("disabled");
    } else {
        menu.find(".menu-item.pay").removeClass("disabled");
        menu.find(".menu-item.refund").addClass("disabled");
    }
}

/*_t.pay = function(id, orderId) {
    var _self = this
    bm.editPopup(app.baseUrl + "order/makePayment", $.i18n.prop("make.payment"), "", {id: id, orderId: orderId}, {
        widget: 850,
        success: function() {
            _self.appTab.reload()
            app.global_event.trigger("payment-added")
        }
    })
}*/

_t.refund = function(id) {
    var _self = this
    bm.confirm($.i18n.prop("this.payment.will.be.marked.as.refunded"),function() {
        bm.ajax({
            url: app.baseUrl + "order/refundPayment",
            data: {id: id},
            success: function(resp) {
                if(resp.reAdjustInventory == true) {
                    bm.confirm($.i18n.prop("do.you.want.adjust.inventory"), function() {
                        bm.ajax({
                            url:  app.baseUrl + "order/reAdjustInventory",
                            data: {orderId: resp.orderId, paymentId: resp.paymentId},
                            success: function() {

                            }
                        });
                    }, function() {})
                }
                _self.appTab.reload()
                app.global_event.trigger("payment-refunded")
            }
        })
    }, function() {
    });
}

app.tabs.managePayment.inherit(app.Tab);

var _mp = app.tabs.managePayment.prototype;

(function(){
    _mp.init = function(){
        app.tabs.managePayment._super.init.call(this);
    }

    _mp.reinit = function() {
        this.bindDetailsTable()
        app.tabs.managePayment._super.reinit.apply(this, arguments)
    }

    _mp.bindDetailsTable = function () {
        var detailsTable = new app.tabs.managePayment.table(this.body.find(".payment-log"), this, app.baseUrl + "order/managePayment?orderId=" + this.order.id)
        detailsTable.init()
    }

    _mp.action_menu_entries = [
        {
            text: $.i18n.prop("add.payment"),
            ui_class: "add-payment",
            action: "add-payment"
        },
        {
            text: $.i18n.prop("send.invoice"),
            ui_class: "send-invoice",
            action: "send-invoice"
        }
    ];

    _mp.onActionMenuClick = function(action) {
        var _self = this;
        switch(action) {
            case "add-payment":
                _self.pay(null, _self.order.id);
                break;
            case "send-invoice":
                bm.ajax({
                    url: app.baseUrl + "order/sendInvoice",
                    data: {orderId: _self.order.id}
                });
                break;
        }
    };

    _mp.onActionMenuOpen = function(navigator) {
        var itemList = [
            {
                key: "order.manage.order",
                class: "send-invoice"
            }
        ];
        app.checkPermission(navigator, itemList);
    }

    _t.pay = _mp.pay = function(id, orderId) {
        var _self = this
        bm.editPopup(app.baseUrl + "order/makePayment", $.i18n.prop("make.payment"), "", {id: id, orderId: orderId}, {
            widget: 850,
            success: function() {
                if(id) {
                    _self.appTab.reload()
                } else {
                    _self.reload()
                }
                app.global_event.trigger("payment-added")
            }
        })
    }
})()