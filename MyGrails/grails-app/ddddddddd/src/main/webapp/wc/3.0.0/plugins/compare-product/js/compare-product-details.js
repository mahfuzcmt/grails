/**
 * Created by sajedur on 9/07/2014.
 */
$(function() {
    $(".compare-details .action-row span.remove").on("click", function() {
        var $this = $(this);
        var productId = $this.attr("product-id")
        bm.ajax({
            url: app.baseUrl + "compareProduct/removeFromCompare",
            data: { productId: productId},
            success: function() {
                location.reload();
            }
        })
    })
    app.global_event.on("product-compare-update", function() {
        location.reload();
    });
})