app.config.checkout_step_message_display_time = app.config.checkout_step_message_display_time  || 6000;
//region Checkout
app.checkout = function () {
    var _self = this;
    _self.body = $('.checkout-page');
    _self.body.find(".section.loaded").each(function () {
        _self.initSection($(this));
    })
};

var _c = app.checkout.prototype;

_c.loadSection = function (section, data, successCallback) {
    var _self = this;
    section.loader();
    bm.ajax({
        url: section.data("url"),
        data: data,
        method: "POST",
        dataType: "html",
        response: function () {
            section.loader(false)
        },
        success: function (resp) {
            section.find(".share-toolbar").remove();
            section.find(".body").html(resp);
            var shareToolbar = section.find(".share-toolbar");
            if(shareToolbar.length) {
                section.find(">.header").append(shareToolbar.removeClass("hidden"))
            }
            _self.initSection(section);
            section.addClass("loaded");
            successCallback && successCallback()
        },
        error: function (status, xhr, resp) {
            var errorMgs = $(resp).find(".message").text();
            renderMessage(section.find(".body"), errorMgs, "error", app.config.checkout_step_message_display_time);
            if(status.status == 412) {
                location.href = app.baseUrl + "cart/details"
            }
        }
    })
};

_c.initSection = function (section) {
    var _self = this, sectionData = section.data(),
        mode = _self.mode = _self.body.find("[name=mode]").val();

    section.find(".edit-section").on("click", function () {
        _self.loadSection(section, {mode: "edit"}, function () {
            _self.body.find(".section").each(function () {
                var $this = $(this);
                if(sectionData.step < $this.data("step")) {
                    $this.removeClass("loaded").find(".share-toolbar").remove();
                    $this.find(".body").empty()
                }
            });
        })
    });

    section.find(".step-continue-button").on("click", function () {
        var beforeContinueFunction = "before" + sectionData.name.capitalize() + "Continue", data = { mode: "view"};
        _self[beforeContinueFunction] && _self[beforeContinueFunction](data, section);
        _self.loadSection(section, data, function () {
           var newSection = _self.getNextSection(sectionData);
           if(newSection.length) {
               _self.loadSection(newSection);
           } else {
               section.loader();
               location.href = app.baseUrl + "shop/payment?confirmed=true"
           }
        })
    });

    var initFunc = "init" + sectionData.name.capitalize() + "Section";
    _self[initFunc] && _self[initFunc](section, sectionData)
};

_c.getNextSection = function (sectionData) {
    var _self = this;
    var next = _self.body.find(".section.step-" + sectionData.step).next();
    while (next.length && next.is(".disabled")) {
        next = next.next()
    }
    return next
};

_c.initAddressSection = function (section, data) {
    var _self = this;
    if(!_self.addressEditor) {
        _self.addressEditor = new app.checkout.Address(section, data, this)
    }
    _self.addressEditor.init()

};

_c.initShippingSection = function (section, data) {
    var _self = this;
    section.find(".remove").on("click", function () {
        var $this = $(this), data = section.serializeObject();
        $.extend(data, {
            mode: "edit",
            removedItem: $this.data("id")
        });
        _self.loadSection(section, data)
    })
};

_c.initConfirmSection = function (section, data) {
    var _self = this;
    if(!_self.confirmEditor) {
        _self.confirmEditor = new app.checkout.Confirm(section, data, this)
    }
    _self.confirmEditor.init()

};

_c.beforeAddressContinue = function (data, section) {
  var sectionData = section.find(".delivery-types").serializeObject();
  sectionData.is_different_shipping = sectionData.is_different_shipping || "false";
  $.extend(data, sectionData)
};

_c.beforeShippingContinue = function (data, section) {
    $.extend(data, section.serializeObject(), {mode: "view"})
};

_c.disableSection = function (sectionName) {
    this.body.find(".section." + sectionName).addClass("disabled")
};

_c.enableSection = function (sectionName) {
    this.body.find(".section." + sectionName).removeClass("disabled")
};


//endregion

//region Address Section
app.checkout.Address = function (body, data, parent) {
    var _self = this;
    _self.body = body;
    _self.parent = parent;
    $.extend(_self, data);
};

var _a = app.checkout.Address.prototype;

_a.init = function () {
    var _self = this, mode = _self.mode = _self.body.find("[name=mode]").val(), initFunc = "init" + mode.capitalize() + "View";
    _self[initFunc] && _self[initFunc]()
};

_a.initEditView = function () {
    var _self = this, body = _self.body;
    bm.autoToggle(body);
    body.find(".edit-address").on("click", function () {
        _self.editAddress($(this).data())
    });
    body.find(".change-address").on("click", function () {
        _self.changeAddress($(this))
    });
    var deliveryTypes = body.find("[name=delivery_type]");
    deliveryTypes.on("change", function () {
        var deliveryType = body.find("[name=delivery_type]:checked").val();
        if(deliveryType == "shipping") {
            _self.parent.enableSection("shipping");
        } else if (deliveryType == "store_pickup"){
            _self.parent.disableSection("shipping");
        } else {
            _self.parent.disableSection("shipping");
        }
    })
};

