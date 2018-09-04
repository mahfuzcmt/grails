/**
 * Created by shajalal on 11/03/2015.
 */
$(function(){
    bm.onReady($.prototype, "raty", function() {
        $(".rating").raty({
            half: true,
            path: app.baseUrl + "plugins/filter/images/raty",
            score: function() {
                return $(this).attr('score');
            }
        });
    })
});
