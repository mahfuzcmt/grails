$(function() {
    function serializeAddToCartButton(productId) {
        var _this = $(".add-to-cart-button[product-id=" + productId + "]");
        var carts = _this.config("cart");
        var combinedConfig = _this.config("combined");
        var quantity = carts["quantity"] || carts["min-quantity"];
        var container = _this.closest(".page-content, .popup, .product-block, .add-cart-info-block");
        var data = {quantity: quantity}
        collectCartData(container, data, false)
        return data;
    }

    function renderCalculatorPopup (content) {
        updateUi(content);
        attachEvent(content);
        var shipment_calculator_popup;
        content.find(".close-popup, .continue-shopping-btn").click(function () {
            shipment_calculator_popup.close();
        })
        if(!shipment_calculator_popup || shipment_calculator_popup.is_closed) {
            shipment_calculator_popup = content.popup({
                is_fixed: true,
                is_always_up: true,
                modal: false,
                clazz: "shipping-calculator-popup",
                width: 800
            }).obj(POPUP)
        } else {
            shipment_calculator_popup.setContent(content.children())
        }
    }
    function updateUi(content) {
        bm.initCountryChangeHandler(content.find(".country-selector-row select"))
        bm.initCityValidator(content.find('[name="postCode"]'))
    }

    function attachResponseEvent(resp) {
        var classSelector = resp.find(".shipping-class-selector")
        if(!classSelector) {
            return;
        }
        classSelector.on("change", function() {
            var price = $(this).find("option:selected").data();
            var error = resp.filter(".error");
            var info = resp.find(".affected-row");
            if(price.invalidShipping) {
                error.show();
                info.hide();
            } else {
                error.hide();
                info.show();
                resp.find(".shipping-class-shipping").text(price.shipping);
                resp.find(".shipping-class-handling").text(price.handling);
            }
        }).trigger("change");
    }

    function attachEvent(content) {
        function calculate() {
            var page = content.find("input[name=page]").val();
            var data = content.serializeObject();
            if(page == 'product') {
                var productId = content.find("input[name=productId]").val();
                data = $.extend(data, serializeAddToCartButton(productId));
            }
            var wrap = content.find(".last-column .column-content");
            wrap.loader();
            bm.ajax({
                url: app.baseUrl + "shipmentCalculator/calculate",
                data: data,
                type: 'post',
                response: function() {
                    wrap.loader(false)
                },
                success: function(resp) {
                    var html = $(resp.html);
                    attachResponseEvent(html)
                    content.find(".last-column .column-content").html(html)
                },
                error: function(a, b, resp) {
                    log(resp)
                    content.find(".last-column .column-content").html('<h1>' + $.i18n.prop("shipping.cost") + '</h1><span>' + resp.message + '</span>');
                }
            });
        }
        content.form({
            preSubmit: function () {
                calculate()
                return false
            }
        })
    }
    function attach() {
        var buttons = $(".shipment-calculator.button");
        app.global_event.on("add-to-cart-button-disabled.shipment-calc", function() {
            if(buttons.attr("page") == "product") {
                buttons.addClass("disabled");
            }
        })
        app.global_event.on("add-to-cart-button-enabled", function() {
            buttons.removeClass("disabled");
        });
        buttons.each(function() {
            var button = $(this)
            if(button.is(".initialized")) {
                return
            }
            button.addClass("initialized");
            button.on("click", function(){
                var _self = $(this);
                if(_self.is(".disabled")) {
                    return;
                }
                var page = _self.attr("page");
                var productId = _self.attr("productId")
                bm.ajax({
                    url: app.baseUrl + "shipmentCalculator/renderPopup",
                    data: {page: page, productId: productId},
                    dataType: "html",
                    success: function(content) {
                        content = $(content);
                        renderCalculatorPopup(content);
                    }
                })
            });
        })
    }
    attach();
    app.global_event.on("checkout-page-address-editor-loaded.shipment-calc", function() {
        attach();
    });
    app.global_event.on("checkout-step-shipping_address-loaded.shipment-calc", function() {
        attach();
    });

})