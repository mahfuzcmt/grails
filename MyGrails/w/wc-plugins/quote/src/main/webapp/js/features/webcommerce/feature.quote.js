/**
 * Created by sajedur on 6/15/2015.
 */
app.tabs.quote = function () {
    this.text = $.i18n.prop("quotes");
    this.tip = $.i18n.prop("manage.quotes");
    this.ui_class = "quotes";
    this.ajax_url = app.baseUrl + "quoteAdmin/loadAppView";
    app.tabs.quote._super.constructor.apply(this, arguments);
};

app.ribbons.report.push({
    text: $.i18n.prop("quote"),
    processor: app.tabs.quote,
    ui_class: "quote",
    license: "allow_quote_feature",
    ecommerce: true
});

app.tabs.quote.inherit(app.SingleTableTab)

var _q = app.tabs.quote.prototype;

_q.sortable = {
    list: {
        "0": "id",
        "2": "created"
    },
    sorted: "0",
    dir: "down"
};

_q.menu_entries = [

    {
        text: $.i18n.prop("send.quote"),
        ui_class: "send-email",
        action: "send-quote"
    },
    {
        text: $.i18n.prop("view"),
        ui_class: "view",
        action: "view"
    },
    {
        text: $.i18n.prop("requote"),
        ui_class: "requote",
        action: "requote"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "remove",
        action: "remove"
    },
    {
        text: $.i18n.prop("make.order"),
        ui_class: "make-order",
        action: "make-order"
    }
];

_q.onMenuOpen = function(navigator) {
    var menu = this.tabulator.menu;
    var itemList = [
        {
            key: "quote.send",
            class: "send-quote"
        },
        {
            key: "quote.view",
            class: "view"
        },
        {
            key: "quote.manage",
            class: "requote, .remove, .make-order"
        }
    ];
    app.checkPermission(menu, itemList);
}

_q.onActionClick = function (action, data) {
    switch (action) {
        case "send-quote":
            this.sendQuote(data.id);
            break;
        case "view":
            this.view(data.id)
            break
        case "requote":
            this.requote(data.id)
            break
        case "remove":
            this.remove(data.id)
            break
        case "make-order":
            this.makeOrder(data.id)
    }
};

_q.sendQuote = function(id) {
    bm.confirm($.i18n.prop("confirm.send.quote"), function() {
        bm.ajax({
            url: app.baseUrl + "quoteAdmin/sendQuote",
            data: {id: id}
        })
    }, function(){})
};

_q.view = function(id) {
    var _self = this
    bm.viewPopup(app.baseUrl + "quoteAdmin/view", {id: id}, {
        width: 800,
        events: {
            content_loaded: function(popup) {
                this.find("button.requote").on("click", function() {
                    popup.close()
                    _self.requote(id);
                })
            }
        }
    })
};

_q.addressEditPop = function(quote, address, section, success) {
    var data = {
        quote: quote,
        section: section,
        address: address
    }
    bm.editPopup(app.baseUrl + "quoteAdmin/changeAddress", $.i18n.prop("change.address"), null, data, {
        events: {
            content_loaded: function() {
                var form = this.find("form");
                bm.countryChange(form, {inputClass: "medium", stateName: "stateId"});
                bm.initCityValidator(form.find("[name=postCode]"), "countryId", "stateId", form);
            }
        },
        beforeSubmit: function(form, settings, popup) {
            success(form.serializeObject())
            popup.close()
            return false
        }
    })
}

