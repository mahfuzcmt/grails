app.tabs.paymentGateway = function () {
    this.text = $.i18n.prop("payment.gateways");
    this.tip = $.i18n.prop("manage.payment.gateway");
    this.ui_class = "payment-gateways";
    this.ajax_url = app.baseUrl + "paymentGateway/loadAppView";
    app.tabs.paymentGateway._super.constructor.apply(this, arguments);
};

app.ribbons.web_commerce.push({
    text: $.i18n.prop("payment.gateways"),
    processor: app.tabs.paymentGateway,
    ui_class: "payment-gateway",
    ecommerce: true
});

app.tabs.paymentGateway.inherit(app.SingleTableTab)

var _pg = app.tabs.paymentGateway.prototype;

_pg.menu_entries = [
    {
        text: $.i18n.prop("config"),
        ui_class: "edit config",
        action: "edit"
    }
];

_pg.onActionClick = function (action, data) {
    switch (action) {
        case "edit":
            this.configPaymentGateway(data.id, data.name);
            break;
    }
};

_pg.onSelectedActionClick = function(action, selecteds){
    switch (action){
        case "status":
            this.changeStatus(selecteds);
            break;
    }
};

_pg.changeStatus = function(selecteds) {
    var _self = this;
    bm.editPopup(app.baseUrl + 'paymentGateway/loadStatusOption', $.i18n.prop('status'), null , {},  {
        width: 600,
        events: {
            content_loaded: function() {
                var $this = $(this);
                var ids = selecteds.collect("id");
                $.each(ids, function (index, value) {
                    $this.find(".edit-popup-form").append('<input type="hidden" name="id" value="' + value + '">');
                });
            }
        },
        success: function () {
            _self.reload();
        }
    });
};

(function() {
    _pg.init = function () {
        app.tabs.paymentGateway._super.init.call(this);
        app.tabs.paymentGateway.tab = this
    }
    app.global_event.on('after-ribbon-render', function (e, ribbonBar) {
        if (app.isPermitted("payment_gateway.view.list")) {
            ribbonBar.enable("payment-gateway");
        } else {
            ribbonBar.disable("payment-gateway");
        }
    });
})();

_pg.configPaymentGateway = function (id, name) {
    var data = {id: id},
        title = $.i18n.prop("payment.gateway.config"),
        _self = this;
    this.renderCreatePanel(app.baseUrl + "paymentGateway/config", title, $.i18n.prop(name), data, {
        width: 795,
        content_loaded: function(form) {
            var popup  = this;

            if(name == "credit.card") {
                var token = popup.find("[name='metafield.CRD.creditCardProcessor']").change(function() {
                    loadCreditCardMeta(popup, $(this).val(), form)
                }).val();
                if(token) {
                    loadCreditCardMeta(popup, token, form);
                }
            }

            var zoneDropdown = popup.find("select[name='zone.id']");
            bm.zoneSelector(popup, zoneDropdown);

            var surchargeDropdown = popup.find('#surchargeType');
            surchargeDropdown.change(function() {
                surchargePanel(popup);
            });
            surchargeDropdown.trigger('change');

            if(popup.find("#surcharge-range-selector").length) {
                surchargeCreateHandler(popup);
            }

            function loadCreditCardMeta(popup, token, form) {
                bm.ajax({
                    dataType: "html",
                    url: app.baseUrl + "paymentGateway/paymentProcessorFields",
                    data: {
                        gateway : token
                    },
                    success: function(res) {
                        var fieldBlock = popup.find("#card-processor-fields");
                        if(!fieldBlock.length) {
                            var row = popup.find("[name='metafield.CRD.creditCardProcessor']").closest(".form-row");
                            row.after(fieldBlock = $("<div id='card-processor-fields'></div>"));
                        }
                        res = $(res);
                        fieldBlock.html(res);
                        fieldBlock.updateUi();
                        bm.autoToggle(fieldBlock)
                        var panel = form.obj(ValidationPanel);
                        panel.attach(res.find("[validation]"), panel)
                    }
                });
            }
            function surchargePanel(popup) {
                var type = popup.find("#surchargeType").val();
                var surchargeField = popup.find('.surcharge');
                var selectorPanel = popup.find('#surcharge-range-selector');
                surchargeField.hide();
                selectorPanel.hide();
                selectorPanel.trigger("validate")
                surchargeField.find("input").trigger("validate")
                switch(type) {
                    case 'flat_surcharge':
                        surchargeField.show();
                        break;
                    case 'surcharge_on_amount_range':
                        selectorPanel.show();
                        break;
                }
            }
            function surchargeCreateHandler(popup) {
                var selectorPanel = popup.find('#surcharge-range-selector');
                var multiCondition = popup.find(".multi-conditions");
                var twoPanelSelection = bm.twoSideSelection(multiCondition, 10, "item", false, {view: false, "column-sort": false}, ['from', 'to', 'surcharge-amount'])
                var editPanel, fromField, toField, surchargeAmount, addBtn;
                twoPanelSelection.initContent = function (panel, selectFunc) {
                    fromField = panel.find(".from");
                    toField = panel.find(".to");
                    surchargeAmount = panel.find(".surcharge-amount");
                    editPanel = panel;
                    addBtn = editPanel.find(".addCondition");
                    panel.attachValidator();
                    addBtn.on("click", function () {
                        if (!panel.valid()) {
                            return
                        }
                        var from = fromField.val(),
                            to = toField.val(),
                            surcharge = surchargeAmount.val();
                        var conditionText = $.i18n.prop("from") + ": " + from + "\n" +
                            $.i18n.prop("to") + ": " + to + "\n" +
                            $.i18n.prop("surcharge.amount") + ": " + surcharge;
                        var tagInput = [from, to, surcharge];
                        panel.find("input").val("");
                        if (panel.attr("data-mode") == "update") {
                            selectFunc([conditionText], tagInput, $(this).data("row"));
                            panel.find(".cancel-button").triggerHandler("click");
                        } else {
                            selectFunc([conditionText], tagInput);
                        }
                        selectorPanel.trigger("change").trigger("validate");
                    });
                };
                var cancelBtn = $("<button type='button' class='cancel-button'>" + $.i18n.prop("cancel") + "</button>");
                twoPanelSelection.onEditClick = function (type, values, name, row) {
                    fromField.val(values.from);
                    toField.val(values.to);
                    surchargeAmount.val(values['surcharge-amount']);
                    addBtn.before(cancelBtn);
                    addBtn.before("&nbsp; ");
                    cancelBtn.on("click", function () {
                        $(this).remove();
                        addBtn.text($.i18n.prop("add"));
                        editPanel.attr("data-mode", "add");
                        editPanel.find("input").val("");
                    });
                    addBtn.text($.i18n.prop("update")).data("row", row);
                    editPanel.attr("data-mode", "update")
                };
                twoPanelSelection.onDeleteClick = function () {
                    var from = fromField.val(),
                        to = toField.val(),
                        surcharge = surchargeAmount.val();
                    editPanel.find(".cancel-button").triggerHandler("click");
                    fromField.val(from);
                    toField.val(to);
                    surchargeAmount.val(surcharge);
                };
                twoPanelSelection.onRemoveAll = function () {
                    cancelBtn.trigger("click");
                };
                twoPanelSelection.triggerExternalInit();
            }
        },
        success: function () {
            _self.reload();
            if(id){
                app.global_event.trigger("payment-gateway-update", [id]);
            }
        }
    });
};