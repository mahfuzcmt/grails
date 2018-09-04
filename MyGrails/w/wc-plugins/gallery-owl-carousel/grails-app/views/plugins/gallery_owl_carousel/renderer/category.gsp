<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants; com.webcommander.util.AppUtil;" %>
<g:set var="imageSize" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CATEGORY_IMAGE, "size")[NamedConstants.CATEGORY_IMAGE_SETTINGS.GRIDVIEW]}"/>
<div id="owl-carousel-${widget.uuid}" class="owl-carousel">
    <%
        Map categoryConfig = [
                description: "true"
        ]

    %>
    <g:each in="${items}" var="category">
        <div class="item">
            <g:include view="site/common/singleCategoryImageView.gsp" model="${[category: category, imageSize: imageSize, config: categoryConfig]}" />
        </div>
    </g:each>
</div>