$(function(){
    bm.onReady($.prototype, "raty", function() {
        $(".review-rating").raty({
            half: true,
            path: app.systemResourceUrl + "plugins/product-review/images/raty",
            score: function() {
                return $(this).attr('score');
            },
            readOnly: function() {
                return $(this).hasClass('read-only')
            }
        });
    })
});