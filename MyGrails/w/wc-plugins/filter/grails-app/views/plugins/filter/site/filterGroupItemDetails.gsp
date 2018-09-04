<div class="brand-details">
    <div class="brand-title">
        <g:set var="itemName" value="${groupItem.heading.encodeAsBMHTML()}"/>
        <g:if test="${groupItem.itemUrl}">
            <a href="${groupItem.itemUrl.encodeAsBMHTML()}" target="_blank"><h1 class="title">${itemName}</h1></a>
        </g:if>
        <g:else>
            <h1 class="title">${itemName}</h1>
        </g:else>
    </div>
    <div class="brand-details-container filter-group-item">
        <g:if test="${config.show_image.toBoolean()}">
            <div class="brand-image-block">
                <img src="${appResource.getFilterGroupItemImageURL(filterGroupItem: groupItem, imageSize: "400")}" alt="${groupItem.imageAlt}">
            </div>
        </g:if>
        <g:if test="${config.show_description.toBoolean()}">
            <div class="brand-description">
                ${description}
            </div>
        </g:if>
    </div>
    <g:if test="${productList}">
        <g:include view="widget/productListings.gsp" model="[url_prefix: 'bdpr']"/>
    </g:if>
</div>