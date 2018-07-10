<g:set var="imageWidth" value="${config.displayPage == 'double' ? config.width.toInteger()/2 - 22 : config.width.toInteger() - 44}"/>
<div class="flipbook-canvas">
    <div class="flipbook-viewport">
        <div class="container">
            <div class="flipbookItem ${config.displayPage} ${config.flippingDirection}" id="flipbook-instance-${widget.id}">
                <div ignore="1" class="previous-button navigation-button"></div>
                <g:each in="${items}" status="i" var="image">
                    <div class="flipbook-page ${config.zooming == 'true' ? 'zoom-enabled' : ''}" style="display: none;">
                        <img alt="${image.altText}" src="${appResource.getAlbumImageURL(image: image)}" style="max-width: ${imageWidth}px; max-height: ${config.height}px">
                        <g:if test="${config.showGradients == 'true'}">
                            <div class="gradient"></div>
                        </g:if>
                    </div>
                </g:each>
                <div ignore="1" class="next-button navigation-button"></div>
            </div>
        </div>
    </div>
    <g:if test="${config.thumbnails == 'true'}">
        <div class="thumb-container" style="width: ${config.width}px">
            <span class="navigator left-navigator"></span>
            <div class="thumb-display-container">
                <g:set var="maxIndex" value="${items.size() - 1}"/>
                <g:if test="${config.displayPage == 'double'}">
                    <g:set var="i" value="${0}"/>
                    <g:while test="${i <= maxIndex}">
                        <g:set var="image" value="${items[i]}"/>
                        <g:if test="${i == 0 || i == maxIndex}">
                            <div class="single image-block-container">
                                <div class="image-block">
                                    <img alt="${image.altText}" src="${appResource.getAlbumImageURL(image: image, sizeOrPrefix: "thumb")}" page="${i + 1}" title="Page ${i + 1}">
                                </div>
                                <span>${i + 1}</span>
                            </div>
                            <g:set var="i" value="${i + 1}"/>
                        </g:if>
                        <g:else>
                            <div class="double image-block-container">
                                <div class="image-block">
                                    <img alt="${image.altText}" src="${appResource.getAlbumImageURL(image: image, sizeOrPrefix: "thumb")}" page="${i + 1}" title="Page ${i + 1}-${i + 2}">
                                    <g:set var="image" value="${items[i + 1]}"/>
                                    <img alt="${image.altText}" title="Page ${i + 1}-${i + 2}" page="${i + 2}" src="${appResource.getAlbumImageURL(image: image, sizeOrPrefix: "thumb")}">
                                </div>
                                <span>${i + 1}-${i + 2}</span>
                            </div>
                            <g:set var="i" value="${i + 2}"/>
                        </g:else>
                    </g:while>
                </g:if>
                <g:else>
                    <g:each in="${items}" status="i" var="image">
                        <div class="single image-block-container">
                            <div class="image-block">
                                <img alt="${image.altText}" src="${appResource.getAlbumImageURL(image: image, sizeOrPrefix: "thumb")}" page="${i + 1}" title="Page ${i + 1}">
                            </div>
                            <span>${i + 1}</span>
                        </div>
                    </g:each>
                </g:else>
            </div>
            <span class="navigator right-navigator"></span>
        </div>
    </g:if>
</div>
<style>
    #flipbook-instance-${widget.id} .flipbookItem.double.rtl .next-button.single-view {
        left: ${imageWidth}px;
        right: auto;
    }
    #flipbook-instance-${widget.id} .flipbookItem.double.rtl .previous-button.single-view {
        right: ${imageWidth}px;
        left: auto;
    }
