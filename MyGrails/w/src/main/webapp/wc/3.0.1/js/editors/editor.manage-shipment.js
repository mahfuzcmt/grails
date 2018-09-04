app.tabs.manageShipment = function() {
    app.tabs.manageShipment._super.constructor.apply(this, arguments);
    this.constructor_args = arguments;
    this.text = $.i18n.prop("manage.shipment");
    this.name = $.i18n.prop("order.hash.number", this.data.order.id),
    this.tip = $.i18n.prop("order.hash.number", this.data.order.id);
    this.ui_class = "manage-shipment edit-tab";
    this.ui_body_class = "manage-shipment-info";
    $.extend(this, {
        orderId: this.data.order.id,
        ajax_url: this.ajax_url + "?orderId=" + this.data.order.id
    });
}
var _ms = app.tabs.manageShipment.inherit(app.Tab);

(function () {
    function attachEvents() {
        var _self = this;
    }
    _ms.init = function () {
        app.tabs.manageShipment._super.init.call(this)
        attachEvents.call(this)
    }
})();

////////////////////////ShipmentInformation////////////////////////////

app.tabs.manageShipment.shipmentInfo = function () {
    $.extend(arguments, {type: 'shipmentInfo'})
    app.tabs.manageShipment.shipmentInfo._super.constructor.apply(this, arguments);
}

var _msi = app.tabs.manageShipment.shipmentInfo.inherit(app.tabs.manageShipment);

(function () {
    function attachEvents() {
        var _self = this;
        var body = _self.body
        body.find(".edit-ship").on("click", function () {
            _self.addShipment($(this).attr("data-shipping-id"), _self.orderId)
        })
        body.find(".show-edit-history").on("click", function () {
            var $this = $(this);
            var data = {shipmentId : $this.attr("data-shipping-id")}
            bm.editPopup(app.baseUrl + 'order/showShipmentHistory', $.i18n.prop("shipment.history"), null , data,  {
                width: 600,
                events: {
                    content_loaded: function() {
                        var $this = $(this);
                        $this.find("[name='changeLabel']").on("change", function () {
                            var selected = $(this).val()
                            $this.loader()
                            bm.ajax({
                                url: app.baseUrl + "order/reloadShipmentHistory",
                                data: {uuid: selected},
                                dataType: "html",
                                response: function () {
                                    $this.loader(false)
                                },
                                success: function(resp) {
                                    $this.find(".history-table").replaceWith(resp)
                                }
                            })
                        })
                    }
                }
            });
        })
    }
    _msi.init = function () {
        app.tabs.manageShipment.shipmentInfo._super.init.call(this);
    }
    _msi.reinit = function () {
        app.tabs.manageShipment.shipmentInfo._super.reinit.call(this);
        attachEvents.call(this);
    }
})();

_msi.switch_menu_entries = [
    {
        text: $.i18n.prop("shipment.details.list"),
        ui_class: "view-switch shipment-details list",
        action: "shipmentDetails"
    }
];

_msi.action_menu_entries = [
    {
        text: $.i18n.prop("add.shipment"),
        ui_class: "add-shipment",
        action: "add-shipment"
    },
    {
        text: $.i18n.prop("send.invoice"),
        ui_class: "send-invoice",
        action: "send-invoice"
    },
    {
        text: $.i18n.prop("print.shipping.label"),
        ui_class: "shipping-level",
        action: "shipping-level"
    },
    {
        text: $.i18n.prop("print.delivery.docket"),
        ui_class: "delivery-docket",
        action: "delivery-docket"
    }
];
_msi.onActionMenuClick = function(action) {
    var _self = this;
    switch (action) {
        case "add-shipment":
            _self.addShipment(null, _self.orderId)
            break;
        case "send-invoice":
            bm.ajax({
                url: app.baseUrl + "order/sendInvoice",
                data: {orderId: _self.orderId}
            })
            break;
        case "shipping-level":
            _self.printShippingLevel(_self.orderId, false);
            break;
        case "delivery-docket":
            _self.printDeliveryDocket(_self.orderId, false);
            break;

    }
}
_msi.addShipment = function (id, orderId) {
    var _self = this
    bm.editPopup(app.baseUrl + "order/addShipment", $.i18n.prop("add.shipment"), "", {shippingId: id, orderId: orderId}, {
        width: 850,
        success: function() {
            _self.reload()
            app.global_event.trigger("shipment-added")
        }
    });
}

_msi.printShippingLevel = function(id, isView) {
    var data = {id: id, view: isView, type: "shipping"}, title = "print.shipping.level"
    var docH = function() {
        return window.innerHeight;
    };
    var uuid = bm.getUUID();
    bm.ajax({
        url: app.baseUrl + "layout/isAdminLoggedIn",
        success: function() {
            bm.editPopup(app.baseUrl+"document/getDocumentData", $.i18n.prop(title), name, data, {
                width: 770,
                height: docH() - docH()/100 * 20,
                scroll: false,
                events: {
                    content_loaded: function (popup) {
                        var printPopup = this;
                        var iframeWindow = printPopup.find(".printBody")[0].contentWindow;
                        bm.onReady(iframeWindow, "printMe", function() {
                            printPopup.find(".print-button").click(function() {
                                iframeWindow.focus();
                                iframeWindow.printMe();
                            });
                        })
                        printPopup.find(".content").css("height", "80%");
                        $(window).bind("resize." + uuid, function() {
                            printPopup.css("height", docH() - docH()/100 * 20);
                        });
                        popup.on("close", function() {
                            $(window).unbind("resize." + uuid);
                        })
                    }
                }
            });
        }
    })
};

_msi.printDeliveryDocket = function(id, isView) {
    var data = {id: id, view: isView, type: "delivery_docket"}, title = "print.delivery.docket"
    var docH = function() {
        return window.innerHeight;
    };
    var uuid = bm.getUUID();
    bm.ajax({
        url: app.baseUrl + "layout/isAdminLoggedIn",
        success: function() {
            bm.editPopup(app.baseUrl+"document/getDocumentData", $.i18n.prop(title), name, data, {
                width: 770,
                height: docH() - docH()/100 * 20,
                scroll: false,
                events: {
                    content_loaded: function (popup) {
                        var printPopup = this;
                        var iframeWindow = printPopup.find(".printBody")[0].contentWindow;
                        bm.onReady(iframeWindow, "printMe", function() {
                            printPopup.find(".print-button").click(function() {
                                iframeWindow.focus();
                                iframeWindow.printMe();
                            });
                        })
                        printPopup.find(".content").css("height", "80%");
                        $(window).bind("resize." + uuid, function() {
                            printPopup.css("height", docH() - docH()/100 * 20);
                        });
                        popup.on("close", function() {
                            $(window).unbind("resize." + uuid);
                        })
                    }
                }
            });
        }
    })
};

_msi.ajax_url = app.baseUrl + "order/shipmentInformation";