_a.initInitView = function () {
    var _self = this;
    _self.initAddressForm(_self.body.find("form"))
};

_a.editAddress = function (data) {
    var _self = this;
    _self.body.loader();
    bm.ajax({
        url: app.baseUrl + "shop/loadAddressEditor",
        dataType: "html",
        method: "get",
        data: data,
        success: function (resp) {
            var addressFormWrap = $(resp);
            _self.body.find(".address." + data.type).append(addressFormWrap);
            _self.attachAddressFormEvt(addressFormWrap);
            _self.body.loader(false);
        }
    })
};

_a.initAddressForm = function (form) {
    var _self = this;
    form.form({
        preSubmit: function (ajaxSettings) {
            _self.parent.beforeAddressContinue(ajaxSettings.data = {}, _self.body)
        },
        ajax: {
            data: {mode: "edit"},
            dataType: "html",
            success: function (resp) {
                _self.body.find(".body").html(resp);
                _self.parent.initSection(_self.body)
            }
        }
    })
};

_a.attachAddressFormEvt = function(addressFormWrap) {
    var _self = this;
    var parentBlock = addressFormWrap.parent();
    parentBlock.addClass("edit");
    bm.initCountryChangeHandler(addressFormWrap.find(".country-selector-row select"));
    bm.initCityValidator(addressFormWrap.find('[name="postCode"]'));
    addressFormWrap.find(".cancel-button").click(function() {
        parentBlock.removeClass("edit");
        addressFormWrap.remove()
    });
    _self.initAddressForm(addressFormWrap.find("form"))
};

_a.reload = function (data) {
    var _self = this;
    _self.body.loader();
    _self.parent.loadSection(_self.body, data)
};

_a.changeAddress = function (changeBtn) {
    var _self = this, config = changeBtn.data();
    bm.selectionFloatingPanel(changeBtn, app.baseUrl + "shop/addressSelectionPopup", {addressType: config.type}, {
        width: 350,
        clazz: "address-selection-popup " + config.type,
        events: {
            content_loaded: function(popup) {
                this.find(".create-address").on("click", function () {
                    _self.editAddress({
                        type: $(this).data("type"),
                        isNew: true
                    });
                    popup.close()
                })
            }
        },
        onSelect: function (data) {
            _self.reload({
                mode: "edit",
                operation: "saveOrSelectAddress",
                selectedAddress: data.id,
                addressType: config.type
            })
        }
    })
};
//endregion

//region Confirm section
app.checkout.Confirm = function (body, data, parent) {
    var _self = this;
    _self.body = body;
    _self.parent = parent;
    $.extend(_self, data);
};

_c = app.checkout.Confirm.prototype;

_c.init = function () {
    var _self = this, body =_self.body, paymentMethod = body.find(".payment-method");
    paymentMethod.on("change", function () {
        _self.reload({payment_gateway: paymentMethod.val()})
    });
    var confirmForm = body.find("form");
    confirmForm.form({
        preSubmit: function() {
            var termsCheckBox = $(this).find("[name='termsAndCondition']");
            if(termsCheckBox.length && !termsCheckBox.is(":checked")) {
                renderMessage(_self.body, $.i18n.prop("terms.condition.fail.message"), "error", app.config.checkout_step_message_display_time);
                return false;
            }
        }
    });
    body.find(".payment-options").attachValidator();
    body.find(".default-payment-amount").on("change", function () {
        var $this = $(this), validator = $this.data("validator-filed-inst");
        _self.updateDefaultPaymentAmount($this.attr("name"), $this.val())
    });
    body.find(".default-payment .remove").on("click", function () {
        _self.updateDefaultPaymentAmount($(this).data("name"), 0)
    });
    body.find(".collapsible .header").on("click", function () {
        var $this = $(this);
        $this.parents(".collapsible").toggleClass("active")
    });
    body.find(".code-submit-form").each(function () {
        var form = $(this)
        form.form({
            validation_config: {
                validate_on_call_only: true
            },
            submitButton: form.find("button"),
            preSubmit: function () {
                _self.reload(form.serializeObject())
                return false
            }
        })
    });
    setTimeout(function () {
        body.find(".message-block").remove()
    }, app.config.checkout_step_message_display_time )
};

_c.reload = function (data, success, error) {
    var _self = this;
    _self.body.loader();
    bm.ajax({
        url: _self.url,
        data: data,
        method: "POST",
        dataType: "HTML",
        success: function (resp) {
            _self.body.find(".body").html(resp);
            _self.parent.initSection(_self.body);
            _self.body.loader(false);
            success && success()
        },
        error: function (status, xhr, resp) {
            _self.body.loader(false);
            var errorMgs = $(resp).find(".message").text();
            renderMessage(_self.body, errorMgs, "error", app.config.checkout_step_message_display_time);
            error && error(errorMgs)
        }
    })
};

_c.updateDefaultPaymentAmount = function (identifier, amount) {
    var _self = this, data = {};
    data[identifier] = amount;
    _self.reload(data)
};

//endregion

$(function () {
   new app.checkout()
});

