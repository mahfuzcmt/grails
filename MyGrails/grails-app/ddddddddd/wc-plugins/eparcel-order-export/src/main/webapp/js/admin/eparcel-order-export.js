$(function () {
    bm.onReady(app.tabs, 'order', function () {
        var _o = app.tabs.order.prototype;
        _o.action_menu_entries.push(
            {
                text: $.i18n.prop("eparcel.order.export"),
                ui_class: "eparcel-order-export",
                action: "eparcel-order-export",
                license: "allow_eparcel_order_export_feature"
            }
        );
        var onActionMenuClick = _o.onActionMenuClick;
        _o.onActionMenuClick = function(action) {
            onActionMenuClick.apply(this, arguments)
            switch (action) {
                case "eparcel-order-export":
                    this.eparcelOrderExport();
                    break;
            }
        }
        _o.eparcelOrderExport = function() {
            bm.editPopup(app.baseUrl + 'eparcelOrderExport/loadExportPrerequisite', $.i18n.prop('eparcel.order.export'), '', {}, {
                width: 480,
                beforeSubmit: function($popup, form, popup) {
                    setTimeout(function() {
                        popup.close()
                    }, 3000)
                }
            });
        };
    });
})