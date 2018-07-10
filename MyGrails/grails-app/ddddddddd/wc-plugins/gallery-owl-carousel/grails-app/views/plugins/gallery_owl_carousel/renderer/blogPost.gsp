<%@ page import="com.webcommander.plugin.gallery_owl_carousel.GalleryOwlCarouselShareTagLib; com.webcommander.util.AppUtil;" %>
<div id="owl-carousel-${widget.uuid}" class="owl-carousel">
    <g:set var="imageSize" value="${AppUtil.getConfig("blog-post-size", "listview")}"/>
    <g:each in="${items}" var="post">
        <div class="item blog-post-item">
            <span class="title">
                <a href="${app.relativeBaseUrl()}${GalleryOwlCarouselShareTagLib.BLOG}/${post.url}" target='_blank'>${post.name}</a>
            </span>
            <g:if test="${post.image}">
                <div class="post post-list-view post-list-view-height">
                    <div class="post-list-view-width">
                        <span class="image">
                            <a href="${app.relativeBaseUrl()}${GalleryOwlCarouselShareTagLib.BLOG}/${post.url}"><img src="${appResource.getBlogPostImageUrl(image: post, sizeOrPrefix: imageSize)}"></a>
                        </span>
                    </div>
                </div>
            </g:if>
            <span class="content">${post.content ? (post.content.textify().truncate(160)) : ""}</span>
            <span class="details-link link"><a href="${app.relativeBaseUrl()}${GalleryOwlCarouselShareTagLib.BLOG}/${post.url}"><g:message code="read.more"/></a></span>
            <span class="blog-post-info">
            <span class="date">${post.date.toSiteFormat(false, false, session.timezone)}</span>
            <span class="author">${post.author?.fullName}</span>
                <g:each in="${post.categories}" var="category">
                    <a href="${app.relativeBaseUrl()}${GalleryOwlCarouselShareTagLib.BLOG}/${category.url}">${category.name}</a>
                </g:each>
                <span class="comment-count">
                    <span class="count">${post.comments.findAll{ it.status == "approved" && it.isSpam == false }.size()}</span>
                    <span class="label"><g:message code="comments"/></span>
                </span>
            </span>
        </div>
    </g:each>
</div>