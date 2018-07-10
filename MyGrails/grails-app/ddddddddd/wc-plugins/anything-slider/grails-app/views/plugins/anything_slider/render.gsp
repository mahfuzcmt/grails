<%@ page import="com.webcommander.plugin.anything_slider.AnythingSliderConstants" %>
<g:set var="theme" value="${config.theme}"/>
<g:set var="widgetId" value="${widget.id}"/>
<%
    if(request.page && !request.is_anything_slider_loaded) {
        request.css_cache.push("plugins/anything-slider/css/anythingslider.css")
        AnythingSliderConstants.THEME.each {
            request.css_cache.push("plugins/anything-slider/css/themes/theme-"+ it.key + ".css")
        }
        request.js_cache.push("plugins/anything-slider/js/easing/jquery.easing.1.3.js")
        request.js_cache.push("plugins/anything-slider/js/slider/jquery.anythingslider.min.js")
        request.is_anything_slider_loaded = true
    }
%>

<div id="anything-wrapper-${widgetId}" class="${config.show_thumbnails == "true" ? "with-thumb" : ""}${config.show_caption == "true" ? " with-caption" : ""}">
    <ul id="slider-${widgetId}" class="anything-slider" album="${config.album}">
        <g:each in="${items}" status="i" var="image">
            <li class="panel">
                <img alt="${image.altText}" src="${appResource.getAlbumImageURL(image: image)}"
                     name="${image.name.encodeAsBMHTML()}" ${config.hyperlink_images == "true" ? ("target='" + image.linkTarget + "' href='" + (image.imageLink() ?: "#") + "'") : ""}>
                <g:if test="${config.show_caption == "true"}"><div class="caption-bottom">${image.altText.encodeAsBMHTML()}</div></g:if>
                <g:else><span></span></g:else>
            </li>
        </g:each>
    </ul>
</div>

<g:if test="${request.page}">
    <script type="text/javascript">
        bm.onReady($, "anythingSlider", function () {
            var sliderPanel = $("#slider-${widgetId}")
            var images = []
            sliderPanel.find("img").each(function() {images.push($(this).attr("name"))})
            $('#anything-wrapper-${widgetId}').css({width: sliderPanel.closest('.widget-gallery').parent().width(), minHeight: ${config.height}})
            sliderPanel.anythingSlider({
                        expand: true,
                        aspectRatio: false,%{--${config.show_caption == "true" ? "false" : "true"},--}%
                        theme: "${config.theme}",
                        mode: "${config.mode}",
                        showMultiple: ${config.show_multiple == "true" && config.mode == "h" ? config.slide_at_once : "false"},
                        easing: "${config.transition_effect}",
                        buildArrows: ${config.build_arrows},
                        buildNavigation: ${config.build_navigation},
                        buildStartStop: ${config.build_start_stop},
                        toggleArrows: ${config.toggle_arrows},
                        toggleControls: ${config.toggle_controls},
                        startText: "${config.start_text.encodeAsBMHTML()}",
                        stopText: "${config.stop_text.encodeAsBMHTML()}",
                        forwardText: "&raquo;",
                        backText: "&laquo;",
                        enableNavigation: ${config.build_navigation},
                        enableKeyboard: ${config.enable_keyboard},
                        startPanel: ${config.start_panel},
                        hashTags: false,
                        infiniteSlides: ${config.infinite_slides},
                        navigationFormatter: function (index, panel) {
                            return ${config.show_thumbnails == "true" ? "'<img src=\"${app.customResourceBaseUrl()}resources/albums/album-${config.album}/thumb-' + images[index - 1] + '\">'" : "images[index - 1]"}
                        },
                        navigationSize: ${config.build_navigation == "true" ? config.navigation_size : "false"},
                        autoPlay: ${config.auto_play},
                        pauseOnHover: ${config.pause_on_hover},
                        playRtl: ${config.play_rtl},
                        delay: ${config.delay},
                        onInitialized: function(e, slider) {
                            var sliderItems = $(slider.$items)
                            sliderItems.find('div[class*=caption]').css({ position: 'absolute' })
                            sliderItems.find("img").each(function() {
                                var _this = $(this)
                                if(_this.attr("href")) {
                                    _this.wrap("<a href='"+ _this.attr("href") +"' target='"+ _this.attr("target") +"'></a>")
                                }
                            })
                        },
                        onSlideComplete: function(slider) {
                            showCaptions($(slider.$currentPage))
                        },
                        onSlideInit: function(e, slider) {
                            hideCaptions($(slider.$currentPage))
                        },
                        onSlideBegin: function(e, slider) {
                            // keep the current navigation tab in view
                            slider.navWindow(slider.$targetPage.index())
                        }
                    })
                    .find('.panel')
                    .find('div[class*=caption]').css({ position: 'absolute' }).end()
                    .hover(function(){ showCaptions( $(this) ) }, function(){ hideCaptions( $(this) ); });

            function showCaptions(el) {
                var $this = el;
                if ($this.find('.caption-bottom').length) {
                    $this.find('.caption-bottom').show().animate({ bottom: 0, opacity: .5 }, 400)
                }
            };
            function hideCaptions(el) {
                var $this = el;
                if ($this.find('.caption-bottom').length) {
                    $this.find('.caption-bottom').stop().animate({ bottom: -100, opacity: 0 }, 400, function () {
                        $this.find('.caption-bottom').hide()
                    })
                }
            };
            // hide all captions initially
            setTimeout(function() {
                hideCaptions($('#slider-${widgetId} .panel'))
            }, ${config.delay})

        })
    </script>

</g:if>