<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants; com.webcommander.util.AppUtil" %>
<g:set var="imageSize" value="thumb"/>
<g:each in="${items}" status="i" var="filterGroupItem">
    <div class="slide slide-${i+1}">
        <div class="filter-group-item filter-group-item-image-view">
            <div class="image et_ecommerce_filter-group-item" et-filter-group-item="link">
                <a href="${app.relativeBaseUrl() + "filter/" + filterGroupItem.url}" title="${filterGroupItem.title}">
                    <g:set var="url" value="${appResource.getFilterGroupItemImageURL(filterGroupItem: filterGroupItem, imageSize: imageSize)}"/>
                    <img src="${url}" alt="${filterGroupItem.imageAlt}">
                </a>
            </div>
            <div class="filter-group-item-name et_ecommerce_filter-group-item" et-filter-group-item="link">
                <a class="filter-group-item-name-link title-link link" href="${app.relativeBaseUrl() + "filter/" + filterGroupItem.url}">${filterGroupItem.title.encodeAsBMHTML()}</a>
            </div>
            <div class="summary">
                ${filterGroupItem.shortDescription ?: (filterGroupItem.shortDescription ? filterGroupItem.shortDescription.textify().truncate(500) : "")}
            </div>
        </div>
    </div>
</g:each>