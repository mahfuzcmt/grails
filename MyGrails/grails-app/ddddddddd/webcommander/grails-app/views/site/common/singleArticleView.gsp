<g:if test="${article.isPublished}">
    <g:if test="${!(article.isInTrash)}">
        <div class="article-item ${config.display_option == "full" ? 'full-article' :
                (config.display_option == "summary" ? 'article-summery' : 'article-summery-with-link')}" data-article-id="${article.id}">
            <g:if test="${config.article_title == 'show'}">
                <div class="article-title"> ${article.name.encodeAsBMHTML()} </div>
            </g:if>
            <g:if test="${config.display_option == "full"}">
                <div class="article-content">
                    ${article.content}
                </div>
            </g:if>
            <g:if test="${config.display_option == "summary"}">
                <div class="article-content"> ${article.summary} </div>
            </g:if>
            <g:if test="${config.display_option == "summary_link"}">
                <div class="article-content">
                    ${article.summary}
                    <span class="read-more"><a href="${app.relativeBaseUrl()}article/${article.url}"><g:message code="read.more"/></a></span>
                </div>
            </g:if>
        </div>
    </g:if>
</g:if>