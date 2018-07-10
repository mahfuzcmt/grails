app.productWidgets = {
    initImageWidget: function(widget) {
        //thumbnail image change on click
        var thumbImageCache = new RegExp("^" + widget.find("#thumb-image-size-cache").val() + "-")
        var detailImageCache = widget.find("#detail-image-size-cache").val()
        if(!widget.length) {
            return
        }
        var imageThumbContainer = widget.find(".image-thumb-container")
        var detailImage = widget.find(".image-preview-box img");
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
        widget.find(".image-thumb-container").scrollbar({
            show_vertical: false,
            show_horizontal: true,
            use_bar: false,
            visible_on: "auto",
            horizontal: {
                handle: {
                    left: widget.find(".multi-image-scroll-wrapper .image-left-scroller"),
                    right: widget.find(".multi-image-scroll-wrapper .image-right-scroller")
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
        var zoomType = widget.find("input#product_image_zoom_type").val();
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
            widget.find(".multi-image-scroll-wrapper").scrollbar("update", true);
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

        var currentIndex = 1
        var initialIndex = 1;
        var imageWrapper = widget.find(".multi-image-scroll-wrapper")
        var maxIndex = +imageWrapper.attr("image-size")
        maxIndex = maxIndex || 1;
        var imgDiv = widget.find(".image-preview-box");
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
                is_always_up: true,
                is_fixed: true
            });
            var dom = $(popupDom);
            popup.append(dom)
            popup.find(".popup-image").on("load", function () {
                popup.removeClass("loading").obj(POPUP).position()
            });
            isActive = true;
            var reload = function () {
                popup.find(".left-button").show();
                popup.find(".right-button").show();
                var image = widget.find(".multi-image-scroll-wrapper .thumb-image[index=" + currentIndex + "]")
                if (currentIndex === maxIndex) {
                    popup.find(".right-button").hide();
                }
                if (currentIndex === initialIndex) {
                    popup.find(".left-button").hide();
                }
                var src = image.length >= 1 ? app.baseUrl + imagePreviewBox.attr("infix-url") + (popImageSize ? (popImageSize + "-") : "") + image.attr("image-name") : _this.find("img").data("zoom-image");
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
    },
    initCartWidget: function(widget) {
        var cartButton = widget.find(".add-to-cart-button"), quantitySelector = widget.find(".product-quantity-selector.text-type").numeric();
        quantitySelector.on("change spinchange", function () {
            cartButton.config("cart", {quantity: this.value})
        })
    },
    initLikeUsWidget: function(widget, productId){
        widget.find(".tell-friend").click(function() {
            tellAFriendAboutProduct(page.productId)
        });
    },
    initVideoWidget: function(productVideoContainer) {
        if (productVideoContainer.length > 0) {
            bm.onReady(window, "videojs", function() {
                function bindVideoJsPlayer($this, isPlay) {
                    var videoUrl = $this.attr("video-ref");
                    var player = videojs(productVideoContainer.find('.my-video')[0], { "controls": true, "autoplay": false, "preload": "auto", "fluid": true});
                    player.ready(function() {
                        player.src(videoUrl);
                        if(isPlay == true)
                            player.play();
                    });
                    productVideoContainer.find(".video-thumbnail.active").removeClass("active");
                    $this.find(".video-thumbnail").addClass("active");
                }
                productVideoContainer.find(".multiple-video-thumbs .thumb-video").on("click", function () {
                    var $this = $(this);
                    bindVideoJsPlayer($this, true);
                });
                bindVideoJsPlayer(productVideoContainer.find(".multiple-video-thumbs .thumb-video:eq(0)"), false);
            });
        }
    },
    initCombinedWidget: function(combination, productId) {
        if(combination.length) {
            initializeCombination(productId, combination)
        }
    },
    init: function(container, productId) {
        var _w = this
        _w.initImageWidget(container.find(".widget-productImage"))
        _w.initCartWidget(container.find(".widget-addCart"))
        _w.initLikeUsWidget(container.find('.widget-likeus'), productId)
        _w.initCartWidget(container.find("#bmui-tab-video"))
        _w.initCombinedWidget(container.find(".included-products-container"), productId)
        _w.initVideoWidget(container.find("#bmui-tab-video"))
    }
};

$(function () {
    if(window.page && page.productId) {
        app.productWidgets.init($("body"), page.productId)
    }
});
