$(function() {
    $(".widget-filter form").form({
        ajax: false
    });
    var form = $(".widget-filter form");
    form.find(".filter-profile").on("change", function(){
        form.submit();
    });
});