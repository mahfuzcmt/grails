<g:applyLayout name="_widget">
    <div class="section-wrapper"><g:each in="${pages}" status="i" var="page"><div class="slidable-sections" slide-index="${i}" id="ss-${widget.uuid}-${page.domId}" style="${config.height ? 'height: ' + (config.height.matches(/\d+/) ? config.height + "px" : config.height) : ''}">
        <render:renderPageContent value="${page.body}"/>
    </div></g:each></div>
    <g:if test="${request.page}">
        <g:if test="${!request.bx_slider_js_loaded}">
            <%
                request.bx_slider_js_loaded = true
            %>
            <script src="${app.systemResourceBaseUrl()}plugins/section-slider/js/slider/jquery.bxslider.min.js"></script>
        </g:if>
        <script type="text/javascript">
            app.config.section_slider_transition_time = app.config.section_slider_transition_time || 500
            app.config.section_slider_transition_effect = app.config.section_slider_transition_effect || 'fade'
            $(window).on("hashchange", function() {
                var hash = window.location.hash
                if(hash) {
                    hash = hash.substring(1)
                    var activeSlide = $("#ss-${widget.uuid}-" + hash)
                    if(activeSlide.length) {
                        activeSlide.each(function() {
                            window.slider['${widget.uuid}'].goToSlide(activeSlide.attr("slide-index"))
                        })
                    }
                }
            })
            var hash = window.location.hash
            var startSlide = 0
            if(hash) {
                hash = hash.substring(1)
                var activeSlide = $("#wi-${widget.uuid} #ss-${widget.uuid}-" + hash)
                if(activeSlide.length) {
                    startSlide = activeSlide.attr("slide-index")
                }
            }
            bm.onReady($.fn, "bxSlider", function() {
                if(!window.slider) {
                    window.slider = {}
                }
                window.slider['${widget.uuid}'] = $("#wi-${widget.uuid} .section-wrapper").bxSlider({
                    speed: app.config.section_slider_transition_time,
                    startSlide: startSlide,
                    infiniteLoop: true,
                    <g:if test="${!config.height}">
                        adaptiveHeight: true,
                    </g:if>
                    easing: app.config.section_slider_transition_effect,
                    pager: false,
                    controls: false,
                    preloadImages: 'all',
                    auto: true
                })
            })
        </script>
    </g:if>
</g:applyLayout>