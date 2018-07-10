site.hook.register("prepareAddCartData", function(data, container, productId, quantity, priceOnly) {
    if(container.closest(".gift-registry-details").length == 0) {
        return;
    }
    var giftRegistryId = $(".gift-registry-details [name=giftRegistryId]").val();
    var giftId = container.find("[name=giftItemId]").val();
    data.giftRegistryId = giftRegistryId;
    data.giftItemId = giftId;
});

$(function() {
    $("a:not(.ui-spinner-button)").on("click", function(event) {
        event.preventDefault();
        var $this = $(this);
        var confirmed = false;
        bm.confirm($.i18n.prop("do.you.want.to.visit.product.details.page.to.make.purchase"), function() {
            confirmed = true;
        }, function(){})
        function checkConfirm() {
            if(!confirmed) {
                setTimeout(checkConfirm, 500)
            } else {
                var destination = $this.attr("href")
                if(destination)
                    window.location.href = destination;
            }
        }
        checkConfirm();
    })
})