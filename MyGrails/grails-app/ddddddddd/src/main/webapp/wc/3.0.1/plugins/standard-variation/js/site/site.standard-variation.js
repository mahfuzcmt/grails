function initialiseStandardVariationWidget(productId, content, widget) {
    var matrix = widget.find(".matrix");
    matrix.find(".cell.available").on("click", function() {
        var cell = $(this);
        if(!cell.is(".selected")) {
            var data = {productId: productId, 'config.variation': cell.attr("v-id")};
            changeStockAndStatus(data, cell);
            changeImage(data, function() {
                content.unmask();
            });
        }
    })

    var select = widget.find("select.product-variation-select");
    select.on("change", function(ev) {
        var data = widget.serializeObject();
        bm.mask(content, widget.data("loader"));
        changeStockAndStatus(data);
        changeImage(data, function() {
            content.unmask();
        });
    });

    var thumbView = widget.find(".variation-thumb");
    thumbView.find(".option-cell").on("click", function() {
        var $this = $(this);
        var ids = [$this.attr("option-id")];
        var types = thumbView.find(".variation-type").not($this.parents(".variation-type"));
        types.each(function() {
            ids.push($(this).find(".option-cell.selected").attr("option-id"));
        });
        var data = {productId: productId, 'config.options': ids};
        bm.mask(content, widget.data("loader"));
        changeStockAndStatus(data, $this);
        changeImage(data, function() {
            content.unmask();
        });
    })

    function changeStockAndStatus(data, cell) {
        var cartBtns = content.find(".widget-addCart .button");
        bm.ajax({
            controller: "productPage",
            action: "priceAndUnavailableMsg",
            data: data,
            success: function(resp) {
                widget.find(".cell.available.selected").removeClass("selected");
                content.find(".product-widget.widget-price").replaceWith(resp.priceWidget);
                var total = content.find(".current-price .total-amount");
                if(total.length) {
                    var quantity = data.quantity || content.find(".add-to-cart-button").config("cart", "quantity");
                    total.text((resp.price * quantity).toFixed(2));
                }
                widget.parent(".widget-variation").find('.product-status').remove();
                content.find('.widget-stockMark').replaceWith(resp.stockWidget).show();
                if(cell) {
                    cell.siblings(".option-cell").removeClass("selected");
                    cell.addClass("selected");
                }
                cartBtns.removeClass("disabled");
            },
            error: function(a, b, resp) {
                content.find('.widget-stockMark').hide();
                widget.find(".product-status").remove();
                if(!content.find(".product-status.error").length) {
                    widget.parent().prepend('<span class="product-status message-block error">'+resp.message+'</span>');
                }
                if(cell) {
                    cell.siblings(".option-cell").removeClass("selected");
                    cell.addClass("selected");
                }
                cartBtns.addClass("disabled");
            }
        })
    }

    function changeImage(data, callback) {
        bm.ajax({
            url: app.baseUrl + "standardVariation/loadImageWidget",
            data: data,
            response: function() {
                callback();
            },
            success: function(resp) {
                if(resp.imageWidget) {
                    var newWidget = $(resp.imageWidget);
                    $(".zoomContainer").remove();
                    content.find(".widget-productImage").replaceWith(newWidget);
                    app.productWidgets.initImageWidget(newWidget)
                }
            },
            error: function(a, b, resp) {

            }
        })
    }
}