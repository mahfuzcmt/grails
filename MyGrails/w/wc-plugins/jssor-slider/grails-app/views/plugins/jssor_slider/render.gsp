<g:set var="widgetId" value="${widget.id}"/>
<g:set var="imgBaseUrl" value="${app.relativeBaseUrl()}plugins/jssor-slider/images/"/>
<%
    if(request.page && !request.is_jssor_slider_loaded) {
        request.js_cache.push("plugins/jssor-slider/js/slider/jssor.slider.mini.js")
        request.js_cache.push("plugins/jssor-slider/js/slider/transitions.js")
        request.css_cache.push("plugins/jssor-slider/css/shared/jssor-slider.css")
        request.is_jssor_slider_loaded = true
    }
%>
<div class="jssor-slider-container" id="jssor-slider-${widgetId}">
    <div class="jssor-loader" u="loading">
        <div class="loader-1">
        </div>
        <div class="loader-2">
        </div>
    </div>
    <div u="slides" class="container">
        <g:include view="plugins/jssor_slider/renderer/${config.galleryContentType}.gsp" model="${[config: config, widget: widget, items: items]}" />
    </div>
    <div u="navigator" class="jssorb03" style="bottom: 16px; right: 6px;">
        <div u="prototype"><div u="numbertemplate"></div></div>
    </div>
    <span u="arrowleft" class="jssora20l">
    </span>
    <span u="arrowright" class="jssora20r">
    </span>
</div>
<g:if test="${request.page}">
    <script type="text/javascript">
        bm.onReady(window, "$JssorSlider$", function () {
            bm.onReady(window, "jssorApp", function () {
                var options = {
                    $AutoPlay: ${config.auto_play},
                    $AutoPlayInterval: ${config.auto_play_interval},
                    $PauseOnHover: ${config.pause_on_hover},
                    $ArrowKeyNavigation: true,
                    $SlideEasing: $JssorEasing$.$EaseOutQuint,
                    $SlideDuration: ${config.slide_duration},
                    $DisplayPieces: 1,
                    $UISearchMode: 1,
                    %{--$PlayOrientation: ${config.sliding_effect == "Slide Down" ? 2 : 1},--}%
                    $DragOrientation: 3,
                    $SlideshowOptions: {
                        $Class: $JssorSlideshowRunner$,
                        $Transitions: [jssorApp.slideshowTransitions["${config.sliding_effect}"]],
                        $TransitionsOrder: 1,
                        $ShowLink: true
                    },
                    $CaptionSliderOptions: {
                        $Class: $JssorCaptionSlider$,
                        $CaptionTransitions: jssorApp.captionTransitions,
                        $PlayInMode: 1,
                        $PlayOutMode: 3
                    },
                    $ArrowNavigatorOptions: {
                        $Class: $JssorArrowNavigator$,
                        $ChanceToShow: ${config.arrow_chance_to_show},
                        $AutoCenter: 2,
                        $Steps: 1
                    },
                    $BulletNavigatorOptions: {
                        $Class: $JssorBulletNavigator$,
                        $ChanceToShow: ${config.bullet_chance_to_show},
                        $AutoCenter: 1,
                        $Steps: 1,
                        $Lanes: 1,
                        $SpacingX: 4,
                        $SpacingY: 4,
                        $Orientation: 1
                    }
                };

                var id = "jssor-slider-${widgetId}";
                <g:if test="${config.hyperlink_images == "true"}">
                $("#"+id+" [u='image']").on("click", function() {
                    var link = $(this).attr("link");
                    var target = $(this).attr("target");
                    if(link) {
                        window.open(link, target);
                    }
                });
                </g:if>
                var jssorSlider = new $JssorSlider$(id, options);
                <g:if test="${config.scale_slider == "true"}">
                //responsive code begin
                function ScaleSlider() {
                    var parentWidth = jssorSlider.$Elmt.parentNode.clientWidth;
                    if (parentWidth) {
                        jssorSlider.$ScaleWidth(Math.max(Math.min(parentWidth, 1920)));
                    } else {
                        window.setTimeout(ScaleSlider, 30);
                    }
                }
                ScaleSlider();
                $(window).bind("load", ScaleSlider);
                $(window).bind("resize", ScaleSlider);
                $(window).bind("orientationchange", ScaleSlider);
                //responsive code end
                </g:if>
            });
        });
    </script>
</g:if>