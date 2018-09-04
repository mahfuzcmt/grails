<%@ page import="com.webcommander.plugin.blog.constants.DomainConstants;" %>
<g:if test="${comments.size()}">
    <div class="blog-comments">
        <span class="comment-count-info-row"><g:message code="show.out.of.total.comments" args="[comments.size(), count]"/></span>
        <div class="comment-list">
            <g:each in="${comments}" var="comment">
                <div class="comment-item">
                    <div class="info-row writer">
                        <span class="name"> ${comment.name.encodeAsBMHTML()}</span>
                        <span class="date">${comment.created.toSiteFormat(true, false, session.timezone)}</span>
                        <g:set var="customerId" value="${session.customer}"/>
                        <g:set var="numberOfLikeOfComment" value="${comment.reactions.findAll {it.type == DomainConstants.BLOG_REACTION.LIKE}.size()}"/>
                        <g:if test="${customerId}"><span>${numberOfLikeOfComment}</span>
                            <g:if test="${numberOfLikeOfComment > 1}">
                                <span> <g:message code="likes"/></span>
                            </g:if>
                            <g:else>
                                <span> <g:message code="like"/></span>
                            </g:else>
                            <span class="${comment.reactions.find {(it.customer.id == customerId) && (it.type == DomainConstants.BLOG_REACTION.LIKE)}?"btn-liked" : "btn-like"} reaction" type="submit" data-id="${comment.id}"></span>
                        </g:if>
                        <g:else>
                            <span>${numberOfLikeOfComment}</span>
                            <g:if test="${numberOfLikeOfComment > 1}">
                                <span> <g:message code="likes"/></span>
                            </g:if>
                            <g:else>
                                <span> <g:message code="like"/></span>
                            </g:else>
                             <span class='loging-to-react reaction btn-like' data-purpose="<g:message code="to.like"/>" data-url="${url}"></span>
                        </g:else>
                        <g:if test="${config.comment_restriction != 'D'}">
                            <g:if test="${(config.comment_restriction == 'A') || (config.comment_restriction = 'R' && session.customer)}">
                                <span class='btn-comment-reply' data-id="${comment.id}"><g:message code="reply"/> </span>
                            </g:if>
                            <g:else>
                                <span class='loging-to-react' data-purpose="<g:message code="to.reply"/>" data-url="${url}"><g:message code="reply"/> </span>
                            </g:else>
                        </g:if>
                    </div>
                    <div class="content">
                        ${comment.content.encodeAsBMHTML()}
                    </div>
                    <g:set var="sortedReplies" value="${comment.replies.sort{it.id}}"/>
                    <g:each in="${sortedReplies}" var="reply">
                        <g:if test="${reply.status == DomainConstants.COMMENT_STATUS.APPROVED}">
                            <div class=comment-item"  style="margin-left:20px;">
                                <div class="info-row writer">
                                    <span class="name"> ${reply.name.encodeAsBMHTML()}</span>
                                    <span class="date">${reply.created.toSiteFormat(true, false, session.timezone)}</span>
                                    <g:set var="numberOfLikeOfReply" value="${reply.reactions.findAll {it.type == DomainConstants.BLOG_REACTION.LIKE}.size()}"/>
                                    <g:if test="${customerId}"> <span>${numberOfLikeOfReply}</span>
                                        <g:if test="${numberOfLikeOfReply> 1}">
                                            <span> <g:message code="likes"/></span>
                                        </g:if>
                                        <g:else>
                                            <span> <g:message code="like"/></span>
                                        </g:else>
                                        <span class="${reply.reactions.find {(it.customer.id == customerId) && (it.type == DomainConstants.BLOG_REACTION.LIKE)}?"btn-liked" : "btn-like"} reaction" type="submit" data-id="${reply.id}"></span>
                                    </g:if>
                                    <g:else>
                                        <span>${reply.reactions.findAll {it.type == DomainConstants.BLOG_REACTION.LIKE}.size()}</span>
                                        <g:if test="${reply.reactions.findAll {it.type == DomainConstants.BLOG_REACTION.LIKE}.size() > 1}">
                                            <span> <g:message code="likes"/></span>
                                        </g:if>
                                        <g:else>
                                            <span> <g:message code="like"/></span>
                                        </g:else>
                                         <span class='loging-to-react reaction btn-like' data-purpose="<g:message code="to.like"/>" data-url="${url}"></span>
                                    </g:else>
                                </div>
                                <div class="content">
                                    <i>${reply.content.encodeAsBMHTML()}</i>
                                </div>
                            </div>
                        </g:if>
                    </g:each>
                </div>
            </g:each>
        </div>
        <g:if test="${comments.size() < count}">
            <span class="view-all-comment"><g:message code="show.all"/></span>
        </g:if>
    </div>
</g:if>

