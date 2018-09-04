<div id="owl-carousel-${widget.uuid}" class="owl-carousel">
    <g:set var="imgSrcAttr" value="${config.lazy_load == "true" ? "data-src" : "src"}"/>
    <g:set var="imgClass" value="${config.lazy_load == "true" ? "owl-lazy" : ""}"/>
    <g:each in="${items}" var="image" status="i">
        <div class="item">
            <img class="${imgClass}" ${imgSrcAttr}="${appResource.getAlbumImageURL(image: image)}" alt="${image.altText}">
        </div>
    </g:each>
</div>