<%@ page import="com.webcommander.constants.NamedConstants; com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants;" %>
<g:set var="imageSize" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CATEGORY_IMAGE, "size")[NamedConstants.CATEGORY_IMAGE_SETTINGS.GRIDVIEW]}"/>
<g:set var="imageSize" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE, "size")[NamedConstants.PRODUCT_IMAGE_SETTINGS.GRIDVIEW]}"/>
<%
    Map categoryConfig = [
            description: "true"
    ]

%>
<g:each in="${items}" status="i" var="category">
    <div class="slide slide-${i+1}">
        <g:include view="site/common/singleCategoryImageView.gsp" model="${[category: category, imageSize: imageSize, config: categoryConfig]}" />
    </div>
</g:each>