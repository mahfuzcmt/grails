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