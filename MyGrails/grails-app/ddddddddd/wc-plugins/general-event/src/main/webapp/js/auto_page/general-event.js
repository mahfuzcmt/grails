$(function() {
    var container = $(".event-container")
    var eventId = container.find("input[name=eventId]").val();
    var manageSeatSelection = function(container, eventId, sectionId, isRecurring) {
        container.find(".seat").click(function() {
            var _self = $(this)
            if(_self.hasClass("selected")) {
                _self.removeClass("selected")
            } else {
                bm.ajax({
                    url: app.baseUrl + "generalEvent/checkIfSeatIsAvailable",
                    data: {
                        sectionId: sectionId,
                        eventId: eventId,
                        seat: _self.attr("seat-number"),
                        isRecurring: isRecurring
                    },
                    success: function() {
                        _self.addClass("selected");
                    },
                    error: function() {
                        var msg = $('<span class="error-message message-block message">'+arguments[2].message+'</span>');
                        $($.find('.section-seat-view')).prepend(msg);
                        setTimeout(function() {
                            msg.fadeOut(1000);
                        }, 2000)
                    }
                });
            }
        });
    };

    /*var manageImageSelection = function(container) {
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
        });

        var image = eventImageContainer.find(".event-thumb-image-view img");
        $(image).click(function() {
            var originalSrc = $(this).closest(".thumb-image").attr("original-src")
            $(".event-image-preview-box img").attr("src", originalSrc)
        });
    };*/

    var attachEventImage = function(eventImageContainer) {

        var thumbImageCache = new RegExp("^" + $("#thumb-image-size-cache").val() + "-")
        var detailImageCache = $("#detail-image-size-cache").val();
        if(!eventImageContainer.length) {
            return
        }
        var imageThumbContainer = eventImageContainer.find(".image-thumb-container")
        var detailImage = eventImageContainer.find(".image-preview-box img");
        imageThumbContainer.find("img").click(function () {
            var $this = $(this);
            imageThumbContainer.find(".thumb-image.active").removeClass("active")
            $this.parents(".thumb-image").addClass("active")
            var src = bm.path(this.src);
            var zoomUrl = $(this).data("zoom-image");
            src.name = src.name.replace(thumbImageCache, detailImageCache + "-");
            detailImage.attr("src", src.full())
            detailImage.attr("alt", $this.attr("alt"))
            detailImage.data("zoom-image", zoomUrl)
            if(detailImage.data('elevateZoom')) {
                detailImage.elevateZoom('refresh')
            }
        })

        //thumb scroll
        eventImageContainer.find(".image-thumb-container").scrollbar({
            show_vertical: false,
            show_horizontal: true,
            use_bar: false,
            visible_on: "auto",
            horizontal: {
                handle: {
                    left: eventImageContainer.find(".multi-image-scroll-wrapper .image-left-scroller"),
                    right: eventImageContainer.find(".multi-image-scroll-wrapper .image-right-scroller")
                }
            }
        })

        //image zooming
        var bodySection = $(".body");
        var imagePreviewBox = $(".image-preview-box");
        if(!imagePreviewBox.length) {
            //possibly in this layout no page content is assigned
            return;
        }

        var multiplier = app.config.product_zoom_box_dimension.substring(0, app.config.product_zoom_box_dimension.length - 1);
        var zoomConfig = {};
        var zoomType = $("input#event_image_zoom_type").val();
        var outerZoom = true;
        if(zoomType == 'tints') {
            zoomConfig.tint = true;
            zoomConfig.tintColour ='#F90';
            zoomConfig.tintOpacity = 0.5;
            outerZoom = true;
        } else if(zoomType == 'inner') {
            zoomConfig.zoomType = 'inner'
        } else if(zoomType == 'lens') {
            zoomConfig.zoomType = 'lens';
            zoomConfig.lensShape = "round";
        } else if(zoomType == 'fade_in_out') {
            zoomConfig.zoomWindowFadeIn = 500;
            zoomConfig.zoomWindowFadeOut = 500;
            outerZoom = true
        } else if(zoomType == 'easing') {
            zoomConfig.easing = true;
            outerZoom = true
        } else if(zoomType == 'mousewheel') {
            zoomConfig.scrollZoom = true;
            zoomConfig.zoomType = 'lens'
        } else if(zoomType == 'mousewheel_inner') {
            zoomConfig.scrollZoom = true;
            zoomConfig.zoomType = 'inner'
        }  else if(zoomType == 'mousewheel_lens') {
            zoomConfig.scrollZoom = true;
            zoomConfig.zoomType = 'lens'
        }

        function zoomImage() {
            var leftWidth = imagePreviewBox.offset().left - bodySection.offset().left;
            var rightWidth = bodySection.outerWidth() - leftWidth - imagePreviewBox.outerWidth()
            var height = imagePreviewBox.innerHeight();
            var position = leftWidth > rightWidth ? 11 : 1;
            var width =  imagePreviewBox.outerWidth();
            var availableWidth = leftWidth > rightWidth ? leftWidth : rightWidth;
            zoomConfig.zoomWindowPosition = position;
            zoomConfig.zoomWindowWidth = width * multiplier;
            zoomConfig.zoomWindowHeight = height * multiplier;

            if(outerZoom && zoomConfig.zoomWindowWidth > availableWidth) {
                return false;
            }
            bm.onReady($.prototype, "elevateZoom", {
                ready: function() {
                    detailImage.elevateZoom(zoomConfig)
                }
            })
        }
        zoomImage();
        $(window).on("resize." + this.id, function () {
            eventImageContainer.find(".multi-image-scroll-wrapper").scrollbar("update", true);
            detailImage.elevateZoom("destroy");
            zoomImage();
        });


        //image popup
        var popup = $('<div class="image-popup-container"></div>');
        var popupDom = '<div class="image-popup-mask"></div>' +
            '<span class="image-wrapper">' +
            '<span class="close-button navigator"></span>' +
            '<span class="left-button navigator"></span>' +
            '<span class="right-button navigator"></span>' +
            '<span class="image-loader"></span>' +
            '<img class="popup-image">' +
            '</span>';

        var currentIndex = 1;
        var initialIndex = 1;
        var imageWrapper = eventImageContainer.find(".multi-image-scroll-wrapper")
        var maxIndex = +imageWrapper.attr("image-size")
        maxIndex = maxIndex || 1;
        var imgDiv = eventImageContainer.find(".image-preview-box");
        var isActive = false;
        var popImageSize = imageWrapper.find("#popup-image-size-cache").val()
        imgDiv.click(function() {
            var _this = $(this);
            if(isActive) {
                return;
            }
            var currentImage = imageWrapper.find(".thumb-image.active")
            currentIndex = +currentImage.attr("index")
            currentIndex = currentIndex ? currentIndex : 1;
            popup.addClass("loading")
            popup.popup({
                content: popup,
                is_fixed: true
            });
            var dom = $(popupDom);
            popup.append(dom)
            popup.find(".popup-image").load(function () {
                popup.removeClass("loading").obj(POPUP).position()
            });
            isActive = true;
            var reload = function () {
                popup.find(".left-button").show();
                popup.find(".right-button").show();
                var image = eventImageContainer.find(".multi-image-scroll-wrapper .thumb-image[index=" + currentIndex + "]")
                if (currentIndex === maxIndex) {
                    popup.find(".right-button").hide();
                }
                if (currentIndex === initialIndex) {
                    popup.find(".left-button").hide();
                }
                var src = image.length >= 1 ? app.baseUrl + "resources/general-event/event-" + eventId + "/images/" + (popImageSize ? (popImageSize + "-") : "") + image.attr("image-name") : _this.find("img").data("zoom-image");
                popup.addClass("loading").find(".popup-image").attr("src", src)
            };
            reload();
            popup.find(".close-button").click(function () {
                popup.obj(POPUP).close();
                isActive = false;
            });
            popup.find(".left-button,.right-button").unbind("click");
            popup.find(".left-button").click(function () {
                currentIndex--;
                reload();
            });
            popup.find(".right-button").click(function () {
                currentIndex++;
                reload();
            });
        });
        $(window).resize(function () {
            if(isActive) {
                popup.obj(POPUP).close();
            }
            isActive = false;
        })
    }

    var manageAddToCart = function(container) {
        var second_popup;
        var isRecurring = container.find("[name='isRecurring']").val()
        container.find(".section-ticket-add-to-cart-button").click(function() {
            var _self = $(this).closest(".section-info-view");
            var cartQuantity = _self.find(".ticket-quantity-selector").val();
            cartQuantity = cartQuantity >= 1 ? cartQuantity : 1;
            var sectionId = _self.find("[name='section']").val();
            bm.ajax({
                url: app.baseUrl + "cart/loadCartTicketPopup",
                dataType: 'html',
                data: {
                    sectionId: sectionId,
                    eventId: eventId,
                    orderedQuantity: cartQuantity,
                    isRecurring: isRecurring
                },
                type: 'post',
                success: function(resp) {
                    var content = $(resp);
                    content.find(".close-popup, .continue-shopping-btn").click(function() {
                        site.global_single_popup.close();
                    });
                    content.find(".final-ticket-add-to-cart").click(function() {
                        var seatList = []
                        var selectedSeats = content.find(".seat.selected");
                        $.each(selectedSeats, function(i, elm) {
                            seatList.push($(elm).attr("seat-number"));
                        });
                        if(!seatList) {
                            return
                        }
                        bm.ajax({
                            controller: "cart",
                            action: "addVenueTicket",
                            dataType: "html",
                            data: {
                                sectionId: sectionId,
                                seats: seatList,
                                eventId: eventId,
                                isRecurring: isRecurring
                            },
                            success: function(resp) {
                                var content = $(resp);
                                content.find(".close-popup, .continue-shopping-btn").click(function () {
                                    second_popup.close();
                                });
                                second_popup = content.popup({
                                    is_fixed: true,
                                    is_always_up: true,
                                    modal: false,
                                    auto_close: 40000,
                                    clazz: "add-to-cart-popup"
                                }).obj(POPUP);
                                site.global_single_popup.close();
                                app.global_event.trigger("update-cart");
                            }
                        });
                    });
                    site.global_single_popup = content.popup({
                        is_fixed: true,
                        is_always_up: true,
                        drag_handle: ".header"
                    }).obj(POPUP)
                    content.find(".body").css({
                        maxWidth: 800,
                        maxHeight: 650
                    })
                    manageSeatSelection(content, eventId, sectionId, isRecurring)
                }
            });
        });
        container.find(".event-ticket-add-to-cart-button").click(function() {
            var seats = container.find(".ticket-quantity-selector").val()
            seats = seats >= 1 ? seats : 1
            bm.ajax({
                controller: "cart",
                action: "addTicketToCart",
                dataType: "html",
                data: {
                    seats: seats,
                    eventId: eventId,
                    isRecurring: isRecurring
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
    };

    ////////////////////// INIT EVENT DETAILS ////////////////////
    (function(container) {
        var container = $(container)
        attachEventImage(container);
        manageAddToCart(container)
        container.find(".ticket-quantity-selector.text-type").each(function() {
            var spin = $(this);
            var data = spin.config("spin")
            spin.stepper(data)
        })
    })(container)

});