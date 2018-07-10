<%@ page import="com.webcommander.util.AppUtil" %>
<g:applyLayout name="_widget">
    <div class="blog-post-content" style="height: ${config.height}px">
        <g:set var="imageSize" value="${AppUtil.getConfig("blog-post-size", "listview")}"/>
        <g:include view="/plugins/blog/widget/blogPostWidgetView.gsp" model="[config: config, posts: posts, imageSize: imageSize, clazz: 'post']"/>

        <div class="footer">
            <g:if test="${config.pagination.toBoolean()}">
                <paginator data-urlprefix="${url_prefix}" total="${total}" offset="${filterMap.offset}" max="${filterMap.max}"></paginator>
            </g:if>
        </div>
    </div>
</g:applyLayout>