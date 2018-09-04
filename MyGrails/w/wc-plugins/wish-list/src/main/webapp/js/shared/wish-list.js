$(function() {
    window.wish_list_global_js_loaded = true;
    function renderPopup (content) {
        renderGlobalSitePopup(content, {
            clazz: "add-to-wishlist-popup",
            width: 800,
            ananimation_clazz: app.config.wish_list_popup_animation_clazz
        })
        site.hook.fire("wish-list-popup-content", content)
    }

    function attachPopupEvent(content) {
        content.find(".submit-button.create-wish-list").on("click", function() {
            if(!content.find(".body").valid()) {
                return;
            }
            var _data = content.find(".body").serializeObject();
            bm.ajax({
                url: app.baseUrl + "wishlist/save",
                data: _data,
                success: function(resp) {
                    var html = $(resp.html)
                    content.html(html.children());
                    content.find(".close-popup").click(function() {
                        site.global_single_popup.close();
                    })
                    attachPopupEvent(content);
                    app.global_event.trigger("wish-list-create")
                },
                error: function(a, b, resp) {
                }
            })
        });
        content.find(".submit-button.add-to-wish-list").on("click", function() {
            var productId = content.find("input[name=productId]").val();
            var data = content.find(".body").serializeObject();
            bm.ajax({
                url: app.baseUrl + "wishlist/add",
                data: data,
                success: function(resp) {
                    var html = $(resp.html);
                    site.global_single_popup.close();
                    renderPopup(html);
                },
                error: function() {

                }
            })
        })
    }

    function addToWishList(productId) {
        bm.ajax({
            url: app.baseUrl + "wishlist/wishListPopup",
            data: {productId: productId},
            success: function(resp){
                var content = $(resp.html);
                renderPopup(content);
                attachPopupEvent(content)
            },
            error: function(a, b, resp) {
                if(resp.url) {
                    var referer = location.pathname + location.search + location.hash + "#add-to-wish-list-" + productId
                    window.location = app.baseUrl + resp.url + "?referer=" + encodeURIComponent(referer);
                }
            }
        })
    }

    window.attachWishListGlobalEvents = function(contnet) {
        contnet.find(".add-to-wish-list").on("click", function() {
            addToWishList($(this).attr("productId"))
        })
    }
    attachWishListGlobalEvents($(document))
    location.hash.split("#").every(function(k, value) {
        var productId
        if(value.startsWith("add-to-wish-list-") && (productId = parseInt(value.substr(17)))) {
            addToWishList(productId)
        }
    });
});