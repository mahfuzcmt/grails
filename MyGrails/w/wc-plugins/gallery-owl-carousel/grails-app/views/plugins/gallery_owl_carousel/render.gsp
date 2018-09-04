<g:include view="plugins/gallery_owl_carousel/renderer/${config.galleryContentType}.gsp" model="${[config: config, widget: widget, items: items]}" />

<g:if test="${request.page}">
    <%
        if(!request.is_owl_carousel_loaded) {
            request.js_cache.push("plugins/gallery-owl-carousel/js/owl-carousel/owl.carousel.min.js")
            request.css_cache.push("plugins/gallery-owl-carousel/css/owl-carousel/owl.carousel.css")
            request.css_cache.push("plugins/gallery-owl-carousel/css/owl-carousel/owl.theme.css")
            request.is_owl_carousel_loaded = true;
        }
    %>

    <script type="text/javascript">
        function loadOwlCarousel_${widget.id}() {
            $("#owl-carousel-${widget.uuid}").owlCarousel({
                items : ${config.items},
                loop: ${config.galleryContentType != "product"},
                margin: ${config.margin ?: 0},
                <g:if test="${config.responsive == "true"}" >
                responsiveClass:true,
                responsive: {
                    <g:if test="${config.items_mobile == "true"}">
                        ${config.items_mobile_max_width}: { items: ${config.items_mobile_no_of_item} },
                    </g:if><g:if test="${config.items_tablet_small == "true"}">
                        ${config.items_tablet_small_max_width}: { items: ${config.items_tablet_small_no_of_item} },
                    </g:if><g:if test="${config.items_tablet == "true"}">
                        ${config.items_tablet_max_width}: { items: ${config.items_tablet_no_of_item} },
                    </g:if><g:if test="${config.items_desktop_small == "true"}">
                        ${config.items_desktop_small_max_width}: { items: ${config.items_desktop_small_no_of_item} },
                    </g:if><g:if test="${config.items_desktop == "true"}">
                      ${config.items_desktop_max_width}: { items: ${config.items_desktop_no_of_item} }
                    </g:if>
                },
                 </g:if>
                 <g:else>
                    responsive: {},
                </g:else>
                autoplay: ${config.auto_play},
                autoplayTimeout: ${config.autoplayTimeout ?: "1000"},
                autoplayHoverPause: ${config.stop_on_over},
                nav: ${config.navigation},
                navText: ["${config.pre_button_text}","${config.next_button_text}"],
                dots: ${config.pagination},
                dotsEach: ${config.pagination_numbers},
                dotsSpeed: ${config.pagination_speed},
                responsiveRefreshRate : ${config.responsive_refresh_rate},
                lazyLoad : ${config.lazy_load}
            })
        }
        $(function() {
            bm.onReady($.prototype, "owlCarousel", function () {
                loadOwlCarousel_${widget.id}();
            });
        });
    </script>

</g:if>