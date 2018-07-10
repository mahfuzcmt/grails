function initialiseEnterpriseVariationWidget(productId, content, widget) {
    var matrix = widget.find(".matrix");
    matrix.find(".cell.available").on("click", function() {
        var cell = $(this);
        if(!cell.is(".selected")) {
            var data = {productId: productId, 'config.variation': cell.attr("v-id")};
            loadVariation(data);
        }
    })

    var select = widget.find("select.product-variation-select");
    select.on("change", function(ev) {
        var data = widget.serializeObject();
        data.productId = productId;
        loadVariation(data);
    });

    var thumbView = widget.find(".variation-thumb");
    thumbView.find(".option-cell").on("click", function() {
        var $this = $(this);
        var ids = [$this.attr("option-id")];
        var types = thumbView.find(".variation-type").not($this.parents(".variation-type"));
        types.each(function() {
            ids.push($(this).find(".option-cell.selected").attr("option-id"));
        });
        loadVariation({productId: productId, 'config.options': ids});
    })

    function loadVariation(data) {
        bm.ajax({
            url: app.baseUrl + 'enterpriseVariation/loadVariationProductUrl',
            data: data,
            success: function(resp) {
                if(resp.url) {
                    window.location.href = app.baseUrl + 'product/' + resp.url
                }
            }
        });
    }
}