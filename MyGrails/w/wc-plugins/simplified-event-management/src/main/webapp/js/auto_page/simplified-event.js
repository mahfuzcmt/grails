$(function() {
    var container = $.find(".event-container")


    function manageImageSelection(container) {
        var eventImageContainer = container.find(".multi-image-scroll-wrapper")
        eventImageContainer.scrollbar({
            show_vertical: false,
            show_horizontal: true,
            use_bar: true,
            visible_on: "auto",
            horizontal: {
                handle: {
                    left: eventImageContainer.find(".image-left-scroller"),
                    right: eventImageContainer.find(".image-right-scroller")
                }
            }
        })

        var image = eventImageContainer.find(".event-thumb-image-view img")
        $(image).click(function() {
            var originalSrc = $(this).closest(".thumb-image").attr("original-src")
            $(".event-image-preview-box img").attr("src", originalSrc)
        })
    }

    function manageAddToCart(container) {
        var second_popup;
        container.find(".event-ticket-add-to-cart-button").click(function() {
            var event = container.find("[name='event']").val()
            var seatList = container.find(".ticket-quantity-selector").val()
            bm.ajax({
                controller: "cart",
                action: "addTicket",
                dataType: "html",
                data: {
                    seats: seatList,
                    event: event
                },
                success: function(resp) {
                    var content = $(resp);
                    content.find(".close-popup, .continue-shopping-btn").click(function () {
                        second_popup.close();
                    })
                    second_popup = content.popup({
                        is_fixed: true,
                        is_always_up: true,
                        modal: false,
                        auto_close: 40000,
                        clazz: "add-to-cart-popup"
                    }).obj(POPUP)
                    site.global_single_popup.close();
                    app.global_event.trigger("update-cart");
                }
            })
        });
    }

    function initEventDetails(container) {
        var container = $(container)
        manageImageSelection(container)
        manageAddToCart(container)
        container.find(".ticket-quantity-selector.text-type").each(function() {
            var spin = $(this);
            var data = spin.config("spin")
            spin.stepper(data)
        })
    }
    initEventDetails(container)
});