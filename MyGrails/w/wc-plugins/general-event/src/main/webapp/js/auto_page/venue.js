$(function() {
    initLocationDetails()
});

function initLocationDetails() {
    var imageContainer = $.find(".location-thumb-image")
    var image = $.find(".location-thumb-image img")
    $(image).click(function(){
        var originalSrc = $(this).closest(".location-thumb-image").attr("original-src")
        $(".location-image-preview-box img").attr("src", originalSrc)
    })
}