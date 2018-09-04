site.hook.register("prepareAddCartData", function(data, container, productId, quantity, priceOnly) {
    if(!priceOnly) {
        var form = container.find(".custom-field-container")
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
})