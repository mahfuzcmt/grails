<g:set var="thumbPosition" value="${config.layout == "alternate" ? config['thumbPositionAlternate'] : config['thumbPositionRollover']}"/>
<g:set var="captionPosition" value="${config.layout == "alternate" ? config['captionPositionAlternate'] : config['captionPositionRollover']}"/>

<div class="galleriffic-gallery-wrap ${config.layout}-layout thumb-position-${thumbPosition} ${config.showCaption == "true" ? "caption-position-" + captionPosition : "no-caption" }">
    <% Closure renderThumbs = { %>
        <div class="thumbs-container">
            <g:if test="${config.layout == "alternate"}">
                <a class="pageLink prev" style="visibility: hidden;" href="#" title="Previous Page"></a>
            </g:if>
            <ul class="thumbs noscript">
                <g:each in="${items}" var="image">
                     <li>
                        <a class="thumb" href="${appResource.getAlbumImageURL(image: image)}" title="${image.altText}">
                            <img src="${appResource.getAlbumImageURL(image: image, sizeOrPrefix: "thumb")}" alt="${image.altText}" />
                        </a>
                        <div class="caption">
                            <div class="image-title">${image.altText?.encodeAsBMHTML()}</div>
                            <div class="image-desc">${image.description?.encodeAsBMHTML()}</div>
                        </div>
                    </li>
                </g:each>
            </ul>
            <g:if test="${config.layout == "alternate"}">
                <a class="pageLink next" style="visibility: hidden;" href="#" title="Next Page"></a>
            </g:if>
        </div>
    <% } %>
    <g:if test="${config.layout == "rollover" && config.showCaption == "true" }">
        <g:if test="${captionPosition == "top"}">
            <div class="caption-container">
                <div class="photo-index"></div>
            </div>
        </g:if>
        <div class="thumb-and-content-wrap">
    </g:if>
    <g:if test="${thumbPosition in ['top', 'left']}">
        <% renderThumbs() %>
    </g:if>
    <div class="galleriffic-content">
        <g:if test="${config.layout == "alternate" && config.showCaption == "true" && captionPosition in ['top', 'left']}">
            <div class="caption-container">
                <div class="photo-index"></div>
            </div>
        </g:if>
        <div class="slideshow-container">
            <div class="loading"></div>
            <div class="slideshow"></div>
        </div>
        <g:if test="${config.layout == "alternate" && config.showCaption == "true" && captionPosition in ["bottom", "right"]}">
            <div class="caption-container">
                <div class="photo-index"></div>
            </div>
        </g:if>
    </div>
    <g:if test="${thumbPosition in ["bottom", "right"]}">
        <% renderThumbs() %>
    </g:if>
    <g:if test="${config.layout == "rollover" && config.showCaption == "true"}">
        </div>
        <g:if test="${captionPosition == "bottom"}">
            <div class="caption-container">
                <div class="photo-index"></div>
            </div>
        </g:if>
    </g:if>
</div>