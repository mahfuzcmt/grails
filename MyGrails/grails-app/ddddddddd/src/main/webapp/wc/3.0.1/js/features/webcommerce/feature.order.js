app.tabs.order = function () {
    this.text = $.i18n.prop("orders");
    this.tip = $.i18n.prop("manage.orders");
    this.ui_class = "orders";
    this.ajax_url = app.baseUrl + "order/loadAppView";
    app.tabs.order._super.constructor.apply(this, arguments);
};

app.ribbons.web_commerce.push({
    text: $.i18n.prop("orders"),
    processor: app.tabs.order,
    ui_class: "order",
    ecommerce: true
});

app.tabs.order.inherit(app.SingleTableTab)

var _o = app.tabs.order.prototype;

_o.sortable = {
    list: {
        "2": "id",
        "4": "created",
        "5": "total"
    },
    sorted: "1",
    dir: "down"
};

_o.action_menu_entries = [
    {
        text: $.i18n.prop("manage.owner.permissions"),
        ui_class: "manage-owner-permissions order-permissions",
        action: "manage-owner-permissions"
    },
    {
        text: $.i18n.prop("export"),
        ui_class: "export",
        action: "export"
    }

];


_o.onActionMenuClick = function(action) {
    switch (action) {
        case "manage-owner-permissions":
            this.manageOwnerPermissions();
            break;
        case "export":
            window.open(app.baseUrl + "order/exportOrder" + (this.advanceSearchFilter ? "?" + bm.buildQuery(this.advanceSearchFilter) : ""))
    }
};
_o.manageOwnerPermissions = function() {
    bm.permissionPopup($.i18n.prop("manage.owner.permissions"), $.i18n.prop("order"), {for: "owner", type: "order"})
};

_o.onActionMenuOpen = function(navigator) {
    var itemList = [
        {
            key: "order.edit.permission",
            class: "manage-owner-permissions"
        }
    ];
    app.checkPermission(navigator, itemList);
};

_o.menu_entries = [
    {
        text: $.i18n.prop("manage.shipment"),
        ui_class: "manage-shipment",
        action: "manage-shipment"
    },
    {
        text: $.i18n.prop("manage.payment"),
        ui_class: "manage-payment",
        action: "manage-payment"
    },
    {
        text: $.i18n.prop("comment.history"),
        ui_class: "comment-history"
    },
    {
        text: $.i18n.prop("print.order"),
        ui_class: "print-order"
    },
    {
        text: $.i18n.prop("print.picking.slip"),
        ui_class: "print-picking-slip"
    },
    {
        text: $.i18n.prop("print.invoice"),
        ui_class: "print-invoice"
    },
    {
        text: $.i18n.prop("complete.order"),
        ui_class: "complete-order"
    },
    {
        text: $.i18n.prop("cancel.order"),
        ui_class: "cancel-order"
    }
]

_o.onMenuOpen = function(navigator) {
    var menu = this.tabulator.menu;
    var itemList = [
        {
            key: "order.view",
            class: "print-order",
            isEntity: true
        },
        {
            key: "order.manage.shipment",
            class: "manage-shipment",
            isEntity: true
        },
        {
            key: "order.manage.payment",
            class: "manage-payment",
            isEntity: true
        },
        {
            key: "order.manage.order",
            class: "print-order, .complete-order, .cancel-order, .comment-history",
            isEntity: true
        }
    ];
    app.checkPermission(menu, itemList, navigator.config("entity"));

    if(navigator.is(".completed")) {
        menu.find(".menu-item.complete-order").addClass("disabled");
    } else {
        menu.find(".menu-item.complete-order").removeClass("disabled");
    }
    if(navigator.is(".cancelled")) {
        menu.find(".cancel-order, .manage-payment, .manage-shipment").hide();
    } else {
        menu.find(".cancel-order, .manage-payment, .manage-shipment").show();
    }
}

_o.onActionClick = function(action, data) {
    switch (action) {
        case "manage-shipment":
            this.manageShipment(data.id);
            break;
        case "manage-payment":
            this.managePayment(data.id);
            break;
        case "comment-history":
            this.commentHistory(data.id);
            break;
        case "print-order":
            this.printOrder(data.id, false);
            break;
        case "print-picking-slip":
            this.printPickingSlip(data.id, false);
            break;
        case "print-invoice":
            this.printInvoice(data.id, false);
            break;
        case "complete-order":
            this.changeStatus(data.id, "complete");
            break;
        case "cancel-order":
            this.changeStatus(data.id, "cancel");
            break;
    }
};

_o.onSelectedActionClick = function(action, selecteds) {
    var _self = this;
    switch (action) {
        case "cancel":
            _self.changeStatus(selecteds.collect("id"), "cancel");
            break;
        case "sendInvoice":
            this.sendInvoice(selecteds.collect("id"));
            break;
    }
};

