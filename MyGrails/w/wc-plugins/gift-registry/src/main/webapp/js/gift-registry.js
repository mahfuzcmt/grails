$(function() {

    function renderGiftRegistryPopup (content) {
        renderGlobalSitePopup(content, {
            clazz: "add-to-gift-registry-popup",
            width: 600,
            animation_clazz: app.config.gift_registry_popup_animation_clazz
        });
    }

    function addToGiftRegistry(data, container) {
        collectCartData(container, data, false)
        bm.ajax({
            url: app.baseUrl + "giftRegistry/add",
            data: data,
            success: function(resp){
                var content = $(resp.html);
                renderGiftRegistryPopup(content);
                attachPopupEvents(content)
            },
            error: function(a, b, resp) {
                if(resp.url) {
                    var referer = location.pathname + location.search + location.hash + "#add-to-gift-registry";
                    window.location = app.baseUrl + resp.url + "?referer=" + encodeURIComponent(referer);
                }
            }
        })
    }

    function attachPopupEvents(contents) {
        contents.find(".create-gift-registry").on("click", function() {
            window.location = app.baseUrl + "customer/profile" + "?referer=" + encodeURIComponent(window.location.href)+ "&gift_registry=true#my-list";
        });
        contents.find(".add-to-gift-registry").on("click", function() {
            var $this = $(this);
            var data = {productId: $this.attr("product-id"), quantity: $this.attr("quantity"), giftRegistry: contents.find("[name=giftRegistry]").val()};
            addToGiftRegistry(data, contents);
        })

    }

    function attachEvent(contents) {
        contents.find(".add-to-gift-registry").on("click", function() {
            var button = $(this);
            prepareAddGiftRegistryData(button);
        });
    }

    function prepareAddGiftRegistryData(button){
        if(button.is(".disabled")) {
            return;
        }
        var productId = button.attr("productId");
        var cartButton = button.closest(".widget-addCart").find(".add-to-cart-button[product-id=" + productId + "]");
        var carts = cartButton.config("cart");
        var container = cartButton.closest(".page-content, .popup, .product-block, .add-cart-info-block");
        var quantity = carts["quantity"] || carts["min-quantity"];
        var data = {productId: productId, quantity: quantity}
        addToGiftRegistry(data, container)
    }

    attachEvent($(document))

    app.global_event.on("add-to-cart-button-disabled", function() {
        $(".add-to-gift-registry").addClass("disabled");
    });

    app.global_event.on("add-to-cart-button-enabled", function() {
        $(".add-to-gift-registry").removeClass("disabled");
    });

    window.GiftRegistryManager = {
        attachEvent: function(contents) {
            attachEvent(contents)
        }
    }

    app.global_event.on("after-product-info-view-initialize", function(evt, productId, content) {
        if(content.find(".add-to-gift-registry").length) {
           GiftRegistryManager.attachEvent(content)
        }
    })

    location.hash.split("#").every(function(k, value) {
        if(value.startsWith("add-to-gift-registry") ) {
            prepareAddGiftRegistryData($('.add-to-gift-registry'))
        }
    });

});