_q.manageRequotePopup = function(popup, popupDom) {
    var _self = this, form = popupDom.find("form"), validatorInstance = form.data("validatorInst"), grandTotal = form.find(".grand-total"),
        quote = form.find("[name=quote]").val(), billing = form.find("[name=billing]") , shipping = form.find("[name=shipping]");
    function resolveFieldValidator (field) {
        var index = validatorInstance.fields.find(function(_input) {
            return _input.elm.is(field)
        });
        return index
    }

    function updateItemTotal(tr) {
        var discount = parseFloat(tr.find(".discount").val()), tax = parseFloat(tr.find(".tax").val());
        var total = tr.find(".price").val() * tr.find(".quantity").val() - (discount ? discount : 0) + (tax ? tax : 0);
        tr.find(".total").text(total)
        updateGrandTotal()
    }
    function updateGrandTotal() {
        var grandTotalAmount = 0.0;
        form.find("table.items tr.item").each(function() {
            grandTotalAmount += parseFloat($(this).find(".total").text().trim())
        })
        var shipping = parseFloat(form.find("[name=shippingCost]").val()), handling = parseFloat(form.find("[name=shippingTax]").val()), tax = parseFloat(form.find("[name=handlingCost]").val())
        grandTotalAmount  = grandTotalAmount + (shipping ? shipping : 0) + (handling ? handling : 0)  + (tax ? tax : 0)
        grandTotal.text(grandTotalAmount)
    }
    function updateTotalAfterGroupEdit(group) {
        var tr = group.parents(".item");
        if(tr.length) {
            updateItemTotal(tr)
        } else {
            updateGrandTotal()
        }
    }
    function afterEditCheck(group) {
        var actionBtn = group.find(".action"), field =  group.find("input"), validator = resolveFieldValidator(field);
        if(validator.validate()) {
            group.removeClass("editing")
            actionBtn.removeClass("apply").addClass("edit")
            group.find(".value").text(field.val())
            updateTotalAfterGroupEdit(group)
            return true
        }
        return false
    }
    function attachEvents() {
        form.find(".editable-group .action").on("click", function() {
            var $this = $(this), group = $this.parents(".editable-group");
            if(group.is(".editing")) {
                afterEditCheck(group)
            } else {
                var currentEditingField = form.find(".editable-group.editing");
                if(!currentEditingField.length || afterEditCheck(currentEditingField) == true) {
                    group.addClass("editing")
                    $this.removeClass("edit").addClass("apply")
                }
            }
        });
        form.find(".item .quantity").on("change", function() {
            var _this = $(this);
            var errorObj = ValidationField.validateAs(_this, _this.attr("validation"));
            if (errorObj) {
                bm.notify($.i18n.prop(errorObj.msg_template, errorObj.msg_params), "alert");
                return;
            }
            var tr = _this.parents(".item");
            updateItemTotal(tr);
        });
        popupDom.find(".change-billing").on("click", function() {
            _self.addressEditPop(quote, billing.val(), "billing", function(formData) {
                billing.val(JSON.stringify(formData))
            })
        });
        popupDom.find(".change-shipping").on("click", function() {
            _self.addressEditPop(quote, shipping.val(), "shipping", function(formData) {
                shipping.val(JSON.stringify(formData))
            })
        })
        form.find(".item .remove").on("click", function() {
            var $this = $(this)
            form.append('<input type="hidden" name="removed" value="' + $this.attr("item-id") + '"/> ')
            $this.parents("tr").remove()
        })
    }
    attachEvents()
}

_q.requote = function(id) {
    var _self = this;
    bm.editPopup(app.baseUrl + "quoteAdmin/requote", $.i18n.prop("quote.details"), "Quote# " + id, {id: id}, {
        width: 800,
        events: {
            content_loaded: function(popup) {
                _self.manageRequotePopup(popup, this)
            }
        },
        success: function() {
            _self.reload();
        }
    }).on("ajax_loaded", function() {
        this.find(".spinner.quantity").on("onStepperBind", function(evt, config) {
            var _this = $(this);
            $.extend(config, _this.config("spin"));
        })
    })
};

_q.remove = function(id) {
    var _self = this;
    bm.remove("quote", "Quote", $.i18n.prop("confirm.remove.quote"), app.baseUrl + "quoteAdmin/remove", id, {
        success: function() {
            _self.reload();
        }
    } )
};
_q.makeOrder = function(id) {
    var _self = this
    bm.confirm($.i18n.prop("make.order.confirm"), function() {
        bm.ajax({
            url: app.baseUrl + "quoteAdmin/makeOrder",
            data: {quoteId: id},
            success: function() {
                _self.reload();
            }
        })
    }, function() {})
};
(function () {
    _q.init = function () {
        app.tabs.quote._super.init.call(this);
        app.tabs.quote.tab = this
    }
    app.global_event.on('after-ribbon-render', function (e, ribbonBar) {
        if (app.isPermitted("quote.view.list")) {
            ribbonBar.enable("quote");
        } else {
            ribbonBar.disable("quote");
        }
    });
})();

