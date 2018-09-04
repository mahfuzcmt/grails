$(function() {
    var customerProfileDom = $("#customer-profile-tabs");
    customerProfileDom.on("tab:load", function (ev, data) {
        switch (data.index) {
            case "abandoned-cart":
                abandonedCart(data);
                break
        }
    });
    var abandonedCart = function(data) {
        var panel = data.panel(".abandoned_cart");
        panel.on("click", "#abondoned-cart-list .delete", function() {
            var $this = $(this);
            var id = $this.attr("cart-id");
            bm.confirm($.i18n.prop("confirm.delete.abandoned.cart", [id]), function() {
                bm.ajax({
                    url: app.baseUrl + "abandonedCart/removeAbandonedCart",
                    data: {cartId: id},
                    success: function(resp) {
                        renderMessage(panel.find("#abondoned-cart-list"), resp.message, "success", app.config.customer_profile_message_display_time);
                        $this.parents("tr").remove();
                    },
                    error: function(xhr, status, resp) {
                        panel.html($.i18n.prop("error"))
                    }
                });
            }, function(){})
        });
        function loadAbandonedCart() {
            bm.ajax({
                url: app.baseUrl + "abandonedCart/loadAbandonedCart",
                dataType: "html",
                success: function(html) {
                    panel.html(html);
                },
                error: function(xhr, status, resp) {
                    panel.html($.i18n.prop("error"));
                }
            });
        }
        panel.on("click", "#abondoned-cart-list .add-to-cart", function() {
            var $this = $(this);
            addToCart($this);
        });
        function addToCart($this) {
            var confirmPanel = $("<div class='confirm-panel'><span class='title'>" + $.i18n.prop("confirm.abandoned.add.to.cart") + "</span>" +
                "<div class='form-row  btn-row'><button class='submit-button' type='button'>" + $.i18n.prop("confirm") + "</button><button class='back-button' type='button'>" + $.i18n.prop("cancel") + "</button></div></div>");
            panel.html(confirmPanel);
            confirmPanel.find(".submit-button").on("click", function() {
                bm.ajax({
                    url: app.baseUrl + "abandonedCart/abandonedAddToCart",
                    data: {cartId: $this.attr("cart-id")},
                    success: function(resp) {
                        location.href = app.baseUrl + "cart/details"
                    },
                    error: function(xhr, status, resp) {
                        panel.html($.i18n.prop("error"));
                    }
                });
            });
            confirmPanel.find(".back-button").on("click", function() {
                loadAbandonedCart();
            })
        }
        panel.on("click", "#abondoned-cart-list .action-icon.details", function() {
            var $this = $(this);
            bm.ajax({
                url: app.baseUrl + "abandonedCart/loadAbandonedCartDetails",
                data: {cartId: $this.attr("cart-id")},
                dataType: "html",
                success: function(html) {
                    panel.html(html);
                    panel.find(".submit-button").click(function() {
                        addToCart($(this));
                    });
                    panel.find(".back-button").click(function() {
                        loadAbandonedCart();
                    });
                },
                error: function(xhr, status, resp) {
                    panel.html($.i18n.prop("error"))
                }
            });
        })
    };
});
