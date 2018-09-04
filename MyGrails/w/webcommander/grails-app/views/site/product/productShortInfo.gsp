<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil; com.webcommander.constants.NamedConstants" %>
<g:set var="previewImageSize" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE, "size")[NamedConstants.PRODUCT_IMAGE_SETTINGS.GRIDVIEW]}"/>
<div class="product-info-view-container short">
    <h1 class="product-name">${productData.name.encodeAsBMHTML()}</h1>
    <div class="container-block">
        <div class="left-panel">
            <div class="product-widget widget-productImage">
                <div class="image-preview-box product-detail-view product-detail-width product-detail-height">
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
                    <div class="vertical-aligner"></div><img src="${url}" ${productData.altText ? "alt=\"" + productData.altText.encodeAsBMHTML() + "\"" : ""}>
                </div>
            </div>
        </div>

        <div class="right-panel" pad-width="400">
            <wi:productwidget type="productSummary"/>
            <wi:productwidget type="productSku"/>
            <wi:productwidget type="productCategory"/>
            <wi:productwidget type="productDownloadableSpec"/>
            <wi:productwidget type="productModel"/>
            <wi:productwidget type="stockMark"/>
            <wi:productwidget type="price"/>
            <wi:productwidget type="customField"/>
            <wi:productwidget type="combinedProduct"/>
            <plugin:hookTag hookPoint="productShortViewContribution"/>
            <wi:productwidget type="addCart"/>
        </div>
    </div>

    <g:if test="${enableDescription}">
        <div class="product-widget widget-information">
            <div class="bmui-tab">
                <div class="bmui-tab-header-container">
                    <div class="bmui-tab-header" data-tabify-tab-id="description">
                        <span class="title"><g:message code="product.description"/></span
                    </div>
                </div>

                <div class="bmui-tab-body-container">
                    <div id="bmui-tab-description">
                        <span class="title">${productData.name.encodeAsBMHTML()}</span>
                        <span class="description">${productData.description}</span>
                    </div>
                </div>
            </div>
        </div>
    </g:if>
</div>