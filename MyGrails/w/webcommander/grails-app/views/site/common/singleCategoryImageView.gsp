<div class="category category-image-view category-image-view-width">
    <div class="image category-image-view-height category-image et_ecommerce_category" et-category="link">
        <a href="${app.relativeBaseUrl() + "category/" + category.url}" title="${category.name}">
        <g:set var="url" value="${appResource.getCategoryImageURL(category: category, imageSize: imageSize)}"/>
        <img src="${url}">
    </a>
    </div>
    <div class="category-name et_ecommerce_category" et-category="link">
        <a class="category-name-link title-link link" href="${app.relativeBaseUrl() + "category/" + category.url}">${category.name.encodeAsBMHTML()}</a>
    </div>
    <g:if test="${config["description"] == "true"}">
        <div class="summary">
            ${category.summary ?: (category.description ? category.description.textify().truncate(500) : "")}
        </div>
    </g:if>
</div>