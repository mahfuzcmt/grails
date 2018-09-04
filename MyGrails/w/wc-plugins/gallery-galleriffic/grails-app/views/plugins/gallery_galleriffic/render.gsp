<%@ page import="com.webcommander.util.StringUtil" %>
<g:set var="uuid" value="#wi-${widget.uuid}"/>
<g:include view="plugins/gallery_galleriffic/renderer/album.gsp" model="${[config: config, widget: widget, items: items, uuid: uuid]}" />
<g:if test="${request.page}">
    <%
        if(!request.is_gallery_galleriffic_loaded) {
            request.js_cache.push("plugins/gallery-galleriffic/js/galleriffiec/jquery.galleriffic.js")
            request.css_cache.push("plugins/gallery-galleriffic/css/galleriffiec/galleriffiec.css")
            request.is_gallery_galleriffic_loaded = true;
        }
    %>

    <script type="text/javascript">
        $(function() {
            bm.onReady($.fn, "galleriffic", function() {
                var gallery = $('${uuid} .thumbs-container').galleriffic({
                    numThumbs:                ${config.numThumbs},
                    enableTopPager:            ${config.layout == "rollover"},
                    enableBottomPager:         ${config.layout == "rollover"},
                    imageContainerSel:         '${uuid} .slideshow',
                    captionContainerSel:       '${uuid} .caption-container',
                    loadingContainerSel:       '${uuid} .loading',
                    renderSSControls:          false,
                    renderNavControls:         false,
                    playLinkText:              'Play Slideshow',
                    pauseLinkText:             'Pause Slideshow',
                    nextPageLinkText:          'Next &rsaquo;',
                    prevPageLinkText:          '&lsaquo; Prev',
                    autoStart:                 ${config.autoStart ?: false},
                    syncTransitions:           true,
                    defaultTransitionDuration: 900,
                    onPageTransitionIn:        function() {
                        var prevPageLink = this.find('a.prev').css('visibility', 'hidden');
                        var nextPageLink = this.find('a.next').css('visibility', 'hidden');

                        // Show appropriate next / prev page links
                        if (this.displayedPage > 0)
                            prevPageLink.css('visibility', 'visible');

                        var lastPage = this.getNumPages() - 1;
                        if (this.displayedPage < lastPage)
                            nextPageLink.css('visibility', 'visible');

                        this.fadeTo('fast', 1.0);
                    }
                });
                gallery.find('a.prev').click(function(e) {
                    gallery.previousPage();
                    e.preventDefault();
                });

                gallery.find('a.next').click(function(e) {
                    gallery.nextPage();
                    e.preventDefault();
                });

                var offset = ${config.thumbOffset ?: 180};
                var initThumb = gallery.numThumbs;
                function updateThumb() {
                    var width = gallery.parents(".galleriffic-gallery-wrap").width();
                    if(width > offset && !gallery.enableBottomPager) {
                        var applyThumb = Math.floor(width/offset);
                        if(applyThumb > initThumb) {
                            applyThumb = initThumb;
                        }
                        gallery.numThumbs = applyThumb;
                        gallery.updateThumbs();
                    }
                }
                updateThumb();
                $(window).on("resize." + bm.getUUID(), function() {
                    updateThumb();
                })

            });
        });
    </script>
</g:if>