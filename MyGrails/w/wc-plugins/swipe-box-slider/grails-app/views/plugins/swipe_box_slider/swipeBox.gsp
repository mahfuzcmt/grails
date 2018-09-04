<g:set var="widgetId" value="${widget.id}"/>
<section id="swipe-wrapper-${widgetId}" class="swipebox-container${config.overflow == 's' ? ' lazy-load' : ''}">
    <g:set var="albumId" value="${config.album}"/>
    <g:set var="itemCount" value="${config.item_per_column.toInteger()}"/>
    <div class="swipe-image-wrap">
        <ul class="box-container">
            <g:each in="${items}" status="i" var="image">
                <li class="box">
                    <a href="${appResource.getAlbumImageURL(image: image)}" class="swipebox" title="${config.altText == 'true' ? image.altText : ''}">
                        <img class="swipe-image" src="${appResource.getAlbumImageURL(image: image, sizeOrPrefix: "gallery")}" alt="${config.altText == 'true' ? image.altText : ''}">
                    </a>
                </li>
            </g:each>
        </ul>
    </div>
    <g:if test="${config.overflow == 'p'}">
        <paginator data-urlprefix="${url_prefix}" total="${totalCount}" offset="${offset}" max="${max}"></paginator>
    </g:if>
</section>