(function() {
    function attachEvents() {
        var _self = this;
        this.body.find(".header .order-status").change(function () {
            _self.body.find(".tool-group.search-form").trigger("submit");
        });
        this.on_global(["shipment-added", "payment-added", "payment-refunded"], function(){
            _self.reload()
        });
        _self.body.find(".toolbar .create").on("click", function () {
            _self.createOrder();
        });
    }

    _o.init = function () {
        var _self = this;
        app.tabs.order._super.init.call(_self);
        attachEvents.call(_self);
        this.afterTableReload()
    }
})();

_o.afterTableReload = function() {
    var _self = this;
    var body = _self.body;
    bm.tableToggleRow(_self.body);
    body.find(".edit-shipping-address").on("click", function() {
        _self.changeOrderAddress(this.id, "shipping");
    });
    body.find(".edit-billing-address").on("click", function() {
        _self.changeOrderAddress(this.id, "billing");
    });
};

_o.changeOrderStatus = function(id, status) {
    var _self = this;
    bm.ajax({
        url: app.baseUrl + 'order/updateOrderStatus',
        data: {orderId: id, orderStatus: status},
        success: function() {
            app.global_event.trigger("order-status-change", [id, status])
            _self.reload()
        }
    })
}

_o.changeOrderAddress = function(id, address_type) {
    var _self = this
    bm.editPopup(app.baseUrl + "order/changeOrderAddress", $.i18n.prop("order.address.editor"), undefined, {orderId: id, addressType: address_type}, {
        success: function() {
            _self.reload();
        },
        events: {
            content_loaded: function () {
                bm.countryChange(this, {stateName: "state.id"});
                bm.initCityValidator(this.find("[name=postCode]"), null, "state.id");
            }
        }
    });
}

_o.managePayment = function(id) {
    var tab = app.Tab.getTab("tab-manage-payment-of-order-" + id);
    if (!tab) {
        tab = new app.tabs.managePayment({
            id: "tab-manage-payment-of-order-" + id,
            order: {
                id: id
            }
        });
        tab.render();
    }
    tab.setActive();
}

_o.commentHistory = function(orderId) {
    var commentPopup
    var sendButton
    var commentArea
    bm.editPopup(app.baseUrl + "order/loadCommentView", $.i18n.prop("order.comment.history"), undefined, {orderId: orderId}, {
        width: 700,
        auto_close_on_success: false,
        clazz: "order-comment-popup",
        events: {
            content_loaded: function(popup, form) {
                var _self = $(this);
                commentPopup = _self;
                commentArea = commentPopup.find(".comment-area");
                commentArea.scrollTop(commentArea[0].scrollHeight);
                sendButton = _self.find(".save-and-send");
                form.on("lock", function() {
                    sendButton.attr("disabled", "disabled");
                });
                form.on("unlock", function() {
                    sendButton.removeAttr("disabled");
                });
                _self.find(".save-and-send").click(function() {
                    form.append("<input type='hidden' name='saveNsend' value='true'/>");
                    sendButton.attr("disabled", "disabled");
                    form.trigger("submit");
                })
            }
        },
        success: function(resp, status) {
            if(status == "success") {
                sendButton.removeAttr("disabled");
                var message = $("<div class='comment-row admin'><span class='name'>" +
                    $.i18n.prop(resp.storeName ? resp.storeName : $.i18n.prop("admin")) + " </span>" +
                    "<span class='date-time-row'><span class='date-time'>" + resp.date + "</span>" +
                    "<span class='show-comment'>" + resp.msg + "</span></span></div>");
                commentPopup.find("input[name=saveNsend]").remove();
                commentPopup.find("textarea[name=message]").val("");
                commentArea.append(message);
                commentArea.animate({scrollTop : commentArea[0].scrollHeight}, "slow");
            }
        },
        beforeSubmit: function(form, settings, popup){
            var saveAndSubmitButton = form.find(".save-and-send");
            saveAndSubmitButton.attr("disabled", "disabled");
        },
        error: function() {
            var saveAndSubmitButton = form.find(".save-and-send");
            saveAndSubmitButton.removeAttr("disabled");
            sendButton.removeAttr("disabled");
            commentPopup.find("input[name=saveNsend]").remove();
        }
    });
}

_o.changeStatus = function(id, status) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm." + status + ".order"), function(){
        bm.ajax({
            url: app.baseUrl + "order/changeStatus",
            data: {id: id, status: status},
            success: function(){
                app.global_event.trigger("order-status-change", [id, status])
                _self.reload();
            }
        })
    }, function(){});
}

_o.sendInvoice = function (ids) {
    var _self = this;
    bm.ajax({
        url: app.baseUrl + "order/sendInvoice",
        data: {orderId: ids}
    })
}

