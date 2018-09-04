<%@ page import="com.webcommander.util.AppUtil" %>
<div class="blog-category-details">
    <g:set var="imageSize" value="${AppUtil.getConfig("blog-post-size", "cat_details")}"/>
    <span class="title">${category.name}</span>
    <g:if test="${config.cat_image == "true" && category.image}">
        <span class="blog-category image"><img src="${appResource.getBlogCategoryImageUrl(image: category, sizeOrPrefix: "450")}"></span>
    </g:if>
    <g:if test="${config.cat_description == "true"}">
        <span class="blog-category description">${raw(category.description)}</span>
    </g:if>
    <div class="blog-posts">
        <g:include view="/plugins/blog/widget/blogPostWidgetView.gsp" model="[config: [image: config.cat_post_image, post_content: config.cat_post_content, content_length : config.cat_post_content_length,
                date: config.cat_post_date, read_more: config.cat_post_read_more, author: config.cat_post_author, categories: config.cat_post_categories, comment_count: config.cat_post_comment_count], posts: posts, imageSize: imageSize, clazz: 'category']"/>
    </div>
</div>