<%@ page import="com.webcommander.constants.NamedConstants; com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants"%>
<div class="category-view image-view">
    <g:set var="imageSize" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CATEGORY_IMAGE, "size")[NamedConstants.CATEGORY_IMAGE_SETTINGS.GRIDVIEW]}"/>
    <g:if test="${config["show-pagination"] == NamedConstants.PAGINATION_TYPE.TOP || config["show-pagination"] == NamedConstants.PAGINATION_TYPE.TOP_AND_BOTTOM}">
        <div class="header">
            <g:if test="${config["show-pagination"] == NamedConstants.PAGINATION_TYPE.TOP || config["show-pagination"] == NamedConstants.PAGINATION_TYPE.TOP_AND_BOTTOM}">
                <g:if test="${config["item-per-page-selection"] == 'true'}">
                    <ui:perPageCountSelector value="${max}"/>
                </g:if>
                <paginator data-urlprefix="${url_prefix}" total="${totalCount}" offset="${offset}" max="${max}"></paginator>
            </g:if>
        </div>
    </g:if>
    <div class="content">
        <g:each in="${categoryList}" var="category">
            <g:include view="site/common/singleCategoryImageView.gsp" model="${[category: category, imageSize: imageSize, config: config]}" />
        </g:each>
    </div>
    <g:if test="${(config["show-pagination"] == NamedConstants.PAGINATION_TYPE.BOTTOM || config["show-pagination"] == NamedConstants.PAGINATION_TYPE.TOP_AND_BOTTOM)}">
        <div class="footer">
            <g:if test="${config["item-per-page-selection"] == 'true'}">
                <ui:perPageCountSelector value="${max}"/>
            </g:if>
            <paginator data-urlprefix="${url_prefix}" total="${totalCount}" offset="${offset}" max="${max}"></paginator>
        </div>
    </g:if>
</div>