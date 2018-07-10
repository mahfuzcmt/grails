bm.onReady(app.tabs, 'order', function () {
    var _o = app.tabs.order.prototype;
    _o.menu_entries.unshift({
        text: $.i18n.prop("reorder"),
        ui_class: "reorder",
        action: "reorder",
        license: "allow_reorder_feature"
    })
    var onMenuOpen = _o.onMenuOpen;
    _o.onMenuOpen = function(navigator) {
        onMenuOpen.call(this, navigator);
        var menu = this.tabulator.menu;
        if(navigator.is(".completed")) {
            menu.enable("reorder");
        } else {
            menu.disable("reorder");
        }
    }

    var onActionClick = _o.onActionClick;
    _o.onActionClick = function(action, data) {
        onActionClick.call(this, action, data);
        switch (action) {
            case "reorder":
                this.createOrder($.i18n.prop("reorder"), {orderId: data.id});
                break;
        }
    }

    app.global_event.on("create-order-loader", function(evt, popupElement) {
        if(popupElement.find(".customer-row.selected").length) {
            popupElement.find(".order-create-first-view .nextStep").trigger("click");
            var notAvailable = popupElement.find(".not-available").val();
            if(notAvailable) {
                bm.notify($.i18n.prop("product.not.available", [notAvailable]), "alert");
            }
        }
    })
});