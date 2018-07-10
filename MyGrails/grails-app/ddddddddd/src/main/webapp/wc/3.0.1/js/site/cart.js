$(function() {
    var page = $(".page-content");
    var updateButton = page.find(".update-cartitem-btn");
    var cartTable = page.find(".cartitem-table");
    var messageBox = page.find(".shopping-cartitem .error-message");
    var loader = '<div><span class="vertical-aligner">' + $.i18n.prop("updating") + '...</span></span></div>';
    if(messageBox.length) {
        messageBox.scrollHere()
        setTimeout(function () {
            messageBox.hide()
        }, app.config.shopping_cart_page_message_display_time)
    }
    var old = cartTable.find(".cart-item[old-quantity]")
    old.each(function() {
        var tr = $(this);
        var oldQuantity = tr.attr("old-quantity");
        var msg = oldQuantity + " quantity for product '" + tr.find(".product-name a").text() + "' is not available";
        renderMessage(cartTable, msg, "error", app.config.shopping_cart_page_message_display_time, "before");
    })
    if(old.length) {
        bm.ajax({ url: app.baseUrl + "cart/revertDirtyQuantity"});
    }

    page.find(".cartitem-table .stepper").on("min", function() {
        var box = $(this).find(".stepper-input");
        var config = box.config("spin");
        var quantity = +box.val();
        box.trigger("ichange", [quantity-1]);
    })
    page.find(".cartitem-table .stepper").on("max", function() {
        var box = $(this).find(".stepper-input");
        var config = box.config("spin");
        var quantity = +box.val();
        box.trigger("ichange", [quantity + 1]);
    });
    var quantitySelector = page.find(".cartitem-table .product-quantity-selector.text-type");
    quantitySelector.on("before-step-change", function()  { // TODO: Temporary fix, should fix ichange
       $(this).focus()
    });
    quantitySelector.ichange(function(evt, quantity) {
        if(quantity == 0 || !this.value) {
            return;
        }
        var box = $(this), id = box.attr("item-id"), cartLine = box.parents(".cart-item"),
            data = {quantity: quantity, cartItemId: id}
        bm.mask(page, loader);
        bm.ajax({
            url: app.baseUrl + "cart/updateQuantity",
            data: data,
            response: function() {
                bm.unmask(page);
            },
            success: function(resp) {
                if(resp.message) renderMessage(cartLine.find(".product-name .wrapper"), resp.message, resp.status + "-message in-cell-message-block", app.config.shopping_cart_page_message_display_time, "append");
                $.each(resp.cartData, function(key, value) {
                    key = key.minusCase();
                    page.find("." + key).text(value)
                });
                page.find(".cart-item").each(function() {
                    var $this = $(this), itemId = +$this.attr("item-id");
                    $.each(resp.cartItemsData[+itemId], function(key, value) {
                        $this.find("." + key.minusCase()).text(value)
                    });
                })
                box.val(resp.cartItemsData[+id].cartItemQuantity)
                app.global_event.trigger("update-cart", [page]);
            },
            error: function(a, b, resp) {
                box.val(resp.cartItemsData[+id].cartItemQuantity)
                renderMessage(cartLine.find(".product-name .wrapper"), resp.message, resp.status + "-message in-cell-message-block", app.config.shopping_cart_page_message_display_time, "append")
            }
        });
    }).numeric()
})