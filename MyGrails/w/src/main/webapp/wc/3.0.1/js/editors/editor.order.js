bm.onReady(app.tabs, "order", function() {
    var selectedCustomer;
    var index;
    var variationProductData = {}

    app.tabs.order.Creator = function(popupElement, parentTab) {
        var _self = this;
        this.popupElement = popupElement;
        this.parentTab = parentTab;
        this.addressData = {}
        index = 0;
        this.step = 1;
        this.maxStep = 3;
        _self.addedProduct = [];
        this.popupElement.find(".selected-products .tr").each(function() {
            var template = $(this);
            _self.addedProduct.push(template.find(".pId").val());
            index++;
            var stock = template.find(".stock");
            _self.bindAddedProduct(template, stock.config("spin"));
        })
        popupElement.find(".select-product-view").hide();
        var wrap = popupElement.find(".select-customer-for-order")
        var customerTable = this.customerTable = bm.table(wrap, {
            url: app.baseUrl + "order/loadCustomer",
            beforeReloadRequest: function () {
                _self.beforeReloadRequestCustomerTable(this.param);
            },
            afterLoad: function () {
                _self.newCustomer = null;
                _self.attachTableEvent()
            }
        });
        popupElement.find(".customer-search-button").on("click", function () {
            customerTable.reload();
        });
        popupElement.find(".customer-search-form").form({
            preSubmit: function () {
                customerTable.reload();
                return false;
            }
        });
        var productWrap = popupElement.find(".select-product-for-order");
        var productTable = bm.table(productWrap, {
            url: app.baseUrl + "order/loadProduct",
            beforeReloadRequest: function () {
                _self.beforeReloadProductTable(this.param);
            },
            afterLoad: function () {
                _self.attachProductTableEvent();
            }
        });
        popupElement.find(".product-search-button").on("click", function () {
            productTable.reload();
        });
        popupElement.find(".product-search-form").form({
            preSubmit: function () {
                productTable.reload();
                return false;
            }
        }).hide();
        _self.attachPopupEvent()
        _self.attachProductTableEvent()
        app.global_event.trigger("create-order-loader", [popupElement])
    }
    var _o = app.tabs.order.Creator.prototype;

    _o.goToNextStep = function(step) {
        if(step < 1 || step > this.maxStep) {
            return false;
        }
        var _self = this, popupElement = this.popupElement;
        popupElement.find("[step]").hide();
        popupElement.find("[step=" + step + "]").show();
        _self.step = step;
        popupElement.find(".body").scrollTop(0)
        return true
    };

    _o.attachPopupEvent = function() {
        var _self = this, popupElement = this.popupElement;
        selectedCustomer = popupElement.find("input[name=customerId]");
        var nextButton = popupElement.find(".nextStep"),
            previousButton = popupElement.find(".previousStep"),
            customerCreateBtn = popupElement.find(".toolbar-btn.create-customer")

        nextButton.on("click", function () {
            if(_self.step == 1) {
                if (!selectedCustomer.val()) {
                    bm.notify($.i18n.prop("no.customer.selected"), "alert");
                    return false;
                }
            } else if(_self.step ==2 && _self.addedProduct.length < 1) {
                bm.notify($.i18n.prop("no.product.is.selected"), "alert");
                return false;
            }
            return _self.goToNextStep(_self.step + 1);
        });
        _self.goToNextStep(1);
        previousButton.on("click", function () {
            _self.goToNextStep(_self.step - 1)
        });
        popupElement.find(".modify-billing-shipping-address").on("click", function() {
            _self.changeBillingShippingAddress();
        });
        customerCreateBtn.on("click", function() {
            app.tabs.customer.customers.prototype.createCustomer.call(_self.parentTab, {
                success: function(resp) {
                    _self.newCustomer = resp.id
                    _self.customerTable.reload()
                },
                createPanelTemplate: app.Tab.prototype.createPanelTemplate.clone()
            })
        });
        popupElement.find(".remove-all").on("click", function() {
            popupElement.find(".selected-products").find(".tr").remove();
            _self.addedProduct = [];
            index = 0;
            _self.setSelectedMark();
        });
        _self.attachTableEvent()
    }

    _o.changeBillingShippingAddress = function() {
        var _self = this, popupElement = this.popupElement;
        var data = {customerId: selectedCustomer.val()}, title = "change.address"
        if(Object.keys(_self.addressData).length) {
            data.billingAddress = _self.addressData.billingAddress
            data.shippingAddress = _self.addressData.shippingAddress
        }
        var changeAddressPopup = bm.editPopup(app.baseUrl + "order/changeAddressView", $.i18n.prop(title), name, data, {
            width: 780,
            events: {
                content_loaded: function () {
                    var addressPopup = this
                    var billingForm = addressPopup.find("form .edit-billing-address");
                    var shippingForm = addressPopup.find("form .edit-shipping-address");
                    bm.countryChange(addressPopup.find("form .edit-billing-address"), {inputClass: "medium"});
                    bm.countryChange(addressPopup.find("form .edit-shipping-address"), {inputClass: "medium"});
                    bm.initCityValidator(billingForm.find("[name=postCode]"), "countryId", "stateId", billingForm);
                    bm.initCityValidator(shippingForm.find("[name=postCode]"), "countryId", "stateId", shippingForm);
                }
            },
            beforeSubmit: function(form, settings, popup) {
                var addressPopup = this,
                    billingForm = addressPopup.find("form .edit-billing-address"),
                    orderForm = popupElement.find("form.order-create"),
                    billingData = {},
                    shippingData = {};
                addressPopup.find(".edit-billing-address input, .edit-billing-address select").each(function () {
                    var _this = $(this)
                    billingData[_this.attr("name")] = _this.val()
                })
                addressPopup.find(".edit-shipping-address input, .edit-shipping-address select").each(function () {
                    var _this = $(this)
                    shippingData[_this.attr("name")] = _this.val()
                })
                orderForm.find("input[name=billingAddress]").remove()
                orderForm.find("input[name=shippingAddress]").remove()
                _self.addressData = {billingAddress: JSON.stringify(billingData), shippingAddress: JSON.stringify(shippingData)}
                orderForm.append("<input type='hidden' name='billingAddress' value='" + _self.addressData.billingAddress + "'>" +
                    "<br><input type='hidden' name='shippingAddress' value='" + _self.addressData.shippingAddress + "'>")
                changeAddressPopup.close()
                return false
            }
        })
    }

    _o.attachTableEvent = function() {
        var _self = this, popupElement = this.popupElement
        popupElement.find(".customer-row").on("click", function () {
            var $this = $(this);
            if(!$this.is(".selected")) {
                _self.addressData = {}
                popupElement.find("form input[name=billingAddress]").remove()
                popupElement.find("form input[name=shippingAddress]").remove()
            }
            var old = popupElement.find(".customer-row.selected");
            old.removeClass("selected");
            $this.addClass("selected")
            selectedCustomer.val($(this).find("input[name=id]").val())
        })
    }

    _o.setSelectedMark = function() {
        var _self = this, popupElement = this.popupElement, selectedProductWrapper = popupElement.find(".selected-products").find(".table-body");
        popupElement.find(".product-row").each(function() {
            var $this = $(this), isSelected = selectedProductWrapper.find(".pId[value='" + $this.attr("product-id") + "']").length > 0;
            if(isSelected) {
                $this.addClass("selected")
            } else {
                $this.removeClass("selected")
            }
        })
    };

    _o.attachProductTableEvent = function() {
        var _self = this;
        this.popupElement.find(".product-row").on("click", function() {
            _self.addToAddedList($(this))
        });
        this.popupElement.find('.add-product.link').click(function () {
            _self.popupElement.find('.cancel').trigger('click');
            ComponentManager.openTab(ComponentManager.getRibbonData(app.ribbons.web_commerce, "item"));
            app.tabs.item();
        });
        app.global_event.on("delete-customer", function (evt, ids) {
            ids = typeof ids == "number" ? [ids] : ids
            _self.popupElement.find(".customer-row  input[name=id]").each(function() {
                var it = $(this);
                if(ids.contains(+it.val())) {
                    it.parents(".customer-row ").remove();
                }
            })
        })
        _self.setSelectedMark();
    }

    _o.addToAddedList = function(tr) {
        var _self = this, popupElement = _self.popupElement;
        var config = {tr: tr};
        config.id = tr.attr("product-id");
        $.each(_self.addedProduct, function(i, v) {
            if(""+this == config.id) {
                config.pos = 0;
            }
        })
        config.return = false;
        config.name = tr.find(".pName").html();
        config.sku = tr.find(".sku").html();
        config.price = tr.find(".pPrice").html();
        config.stock = tr.find(".stock").html();
        config.quantity = tr.find(".stock").attr("value");
        config.included = tr.find(".stock").config("included");
        config.included = config.included ? JSON.stringify(config.included) : "";
        config.combinedConf = tr.find(".stock").config("combined");
        config.spinConfig = tr.find(".stock").config("spin");

        app.global_event.trigger("on-backend-order-create", [config, popupElement, updateAddedList]);
        if(config.return) {
            return;
        }
        updateAddedList(config);

        function updateAddedList(config) {
            if(config.pos > -1) {
                bm.notify($.i18n.prop("product.already.added"), "alert");
                return;
            }
            var data = $.extend({productId: config.id, quantity: config.quantity, customerId: selectedCustomer.val(), included: config.included, 'config.options': config.options}, tr.closest(".order-create").serializeObject())
            bm.ajax({
                url: app.baseUrl + "order/validateQuantity",
                data: data,
                success: function(resp) {
                    var priceInput = '<input class="pPrice" type="hidden" name="products.product_'+ index +'.price" value="' + config.price + '"/>'
                    var quantityInput = '<input class="quantity tiny" type="text" name="products.product_'+ index +'.quantity" value="' + config.quantity + '"/>';
                    var variationInput = "";
                    if(config.options) {
                        $.each(config.options, function(i, v) {
                            variationInput += "<input type='hidden' name='products.product_"+ index +".config.options' value='"+v+"'>";
                        })
                    }
                    var attrs = config.attrs ? config.attrs : {};
                    var includedField = config.combinedConf.iscombined == true ? '<input class="included" type="hidden" name="products.product_' + index  + '.included" value=\''+ config.included +'\'>' : "";
                    var template = '<tr class="tr" data-attrs="'+attrs+'"> <input class="pId" type="hidden" value="'+ config.id +'" name="products.product_'+ index  + '.id" />' + includedField + '<td class="product-name">'
                        + config.name + '</td><td>'+ config.sku + '</td><td class="editable price" restrict="decimal" validation="price required">' + priceInput + '<span class="value">' + config.price + '</span></td><td class="stock">' + config.stock +
                        '</td><td class="quantity" restrict="numeric">'+ quantityInput  + '</td><td><span class="tool-icon remove"></span></td>'+(variationInput ? variationInput:'')+'</tr>'
                    template = $(template)
                    $.each(attrs, function (k, v) {
                        template.append("<input type='hidden' name='products.product_"+ index +"."+k+"' value='"+v+"'>");
                    })
                    popupElement.find('tr.no-order-found').remove();
                    popupElement.find(".selected-products").find(".table-body").append(template);
                    tr.addClass("selected");
                    _self.addedProduct.push(config.id);
                    index++
                    _self.bindAddedProduct(template, config.spinConfig ? config.spinConfig : {});
                }
            })
        }
    }

    _o.bindAddedProduct = function(template, spinConfig) {
        var _self = this;
        var priceTd = template.find(".editable:not(.quantity)");
        var quantityInput = template.find("td.quantity input");
        quantityInput.stepper(spinConfig);
        function callBack(td, val) {
            var input = td.find("input[type=hidden]");
            if(td.is(".quantity")) {
                var stock = parseInt(td.siblings("td.stock").text());
                if (stock < val) {
                    td.find(".value").text(input.val());
                    bm.notify($.i18n.prop("out.of.stock"), "alert");
                    return;
                }
            } else if(td.is(".price")) {
                var validation = td.attr("validation");
                var editField = td.find(".td-full-width");
                var errorObj = ValidationField.validateAs(editField, validation);
                if (errorObj) {
                    bm.notify($.i18n.prop(errorObj.msg_template, errorObj.msg_params), "alert");
                    editField.addClass("error-highlight");
                    setTimeout(function () {
                        editField.removeClass("error-highlight");
                    }, 1000);
                    return false;
                }
            }
            input.val(val);
        }
        bm.makeTableCellEditable(priceTd, callBack);
        template.find (".remove").on("click", function(){
            var parent = $(this).parent().parent();
            var id = parent.find(".pId").val();
            var pos = _self.addedProduct.indexOf(id);
            _self.addedProduct.splice(pos, 1);
            parent.remove();
            _self.setSelectedMark();
        })
    }

    _o.updateProductPrice = function(block, data) {
        var _self = this
        if(Object.keys(_self.addressData).length) {
            data.billingAddress = _self.addressData.billingAddress;
        }
        data.customerId = selectedCustomer.val();
        var currencyBlock = block.find(".current-price .currency-symbol");
        var priceBlock = block.find(".current-price .price-amount");
        var title = block.find(".title .emphasized");
        bm.ajax({
            controller: "order",
            action: "priceAndUnavailableMsg",
            data: data,
            success: function(resp) {
                var name = resp.productName;
                var price = resp.price.toFixed(2);
                variationProductData.price = price;
                currencyBlock.text(resp.currency);
                priceBlock.text(price);
                title.text(name);
            },
            error: function(a, b, resp) {
                variationProductData = {};
                title.text("");
                currencyBlock.text("");
                priceBlock.text(resp.message? resp.message : $.i18n.prop("not.available"));
            }
        })
    };

    _o.beforeReloadRequestCustomerTable = function (param) {
        var popupElement = this.popupElement, searchText = popupElement.find(".customer-search-text").val();
        var _params = {id: selectedCustomer.val(), newCustomer: this.newCustomer, searchText: searchText};
        $.extend(param, _params);
    }

    _o.beforeReloadProductTable = function (param) {
        var searchText = this.popupElement.find(".product-search-text").val();
        var _params = {searchText: searchText};
        $.extend(param, _params);
    }
})