</style>
<g:if test="${request.page}">
    <%
        if(!request.is_flip_book_loaded) {
            request.js_cache.push("plugins/gallery-flip-book/js/turnjs/zoom.min.js")
            request.js_cache.push("plugins/gallery-flip-book/js/turnjs/turn.min.js")
            request.css_cache.push("plugins/gallery-flip-book/css/flipbook.css")
            request.is_flip_book_loaded = true;
        }
    %>

    <script type="text/javascript">
        function loadFlipbook_${widget.id}() {
            try {
                function onTurn(leftPage, rightPage) {
                    leftPage = leftPage || 1;
                    rightPage = rightPage || totalPageCount;
                    if(options.display == "double") {
                        if(leftPage == rightPage) {
                            flipbook.find('.navigation-button').addClass("single-view");
                        } else {
                            flipbook.find('.navigation-button').removeClass("single-view");
                        }
                    }
                    if(leftPage == 1) {
                        flipbook.find('.previous-button').hide()
                    } else {
                        flipbook.find('.previous-button').show()
                    }
                    if(rightPage == totalPageCount) {
                        flipbook.find('.next-button').hide()
                    } else {
                        flipbook.find('.next-button').show()
                    }
                    <g:if test="${config.thumbnails == 'true'}">
                    var thumbContainer = flipbook.closest(".flipbook-canvas").find(".thumb-container");
                    thumbContainer.find(".image-block-container.active").removeClass("active")
                    thumbContainer.find("img[page=" + leftPage + "]").parents(".image-block-container").addClass("active");
                    </g:if>
                }

                var totalPageCount = ${items.size()};
                var flipbook = $("#flipbook-instance-" + ${widget.id});
                var options = {
                    width: ${config.width} - 44,
                    height: ${config.height},
                    elevation: 50,
                    inclination: 500,
                    gradients: ${config.showGradients ?: false},
                    autoCenter: ${config.autoCenter?: false},
                    acceleration: true,
                    direction: "${config.flippingDirection}",
                    page: ${config.startPage ?: 1},
                    display: "${config.displayPage}",
                    duration: ${config.transDuration ?: 100},
                    when: {
                        turned: function(a, b, pages) {
                            onTurn(pages[0], pages[pages.length - 1]);
                        }
                    }
                };
                if(options.page > totalPageCount) {
                    options.page = totalPageCount;
                }
                flipbook.find(".flipbook-page").show();
                flipbook.turn(options).turn("peel", "tr");
                flipbook.find('.navigation-button').css('height', ${config.height});
                flipbook.find('.navigation-button').bind($.mouseEvents.over, function() {
                    $(this).addClass('hover');
                }).bind($.mouseEvents.out, function() {
                            $(this).removeClass('hover');
                        }).bind($.mouseEvents.down, function() {
                            $(this).addClass('mouse-down');
                        }).bind($.mouseEvents.up, function() {
                            $(this).removeClass('mouse-down');
                        }).click(function() {
                            if($(this).is(".next-button")) {
                                flipbook.turn('next');
                            } else {
                                flipbook.turn('previous');
                            }
                        });
                <g:if test="${config.thumbnails == 'true'}">
                var thumbBlock = flipbook.closest(".flipbook-canvas").find(".thumb-container");
                thumbBlock.find("img").click(function() {
                    var page = $(this).attr("page");
                    flipbook.turn("page", page);
                });
                thumbBlock.find(".image-block-container").hover(function() {
                    $(this).addClass("hover");
                }, function() {
                    $(this).removeClass("hover");
                });

                thumbBlock.find(".thumb-display-container").scrollbar({
                    show_vertical: false,
                    show_horizontal: true,
                    use_bar: false,
                    visible_on: "auto",
                    horizontal: {
                        handle: {
                            left: thumbBlock.find(".navigator.left-navigator"),
                            right: thumbBlock.find(".navigator.right-navigator")
                        }
                    }
                })

                </g:if>
                var zoomState = false;
                <g:if test="${config.zooming == 'true'}">
                var peelState = true;
                flipbook.bind("start", function(a, b, pages) {
                    peelState = true;
                    flipbook.removeClass("zoomable");
                });
                flipbook.bind("end", function(a, b, pages) {
                    peelState = false;
                    flipbook.addClass("zoomable");
                });
                flipbook.click(function() {
                    if(peelState) {
                        return;
                    }
                    var pages = flipbook.turn("view");
                    var selector = "";
                    $.each(pages, function() {
                        selector += ",.p" + this + " img";
                    });
                    pages = flipbook.find(selector.substring(1));
                    var overlay = $("<div class='full-block-overlay flipbook-zoom-overlay'></div>").css({
                        position: "fixed",
                        left: 0,
                        right: 0,
                        top: 0,
                        bottom: 0
                    }).appendTo(document.body);
                    var clonePages = pages.clone();
                    pages.each(function(index) {
                        var offset = $(this).offset();
                        var cachedCss = {
                            top: offset.top - $(document).scrollTop(),
                            left: offset.left - $(document).scrollLeft(),
                            width: $(this).width(),
                            height: $(this).height()
                        };
                        clonePages.eq(index).removeAttr("style").addClass("zoomed").css({
                            position: "fixed"
                        }).css(cachedCss).data("css", cachedCss).addClass($(this).closest(".page").is(".odd") ? "odd" : "even");
                    });
                    var wrapper = $("<span class='flipbook-zoom-wrapper'></span>").appendTo(document.body);

                    wrapper.append(clonePages);

                    var totalWidth = overlay.width();
                    var totalHeight = overlay.height();
                    var middlePostion = totalWidth / 2;
                    var possibleImageWidth = totalWidth;
                    var imageWidth = ${imageWidth};
                    var totalImageWidth = imageWidth;
                    var imageHeight = ${config.height};
                    if(pages.length == 2) {
                        possibleImageWidth = middlePostion;
                        totalImageWidth = totalImageWidth * 2;
                    }
                    var scaleFactor;
                    var wFactor = possibleImageWidth / imageWidth;
                    var hFactor = totalHeight / imageHeight;
                    if(hFactor < wFactor) {
                        scaleFactor = hFactor;
                    } else {
                        scaleFactor = wFactor;
                    }
                    var filledWidth = totalImageWidth * scaleFactor;
                    var filledHeight = imageHeight * scaleFactor;
                    var wPad = (totalWidth - filledWidth)/2;
                    var hPad = (totalHeight - filledHeight)/2;
                    if(pages.length == 1) {
                        middlePostion = wPad;
                    }
                    clonePages.each(function() {
                        var page = $(this);
                        var oldCss = page.data("css");
                        var wGap = (imageWidth - oldCss.width)/2 * scaleFactor;
                        var newCss = {
                            width: oldCss.width * scaleFactor,
                            height: oldCss.height * scaleFactor,
                            left: wGap + (options.direction == "rtl" ? (page.is(".even") ? middlePostion : wPad) : (page.is(".odd") ? middlePostion : wPad)  ),
                            top: hPad + (imageHeight - oldCss.height)/2 * scaleFactor
                        }
                        page.data("animateTo", newCss);
                    });
                    clonePages.each(function() {
                        var page = $(this);
                        page.animate(page.data("animateTo"));
                        zoomState = true;
                    });
                    overlay.add(wrapper).click(function() {
                        clonePages.each(function() {
                            var page = $(this);
                            page.animate(page.data("css"), function() {
                                overlay.remove();
                                wrapper.remove();
                                zoomState = false;
                            });
                        });
                    })
                });
                </g:if>



                if(options.display == "double") {
                    if(options.page % 2 == 0) {
                        var nextPage = options.page + 1
                        if(nextPage > totalPageCount) {
                            nextPage = 0;
                        }
                        onTurn(options.page, nextPage);
                    } else {
                        onTurn(options.page - 1, options.page);
                    }
                } else {
                    onTurn(options.page, options.page);
                }

                $(document).keydown(function(e){
                    if(zoomState) {
                        return;
                    }
                    var previous = 37, next = 39, esc = 27;
                    switch (e.keyCode) {
                        case previous:
                            flipbook.turn('previous');
                            break;
                        case next:
                            flipbook.turn('next');
                            break;
                    }
                });
            } catch (e) {
                console.log(e)
            }
        }

        $(function() {
            bm.onReady($.prototype, "turn", function () {
                loadFlipbook_${widget.id}();
            });
        });
    </script>

</g:if>