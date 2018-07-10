/**
 * Created by sajedur on 6/8/2015.
 */
$(function() {
    app.config.saveCartStatusMgsTime = 6000;
    var PANEL, customerProfileDom = $("#customer-profile-tabs");
    function bindEventForInitForm(from) {
        from.form({
            ajax: true,
            preSubmit: function(ajaxSettings) {
                $.extend(ajaxSettings, {
                    success: function(resp) {
                        renderMessage(PANEL, resp.message, "success", app.config.saveCartStatusMgsTime, "before")
                        loadListing()
                    }
                })
            }
        });
        from.find(".cancel-button").on("click", function() {
            loadListing(true)
        })
    }

    function removeCart(cartRow) {
        bm.confirm($.i18n.prop("confirm.delete.saved.cart"), function() {
            bm.ajax({
                url: app.baseUrl + "saveCart/removeSavedCart",
                data: {cartId: cartRow.attr("cart-id")},
                success: function(resp) {
                    renderMessage(PANEL, resp.message, "success", app.config.saveCartStatusMgsTime)
                    cartRow.parents("tr").remove();
                },
                error: function(xhr, status, resp) {
                    renderMessage(PANEL, resp.message, "error", app.config.saveCartStatusMgsTime)
                }
            });
        }, function(){})

    }

    function addToCart($this) {
        bm.confirm($.i18n.prop("confirm.add.to.cart"), function() {
            bm.ajax({
                url: app.baseUrl + "saveCart/addToCart",
                data: {cartId: $this.attr("cart-id")},
                success: function(resp) {
                    if(resp.content) {
                        renderGlobalSitePopup($(resp.content), {clazz:  "exception-popup"})
                    } else {
                        location.href = app.baseUrl + "cart/details"
                    }
                },
                error: function(xhr, status, resp) {
                    renderMessage(PANEL, resp.message, "error", app.config.saveCartStatusMgsTime)
                }
            });
        }, function(){})
    }

    function loadSavedCartDetails(cartRow,dom) {
        dom.loader()
        bm.ajax({
            url: app.baseUrl + "saveCart/loadSavedCartDetails",
            data: {cartId: cartRow.attr("cart-id")},
            dataType: "html",
            success: function(html) {
                PANEL.html(html);
                PANEL.find(".submit-button").click(function() {
                    addToCart($(this));
                })
                PANEL.find(".back-button").click(function() {
                    loadListing(dom);
                });
                dom.loader(false);
            },
            error: function(xhr, status, resp) {
                renderMessage(PANEL, resp.message, "error", app.config.saveCartStatusMgsTime)
                dom.loader(false)
            }
        });
    }

    function bindEventForListing(panel) {
        panel.find("#saved-cart-listing .delete").on("click", function() {
            var $this = $(this);
           removeCart($this)
        })

        panel.find("#saved-cart-listing .add-to-cart").on("click", function() {
            var $this = $(this)
            addToCart($this)
        })
        panel.find("#saved-cart-listing .action-icon.details").on("click", function() {
            var $this = $(this);
            loadSavedCartDetails($this,panel)
        })
    }

    function loadListing(clearSaveOperation) {
        PANEL.loader()
        bm.ajax({
            url: app.baseUrl + "saveCart/customerProfile",
            dataType: "html",
            data: {clearSaveOperation: clearSaveOperation},
            success: function(html) {
                PANEL.html(html);
                bindEventForListing(PANEL)
            },
            error: function(xhr, status, resp) {
                PANEL.html($.i18n.prop("error"));
            },
            response: function() {
                PANEL.loader(false)
            }
        });
    }

    function manageSaveCart(panel) {
        PANEL = panel.find(".save_cart");
        var saveCartsPanel = panel.find(".save_cart");
        var saveCartInitForm = saveCartsPanel.find(".save-cart-init-form")
        bindEventForInitForm(saveCartInitForm)
        bindEventForListing(saveCartsPanel)
    }

    customerProfileDom.on("tab:load", function(ev, data) {
        tabSelect = document.location.search.substring(1,document.location.search.lenght)
        if(tabSelect  == "saveCart=true"){
            $("#my-carts").tabify("activate", "save_cart");
        }
        if(data.index == "my-carts"){
            manageSaveCart(data.panel)
        }
    });

});