_o.manageShipment = function(id) {
    var tab = app.Tab.getTab("tab-manage-shipment-of-order-" + id);
    if (!tab) {
        tab = new app.tabs.manageShipment.shipmentInfo({
            id: "tab-manage-shipment-of-order-" + id,
            data: {
                order: {
                    id: id
                }
            }
        });
        tab.render();
    }
    tab.setActive();
}

_o.createPanelTemplate = $('<div class="embedded-edit-form-panel create-panel fade-in-up create-form-editor">' +
'<div class="header"><span class="header-title"></span>' +
'<span class="toolbar toolbar-right">' +
'<div class="search-form tool-group customer-search-form" step="1">' +
'<input type="text" class="search-text customer-search-text" placeholder="'+ $.i18n.prop('search')+'">' +
'<button type="button" class="icon-search customer-search-button" onclick="return false"></button></div>' +
'<div class="search-form tool-group product-search-form" step="2">' +
'<input type="text" class="search-text product-search-text" placeholder="'+ $.i18n.prop('search')+'">' +
'<button type="button" class="icon-search product-search-button" onclick="return false"></button></div>' +
'<span class="tool-group toolbar-btn create-customer create" step="1"><i></i>' + $.i18n.prop("create")+ '</span>' +
'<span class="tool-group toolbar-btn nextStep" step="1">' + $.i18n.prop("next")+ '</span>' +
'<span class="tool-group toolbar-btn previousStep" step="2">'+ $.i18n.prop('previous')+'</span>' +
'<span class="tool-group toolbar-btn nextStep" step="2">' + $.i18n.prop("next")+ '</span>' +
'<span class="tool-group toolbar-btn previousStep" step="3">'+ $.i18n.prop('previous')+'</span>' +
'<span class="tool-group toolbar-btn save" step="3">'+ $.i18n.prop('order.now')+'</span>' +
'<span class="tool-group toolbar-btn cancel">' + $.i18n.prop("cancel") + '</span></span>' +
'</div><div class="body"></div></div>');

_o.createOrder = function (title, data) {
    title = title ? title : $.i18n.prop("create.order");
    data = data ? data : {};
    var _self = this;
    this.renderCreatePanel(app.baseUrl + "order/create", title, undefined, data, {
        content_loaded: function () {
            var popup = this;
            _self.orderCreator = new app.tabs.order.Creator(popup, _self);
        },
        width: 850,
        success: function () {
            _self.reload();
            app.global_event.trigger("order-create");
        },
        beforeSubmit: function() {
            if(_self.orderCreator.addedProduct.length < 1) {
                bm.notify($.i18n.prop("no.product.is.selected"), "alert");
                return false;
            }
        }
    });
}

_o.view = function(id) {
    var popup = bm.viewPopup(app.baseUrl + "order/view", {id: id}, {width: 800, clazz: "view-popup order-details-view"});
    popup.on("content_loaded", function() {
        var dom = popup.getDom();
        dom.find(".manage-payment").on("click", function() {
            _o.managePayment(id);
            popup.close();
        });
        dom.find(".manage-shipment").on("click", function() {
            _o.manageShipment(id);
            popup.close();
        });
    })
}

_o.printOrder = function(id, isView) {
    var data = {id: id, view: isView, type: "order"}, title = isView ? "view.order" : "print.order"
    var docH = function() {
        return window.innerHeight;
    };
    var popupConfig = {
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
    }
    var uuid = bm.getUUID();
    bm.ajax({
        url: app.baseUrl + "layout/isAdminLoggedIn",
        success: function() {
            bm.ajax({
                url: app.baseUrl + "document/isDocumentActive",
                data: {type: "order"},
                success: function() {
                    bm.editPopup(app.baseUrl+"document/getDocumentData", $.i18n.prop(title), name, data, popupConfig);
                },
                error: function () {
                    popupConfig.content = $("<span class='print-order-image print-button'></span><iframe style='height: 100%; width: 100%; border: none; overflow: scroll' class='printBody' src='" +
                        app.baseUrl+"order/print?id="+id+"'></iframe>")
                    bm.editPopup(undefined, $.i18n.prop(title), name, data, popupConfig);
                }
            })
        }
    })
};

_o.printPickingSlip = function(id, isView) {
    var data = {id: id, view: isView, type:"picking_slip"}, title = isView ? "view.order" : "print.picking.slip"
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

_o.printInvoice = function(id, isView) {
    var data = {id: id, view: isView, type: "invoice"}, title = isView ? "view.order" : "print.invoice"
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

_o.advanceSearchUrl = app.baseUrl + "order/filter"
_o.advanceSearchTitle = $.i18n.prop("order");

_o.beforeReloadRequest = function (params) {
    app.tabs.order._super.beforeReloadRequest.call(this, params)
    var orderStatus = this.body.find(".header select.order-status");
    if(this.advanceSearchFilter) {
        orderStatus.chosen("val", "");
    } else {
        $.extend(params, {orderStatus: orderStatus.val()})
    }
};