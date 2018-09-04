/*****use for add to cart, order creation variation selection***********/
var initVariationSelection = (function(productId, content, callback) {
    var variationContainer = content.find(".variation-container");
    var select = variationContainer.find("div.product-variation-select");
    var loader = "<div><span class='vertical-aligner'></span><img src='" + app.systemResourceUrl + "plugins/variation/images/site/loading.gif'></div>";
    if(select.length) {
        select.chosen({
            disable_search_threshold: 10,
            disable_search: true
        });
        var type = content.find(".variation-model").val();
        select = content.find("select.product-variation-select");
        var cartBtn = content.find(".add-to-cart-button");
        select.on("change", function(ev) {
            var data = variationContainer.serializeObject();
            bm.mask(content, loader);
            bm.ajax({
                url: app.baseUrl + "variation/loadPriceStockImage",
                data: data,
                response: function() {
                    content.unmask();
                },
                success: function(resp) {
                    var info = content.find(".short-info");
                    info.find(".name").html(resp.name);
                    content.find(".product-img img").attr("src", resp.image);
                    var priceInfo = info.find(".current-price");
                    if(resp.price) {
                        var quantity = resp.quantity, priceDom = '<span>' + $.i18n.prop("price") + ': </span><span class="currency-symbol">'+resp.currency+'</span>' +
                            '<span class="price-amount">' + resp.price + '</span> X <span class="quantity">' + quantity + '</span>=' +
                            '<span class="currency-symbol">' + resp.currency + '</span><span class="total-amount">' + resp.cartPageDisplayTotal + '</span>',
                            quantityDom = '<span>' + $.i18n.prop('quantity') + ': </span> ' + quantity;
                        priceInfo.html(priceDom);
                        info.find('div.quantity').html(quantityDom)
                        cartBtn.removeClass("disabled").prop("disabled", false);
                    } else {
                        priceInfo.html('<span class="product-status message-block error">' + resp.msg + '</span>');
                        cartBtn.addClass("disabled").prop("disabled", true);
                    }
                    if(callback) {
                        callback(resp);
                    }
                }
            })
        });
    }
});

var initVariationSelectionInProductList = (function (productId, content) {
    var singleProductBody = null;
    content = content.jqObject;
    var matrix = content.find(".matrix");
    matrix.find(".cell.available").on("click", function () {
        var $this = $(this);
        var ids = [$this.attr("v-id")];
        if (!$this.is(".selected")) {
            var data = {productId: productId, 'config.variation': ids};
            initVariationData($this, data);
        }
    });

    var selectOptions = content.find("div.product-variation-select");
    if(selectOptions.length) {
        selectOptions.chosen({
            disable_search_threshold: 10,
            disable_search: true
        });
    }

    var select = content.find("select.product-variation-select");
    select.on("change", function (ev) {
        var $this = $(this);
        var data = content.serializeObject();
        data.productId = productId;
        initVariationData($this,data);
    });

    var thumbView = content.find(".variation-thumb");
    thumbView.on("click", ".option-cell", function () {
        var $this = $(this);
        var ids = [$this.attr("option-id")];
        var types = $this.closest(".variation-thumb").find(".variation-type").not($this.parents(".variation-type"));
        types.each(function () {
            ids.push($(this).find(".option-cell.selected").attr("option-id"));
        });
        initVariationData($this,{productId: productId, 'config.options': ids});
    });

    function initVariationData(widgetDom, data) {
        singleProductBody = widgetDom.closest(".product-block");
        var productScope = widgetDom.closest("#category-product-listing");
        if (productScope.length > 0) {
            productScope = "category"
        } else {
            productScope = widgetDom.closest(".widget.widget-product").attr("widget-id");
        }
        data.productScope = productScope;
        loadVariation(data);
    }

    function loadVariation(data) {
        bm.ajax({
            url: app.baseUrl + 'variation/loadVariationDataInProductList',
            data: data,
            type: 'post',
            success: function (resp) {
                var content = $(resp.html)[0];
                singleProductBody.replaceWith(content);
                initProductWidgetEvents(productId,content.jqObject);
            }
        });
    }
});

var initProductWidgetEvents = function (productId, content) {
    initializeProductWidget();
    bindAddToCartClickEvent();
    initVariationSelectionInProductList(productId, content);
    if (window.GiftRegistryManager) {
        GiftRegistryManager.attachEvent(content)
    } else if (content.find(".add-to-gift-registry").length) {
        bm.onReady(window, "GiftRegistryManager", {
            ready: function () {
            },
            not: bm.addScript.bind(undefined, "plugins/gift-registry/js/gift-registry.js", true)
        })
    }
    if (window.attachWishListGlobalEvents) {
        window.attachWishListGlobalEvents(content)
    } else if (content.find(".add-to-wish-list").length) {
        bm.onReady(window, "attachWishListGlobalEvents", {
            ready: function () {
            },
            not: bm.addScript.bind(undefined, "plugins/wish-list/js/shared/wish-list.js", true)
        })
    }

    if (window.bindAddToCompareClickEvent) {
        window.bindAddToCompareClickEvent(content)
    } else if (content.find(".add-to-compare-button").length) {
        bm.onReady(window, "bindAddToCompareClickEvent", {
            ready: function () {
            },
            not: bm.addScript.bind(undefined, "plugins/compare-product/js/compare-product.js", true)
        })
    }
    if (window.bindRemoveFromCompareClickEvent) {
        window.bindRemoveFromCompareClickEvent(content)
    }
    if (window.bindAddinitProductQuickView) {
        window.bindAddinitProductQuickView(content)
    }
};

