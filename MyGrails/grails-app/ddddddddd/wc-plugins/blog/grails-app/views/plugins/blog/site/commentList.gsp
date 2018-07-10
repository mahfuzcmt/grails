<g:if test="${comments.size()}">
    <div class="blog-comments">
            <span class="comment-count-info-row"><g:message code="show.out.of.total.comments" args="[comments.size(), count]"/></span>
        <div class="comment-list">
            <g:each in="${comments}" var="comment" status="i">
                <div class="comment-item">
                    <div class="info-row writer">
                        <span class="name"> ${comment.name.encodeAsBMHTML()}</span>
                        <span class="date">${comment.created.toSiteFormat(true, false, session.timezone)}</span>
                    </div>
                    <div class="content">
                        ${comment.content.encodeAsBMHTML()}
                    </div>
                </div>
            </g:each>
        </div>
        <g:if test="${comments.size() < count}">
            <span class="view-all-comment"><g:message code="show.all"/></span>
        </g:if>
    </div>
</g:if>
