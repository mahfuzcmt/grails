<div class="manufacturer-details">
    <div class="manufacturer-title">
        <g:set var="manufacturerName" value="${manufacturer.name.encodeAsBMHTML()}"/>
        <g:if test="${manufacturer.manufacturerUrl}">
            <a href="${manufacturer.manufacturerUrl.encodeAsBMHTML()}" target="_blank"><h1 class="title">${manufacturerName}</h1></a>
        </g:if>
        <g:else>
            <h1 class="title">${manufacturerName}</h1>
        </g:else>

    </div>
    <div class="manufacturer-details-container">
        <g:if test="${config.show_image.toBoolean()}">
            <div class="manufacturer-image-block">
                <img src="${appResource.getManufacturerImageURL(image: manufacturer, sizeOrPrefix: "")}" alt="${manufacturerName}">
            </div>
        </g:if>
        <g:if test="${config.show_description.toBoolean()}">
            <div class="manufacturer-description">
                ${manufacturer.description.encodeAsBMHTML()}
            </div>
        </g:if>
    </div>
    <g:include view="widget/productListings.gsp" model="[url_prefix: 'mdpr']"/>
</div>