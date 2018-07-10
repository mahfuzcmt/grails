var product_quick_view_popup;
$(function () {
    var quickViewLoader = "<div><span class='vertical-aligner'></span><img src='" + app.baseUrl + "plugins/product-quick-view/images/site/loading.gif'></div>";
    var quickViewProductContent = $(".product-quick-view");
    bm.onReady(window, "$_i18n_properties_loaded", function() {
        quickViewProductContent.each(function(){
            initProductQuickView($(this));
        });
    })
    app.global_event.on("new-product-block-added", function() {
        quickViewProductContent.each(function(){
            initProductQuickView($(this));
        });
    });

    var initProductDetailsEventEvents = function(productId, content) {
        initializeProductInfoView(productId, content, "details", function() {
            if(window.GiftRegistryManager) {
                GiftRegistryManager.attachEvent(content)
            } else if(content.find(".add-to-gift-registry").length){
                bm.onReady(window, "GiftRegistryManager", {
                    ready: function() {},
                    not: bm.addScript.bind(undefined, "plugins/gift-registry/js/gift-registry.js", true)
                })
            }
            if(window.attachWishListGlobalEvents) {
                window.attachWishListGlobalEvents(content)
            } else if(content.find(".add-to-wish-list").length){
                bm.onReady(window, "attachWishListGlobalEvents", {
                    ready: function() {},
                    not: bm.addScript.bind(undefined, "plugins/wish-list/js/shared/wish-list.js", true)
                })
            }

            if(window.bindAddToCompareClickEvent) {
                window.bindAddToCompareClickEvent(content)
            } else if(content.find(".add-to-compare-button").length){
                bm.onReady(window, "bindAddToCompareClickEvent", {
                    ready: function() {},
                    not: bm.addScript.bind(undefined, "plugins/compare-product/js/compare-product.js", true)
                })
            }
            if(window.bindRemoveFromCompareClickEvent) {
                window.bindRemoveFromCompareClickEvent(content)
            }
        })

    };

    var initQuickPopup = function (popup, clazz) {
        var loader = "<div class='div-mask'><span class='vertical-aligner'></span><span>" + $.i18n.prop("loading") + "</span><div>"
        var content = $("<div>"
            + "<div class='header'>"
            + "<span class='close-popup close-icon'></span>"
            + "<div class='title'>" + $.i18n.prop("product.quick.view") + "</div>"
            + "<div class='view-switcher scroller'>"
            + "<span class='view-left scroll-left disabled'></span>"
            + "<span class='view-right scroll-right disabled'></span>"
            + "</div>"
            + "</div>"
            + "<div class='body'>"
            + loader
            + "</div>"
            + "</div>")
        return renderSitePopup({
            el: content,
            is_fixed: true,
            is_always_up: true,
            width: null,
            height: null,
            left: null,
            is_center: false,
            default_left: null,
            animation_clazz: app.config.quick_view_popup_animation_clazz,
            clazz: clazz
        }, popup)
    };

    var loadPopupContent = function(productBlock) {
        var productId = productBlock.attr("product-id")
        bm.ajax({
            url: app.baseUrl + "quickView/popup",
            data: {productId: productId},
            type: 'post',
            success: function (resp) {
                var content = $(resp.html)
                content.find(".widget-information, .widget-related, .widget-likeus, .widget-shipmentCalculator").remove()
                product_quick_view_popup.el.find(".body").replaceWith(content.find(".body"))
                bindNavigationEvent(product_quick_view_popup.el, productBlock)
                initProductDetailsEventEvents(productId, product_quick_view_popup.el)
            },
            error: function(a, b, resp) {
                var body = product_quick_view_popup.el.find(".body");
                bm.unmask(body);
                showStatus(body, "info", resp.message);
            }
        });
    };

    function initProductQuickView(productView) {
        productView.find(".product-block").each(function () {
            var productBlock = $(this)
            var quickViewButton = $("<span class='quick-view-btn button' style='display: none;'>" + $.i18n.prop('quick.view') + "</span>")
            if (productBlock.find(".quick-view-btn").length) {
                return
            }
            var imageBlock = productBlock.find(".product-image-link")
            imageBlock.before(quickViewButton)
            imageBlock.parent().closest("div").on({
                mouseover: function () {
                    quickViewButton.show()
                },
                mouseout: function () {
                    quickViewButton.hide()
                }
            })
            quickViewButton.on("click", function() {
                var currentBlock = null
                product_quick_view_popup = initQuickPopup(product_quick_view_popup, "product_quick_view_popup")
                var popupContent = product_quick_view_popup.el
                popupContent.css({left: '', top: ''})
                currentBlock = productBlock;
                loadPopupContent(productBlock)
                popupContent.find(".view-left").click(function() {
                    var $this = $(this), prev = null
                    if(!$this.is('.disabled') && (prev = currentBlock.prev()).length) {
                        loadPopupContent(prev)
                        currentBlock = prev;
                    }
                });
                popupContent.find(".view-right").click(function() {
                    var $this = $(this), next = null
                    if(!$this.is('.disabled') && (next = currentBlock.next()).length) {
                        loadPopupContent(next)
                        currentBlock = next;
                    }
                })
            })
        })
    }

    function bindNavigationEvent(content, productBlock) {
        if(productBlock.prev().length) {
            content.find(".view-left").removeClass("disabled")
        } else {
            content.find(".view-left").addClass("disabled")
        }
        if(productBlock.next().length) {
            content.find(".view-right").removeClass("disabled")
        } else {
            content.find(".view-right").addClass("disabled")
        }
    }

    function showStatus(dom, status, message) {
        var statusDom = dom.find(".message-block");
        if(!statusDom.length) {
            statusDom = dom.prepend('<span class="message-block"></span>').find(".message-block");
        }
        statusDom.removeClass('success').removeClass('error').removeClass('info').addClass(status);
        statusDom.append(message);
    }

})