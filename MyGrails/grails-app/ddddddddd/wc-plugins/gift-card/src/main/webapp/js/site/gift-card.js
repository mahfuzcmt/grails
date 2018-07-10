site.hook.register("prepareAddCartData", function(data, container, productId, quantity, priceOnly) {
    if(!priceOnly) {
        var form = container.find(".gift-card-fields");
        if(form.length) {
            if(form.valid({
                    validate_on_call_only: true
                })) {
                $.extend(data, form.serializeObject())
            } else {
                throw $.error("invalid data")
            }
        }
    }
    return data;
});

app.global_event.on("initialize-info-chose-popup", function(evt, productId, content) {
    bm.autoToggle(content)
    bm.initCountryChangeHandler(content.find(".country-row select"),  "gift_card.stateId");
    bm.initCityValidator(content.find('[name="gift_card.postCode"]'), "gift_card.countryId", "gift_card.stateId", content.find(".gift-card-fields"), "gift_card.city");
});

$(function () {
    if(window.page && page.productId) {
        app.productWidgets.initGiftCardWidget = function (giftCardInfo) {
            bm.autoToggle(giftCardInfo)
            bm.initCountryChangeHandler(giftCardInfo.find(".country-row select"), "gift_card.stateId");
            bm.initCityValidator(giftCardInfo.find('[name="gift_card.postCode"]'), "gift_card.countryId", "gift_card.stateId", giftCardInfo, "gift_card.city");

        };
        app.productWidgets.initGiftCardWidget($(".gift-card-fields"));
    }
});
