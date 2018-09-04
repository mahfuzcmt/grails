<div class="brand-details">
    <div class="brand-title">
        <g:set var="brandName" value="${brand.name.encodeAsBMHTML()}"/>
        <g:if test="${brand.brandUrl}">
            <a href="${brand.brandUrl.encodeAsBMHTML()}" target="_blank"><h1 class="title">${brandName}</h1></a>
        </g:if>
        <g:else>
            <h1 class="title">${brandName}</h1>
        </g:else>
    </div>
    <div class="brand-details-container">
        <g:if test="${config.show_image.toBoolean()}">
            <div class="brand-image-block">
                <img src="${appResource.getBrandImageURL(image: brand, sizeOrPrefix: "")}" alt="${brandName}">
            </div>
        </g:if>
        <g:if test="${config.show_description.toBoolean()}">
            <div class="brand-description">
                ${brand.description.encodeAsBMHTML()}
            </div>
        </g:if>
    </div>
    <g:if test="${productList}">
        <g:include view="widget/productListings.gsp" model="[url_prefix: 'bdpr']"/>
    </g:if>
</div>