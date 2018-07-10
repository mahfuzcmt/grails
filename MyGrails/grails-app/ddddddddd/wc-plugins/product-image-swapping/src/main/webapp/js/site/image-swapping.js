$(function () {
    initSwap()
    app.global_event.on("new-product-block-added", function() {
        initSwap()
    });

});

this.initSwap = function() {
    $(".image-swap").find(".product-block").each(function () {
        var productBlock = $(this);
        var newSrc = productBlock.find("input[name=swapImageUrl]").val();
        if (newSrc != null) {
            var imgTag = productBlock.find(".product-image-link img");
            var currentSrc = imgTag.attr("src");
            imgTag.parent().closest("div").on({
                mouseover: function () {
                    imgTag.attr('src', newSrc);
                },
                mouseout: function () {
                    imgTag.attr('src', currentSrc);
                }
            });
        }
    });
}