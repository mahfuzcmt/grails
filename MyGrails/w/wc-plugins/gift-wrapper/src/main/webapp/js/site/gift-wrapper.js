$(function() {
    function addGiftWrapperPopup(target) {
        var itemId = target.attr("item-id");
        var productId = target.attr("product-id");
            bm.ajax({
                url: app.baseUrl + "giftWrapperPage/giftWrapperPopup",
                type: 'post',
                data: {cartItemId: itemId,productId: productId},
                success: function(resp) {
                    var content = $(resp.html);
                    if (content) {
                        renderGlobalSitePopup(content,
                        {
                            clazz: "add-to-gift-wrapper-popup",
                            width: 1000,
                            top:120,
                        });
                    }
                    $(".add-gift-wrapper-to-cart").on("click", function(event) {
                        var msg = event.currentTarget.parentElement.parentElement.jqObject.find(".gift-wrapper-message-input").val();
                        addGiftWrapperToCartItem($(event.target),msg)
                    });
                }
            });
    }

    $(".gift-wrapper-add-btn").on("click", function(event) {
        addGiftWrapperPopup($(event.target))
    });

    function addGiftWrapperToCartItem(target,message) {
        var itemId = target.attr("item-id");
        var productId = target.attr("product-id");
        var giftWrapperId = target.attr("gift-wrapper-id");
        var giftWrapperMsg = message;
            bm.ajax({
                url: app.baseUrl + "giftWrapperPage/addGiftWrapperToCart",
                type: 'post',
                data: {cartItemId: itemId,productId: productId,giftWrapperId: giftWrapperId,giftWrapperMsg:giftWrapperMsg},
                success: function(resp) {
                    location.reload();
                }
            });
    }

    function  removeGiftWrapperFromCartItem(target) {
        var itemId = target.attr("item-id");
        var productId = target.attr("product-id");
        var giftWrapperId = target.attr("gift-wrapper-id");
            bm.ajax({
                url: app.baseUrl + "giftWrapperPage/removeGiftWrapperFromCartItem",
                type: 'post',
                data: {cartItemId: itemId,productId: productId,giftWrapperId: giftWrapperId},
                success: function(resp) {
                    location.reload();
                }
            });
    }

    $(".add-gift-wrapper-to-cart").on("click", function(event) {
        addGiftWrapperToCartItem($(event.target))
    });

    $(".gift-wrapper-remove-btn").on("click", function(event) {
        removeGiftWrapperFromCartItem($(event.target))
    });

});