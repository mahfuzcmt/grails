$(function() {
    var container = $.find(".event-container")

    function manageSeatSelection(container, event, session, section) {
        container.find(".seat").click(function() {
            var _self = $(this)
            if(_self.hasClass("selected")) {
                _self.removeClass("selected")
            } else {
                bm.ajax({
                    url: app.baseUrl + "event/checkAvailabilityOfSeat",
                    data: {
                        section: section,
                        event: event,
                        session: session,
                        seat: _self.attr("seat-number")
                    },
                    success: function() {
                        _self.addClass("selected")
                    }
                })
            }
        })
    }

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
        container.find(".ticket-add-to-cart-button").click(function() {
            var event = container.find("[name='event']").val(),
                session = container.find("[name='eventSession']").val()
            var _self = $(this).closest(".section-info-view")
            var cartQuantity = _self.find(".ticket-quantity-selector").val()
            var section = _self.find("[name='section']").val()
            bm.ajax({
                url: app.baseUrl + "cart/loadCartPopup",
                dataType: 'html',
                data: {
                    section: section,
                    event: event,
                    session: session,
                    orderedQuantity: cartQuantity
                },
                type: 'post',
                success: function(resp) {
                    var content = $(resp);
                    content.find(".close-popup, .continue-shopping-btn").click(function() {
                        site.global_single_popup.close();
                    })
                    content.find(".final-ticket-add-to-cart").click(function() {
                        var seatList = []
                        var selectedSeats = content.find(".seat.selected")
                        $.each(selectedSeats, function(i, elm) {
                            seatList.push($(elm).attr("seat-number"))
                        })
                        bm.ajax({
                            controller: "cart",
                            action: "addTicket",
                            dataType: "html",
                            data: {
                                section: section,
                                seats: seatList,
                                event: event,
                                session: session
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
                    })
                    site.global_single_popup = content.popup({
                        is_fixed: true,
                        is_always_up: true,
                        drag_handle: ".header"
                    }).obj(POPUP)
                    content.find(".body").css({
                        maxWidth: 800,
                        maxHeight: 650
                    })
                    manageSeatSelection(content, event, session, section)
                }
            });
        })
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