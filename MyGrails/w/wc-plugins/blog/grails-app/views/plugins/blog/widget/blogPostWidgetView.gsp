<g:each in="${posts}" var="post" status="idx">
    <div class="blog-post-item${idx == 0 ? ' first' : ''}${idx == posts?.size() - 1 ? ' last' : ''}">
        <g:if test="${config.image == 'true' && post.image}">
            <div class="${clazz} post-list-view post-list-view-height post-list-view-width">
                <a href="${app.relativeBaseUrl() + "blog/" + post.url}"><img src="${appResource.getBlogPostImageUrl(image: post, sizeOrPrefix: imageSize)}" ></a>
            </div>
        </g:if>
        <div class="post-description">
            <h1 class="title">
                <a href="${app.relativeBaseUrl() + "blog/" + post.url}">${post.name}</a>
            </h1>
            <div class="blog-post-info">
                <g:if test="${config.date == 'true'}">
                    <span class="date">${post.date.toSiteFormat(false, false, session.timezone)}</span>
                </g:if>
                <g:if test="${config.author == 'true'}">
                    <span class="author">${post.author?.fullName}</span>
                </g:if>
                <g:if test="${config.categories == 'true' && post.categories.size()}">
                    <span class="categories">
                        <g:each in="${post.categories}" var="category">
                            <a href="${app.relativeBaseUrl() + "blog-category/" + category.url}">${category.name}</a>
                        </g:each>
                    </span>
                </g:if>
                <g:if test="${config.comment_count == 'true'}">
                    <span class="comment-count">
                        <g:set var="filterData" value="[post: post.id,status: 'approved', isSpam: 'false']"/>
                        <g:set var="blogService" bean="blogService"/>
                        <span class="count">${blogService.getCommentsCount(filterData)}</span>
                        <span class="label"><g:message code="comments"/></span>
                    </span>
                </g:if>
            </div>
            <g:if test="${config.post_content == 'F'}">
                <div class="content">${post.content}</div>
            </g:if>
            <g:else>
                <div class="content">${post.content? (post.content.textify().truncate(config.content_length.toInteger())) : ""}</div>
               <a href="${app.relativeBaseUrl() + "blog/" + post.url}"><site:message code="${config.read_more}"/></a>
            </g:else>
        </div>
    </div>
</g:each>