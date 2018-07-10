<%@ page import="com.webcommander.constants.NamedConstants;com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<g:set var="imageSize" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CATEGORY_IMAGE, "size")[NamedConstants.CATEGORY_IMAGE_SETTINGS.DETAILS]}"/>
<div class="category-details">
    <div class="category-details-container">
        <h1 class="title page-heading">${category.heading ? category.heading.encodeAsBMHTML() : category.name.encodeAsBMHTML()}</h1>
        <g:if test="${config["category_image"] == "true"}">
            <div class="category category-detail-view category-detail-width">
                <div class="image category-detail-view-height category-image">
                    <g:set var="url" value="${appResource.getCategoryImageURL(category: category, imageSize: imageSize)}"/>
                    <img src="${url}">
                </div>
            </div>
        </g:if>
        <g:if test="${config["category_description"] == "true"}">
            <span class="description">${category.description}</span>
        </g:if>
    </div>
    <g:if test="${config["subcategory"] == "true" && categoryList.size() > 0}">
        <div class="subcategory-container">
            <div class="title">
                <span class="label"><g:message code="subcategories.under"/></span>
                <span class="category-name">${category.name.encodeAsBMHTML()}</span>
            </div>
            <g:include view="widget/categoryImageView.gsp" model="[categoryList: categoryList, url_prefix:'cdcr', config: [description: config['subcategory_description'],
             'show-pagination': config['subcategory_show_pagination'], 'item-per-page-selection': config['item-per-page-selection']], totalCount: totalCategoryCount, offset: categoryOffset, max: categoryMax]"/>
        </div>
    </g:if>
    <g:if test="${productList.size() > 0}">
        <div class="product-container">
            <div class="title">
                <span class="label"><g:message code="products.under"/></span>
                <span class="category-name">${category.name.encodeAsBMHTML()}</span>
            </div>
            <g:include view="widget/productListings.gsp" model="[productList: productList, config: config, totalCount: totalProductCount, offset: productOffset, max: productMax, url_prefix: 'cdpr']"/>
        </div>
    </g:if>
</div>
