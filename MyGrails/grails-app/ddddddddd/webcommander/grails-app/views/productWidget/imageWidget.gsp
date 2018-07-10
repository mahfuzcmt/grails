<%@ page import="com.webcommander.manager.HookManager; com.webcommander.constants.NamedConstants; com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants"%>
<g:set var="previewImageSize" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE, "size")[NamedConstants.PRODUCT_IMAGE_SETTINGS.DETAILS]}"/>
<g:set var="thumbImageSize" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE, "size")[NamedConstants.PRODUCT_IMAGE_SETTINGS.THUMBNAIL]}"/>
<g:set var="popupImageSize" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE, "size")[NamedConstants.PRODUCT_IMAGE_SETTINGS.POPUP]}"/>
<g:set var="popupImageConfig" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE, "popup_use_original")}"/>
<g:set var="config" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT)}"/>
<g:applyLayout name="_productwidget">
    <%
        if(config.image_zoom.toBoolean()) {
            request.js_cache?.add("js/site/jquery.elevatezoom.js");
        }
    %>
    <input type="hidden" id="product_image_zoom_type" value="${config.zoom_type}">
    <g:set var="infixUrl" value="${productData.images[0]?.urlInfix}"/>
    <div class="image-preview-box product-detail-view product-detail-width product-detail-height large_image" infix-url="${infixUrl}">
        <g:if test="${productData.isOnSale}">
            <span class="sale tag-mark"></span>
        </g:if>
        <g:if test="${productData.isNew}">
            <span class="new tag-mark"></span>
        </g:if>
        <g:if test="${productData.isFeatured}">
            <span class="featured tag-mark"></span>
        </g:if>
        <g:set var="url" value="${appResource.getSiteProductImageURL(productData:productData, imageSize: previewImageSize)}"/>
        <g:set var="originalImageUrl" value="${appResource.getSiteProductImageURL(productData:productData)}"/>
        <div class="vertical-aligner"></div><img src="${url}" ${productData.altText ? "alt=\"" + productData.altText.encodeAsBMHTML() + "\"" : ""} data-zoom-image="${originalImageUrl}">
    </div>
    <g:if test="${productData.images.size() > 1}">
        <input type="hidden" id="thumb-image-size-cache" value="${thumbImageSize}">
        <input type="hidden" id="detail-image-size-cache" value="${previewImageSize}">
        <g:if test="${popupImageConfig.toBoolean()}">
            <input type="hidden" id="popup-image-size-cache" value="${popupImageSize}">
        </g:if>
        <div class="multi-image-scroll-wrapper" image-size=${productData.images.size()}>
            <div class="image-left-scroller scroll-navigator"></div>
            <div class="image-right-scroller scroll-navigator"></div>
            <div class="image-thumb-container product-thumb-view">
                <g:each in="${productData.images}" var="image">
                    <g:set var="baseUrl" value="${image.baseUrl}"/>
                    <div class="thumb-image product-thumb-width product-thumb-height ${image.idx == 1 ? 'active' : ''}" index="${image.idx}" image-name="${image.name}">
                        <g:set var="url" value="${baseUrl + infixUrl + thumbImageSize + "-" + image.name}"/>
                        <g:set var="originalImageUrl" value="${baseUrl + infixUrl + image.name}"/>
                        <div class="vertical-aligner"></div><img src="${url}" ${image.altText ? "alt=\"" + image.altText.encodeAsBMHTML() + "\"" : ""} data-zoom-image="${originalImageUrl}">
                    </div>
                </g:each>
            </div>
        </div>
    </g:if>
</g:applyLayout>