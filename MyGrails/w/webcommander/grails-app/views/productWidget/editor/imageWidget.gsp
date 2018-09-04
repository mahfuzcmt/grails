<%@ page import="com.webcommander.constants.NamedConstants; com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants"%>
<g:set var="previewImageSize"
       value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE, "size")[NamedConstants.PRODUCT_IMAGE_SETTINGS.DETAILS]}"/>
<g:set var="thumbImageSize"
       value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE, "size")[NamedConstants.PRODUCT_IMAGE_SETTINGS.THUMBNAIL]}"/>
<g:set var="popupImageSize"
       value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE, "size")[NamedConstants.PRODUCT_IMAGE_SETTINGS.POPUP]}"/>
<g:set var="popupImageConfig"
       value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE, "popup_use_original")}"/>
<g:set var="config" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT)}"/>
<g:applyLayout name="_productwidget">

    <div class="image-preview-box product-detail-view product-detail-width product-detail-height large_image">
        <span class="sale tag-mark"></span>
        <span class="new tag-mark"></span>
        <span class="featured tag-mark"></span>
        <g:set var="url" value="${appResource.getProductDefaultImageURL(imageSize: previewImageSize)}"/>
        <g:set var="originalImageUrl" value="${appResource.getProductDefaultImageURL()}"/>
        <div class="vertical-aligner"></div><img src="${url}" data-zoom-image="${originalImageUrl}">
    </div>
    <input type="hidden" id="thumb-image-size-cache" value="${thumbImageSize}">
    <input type="hidden" id="detail-image-size-cache" value="${previewImageSize}">
    <g:if test="${popupImageConfig.toBoolean()}">
        <input type="hidden" id="popup-image-size-cache" value="${popupImageSize}">
    </g:if>
    <div class="multi-image-scroll-wrapper" image-size="">
        <div class="image-left-scroller scroll-navigator"></div>
        <div class="image-right-scroller scroll-navigator"></div>
        <div class="image-thumb-container product-thumb-view">
            <div class="thumb-image product-thumb-width product-thumb-height active" index="1" image-name="default.png">
                <g:set var="url"
                       value="${appResource.getProductDefaultImageURL(imageSize: thumbImageSize)}"/>
                <g:set var="originalImageUrl"
                       value="${appResource.getProductDefaultImageURL()}"/>
                <div class="vertical-aligner"></div><img src="${url}" data-zoom-image="${originalImageUrl}">            </div>
        </div>
    </div>
</g:applyLayout>