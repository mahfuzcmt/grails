<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<%
    request.autopage_js = ["plugins/blog/js/auto_page/blog-post-details-page.js"];
%>
<license:allowed id="allow_blog_feature">
    <div class="blog-post-details">
        <div class="post-description">
            <input type="hidden" name="postId" value="${post.id}">
            <h1 class="title">${post.name.encodeAsBMHTML()}</h1>
            <div class="blog-post-info">
                <g:if test="${config.post_date == 'true'}">
                    <span class="date">${post.date.toSiteFormat(false, false, session.timezone)}</span>
                </g:if>
                <g:if test="${config.post_author == 'true'}">
                    <span class="author">${post.author?.fullName}</span>
                </g:if>
                <g:if test="${config.post_categories == 'true' && post.categories.size() > 0}">
                    <span class="categories">
                        <g:each in="${post.categories}" var="category">
                            <a href="${app.relativeBaseUrl() + "blog-category/" + category.url}" target='_blank'>${category.name}</a>
                        </g:each>
                    </span>
                </g:if>
            </div>
            <g:if test="${post.image}">
                <div class="image"><img src="${appResource.getBlogPostImageUrl(image: post, sizeOrPrefix: "600")}"></div>
            </g:if>
            <div class="content">${raw(post.content)}</div>
            <span class="social-media-share-bar addthis_toolbox addthis_32x32_style">
                <g:if test="${config.facebook == 'true'}">
                    <span class="social-media-share">
                        <a class="addthis_button_facebook" target="_blank">
                        </a>
                    </span>
                </g:if>
                <g:if test="${config.twitter == 'true'}">
                    <span class="social-media-share">
                        <a class="addthis_button_twitter" target="_blank">
                        </a>
                    </span>
                </g:if>
                <g:if test="${config.google == 'true'}">
                    <span class="social-media-share">
                        <a class="addthis_button_google" target="_blank">
                        </a>
                    </span>
                </g:if>
                <g:if test="${config.linkedIn == 'true'}">
                    <span class="social-media-share">
                        <a class="addthis_button_linkedin" target="_blank">
                        </a>
                    </span>
                </g:if>
                <g:if test="${config.send_friend == 'true'}">
                    <span class="social-media-share">
                        <a class="addthis_button_email" target="_blank">
                        </a>
                    </span>
                </g:if>
            </span>

            <g:if test="${config.comment_restriction != 'D'}">
                <div class="blog-post-comment-panel">
                    <g:include view="/plugins/blog/site/commentList.gsp" model="[comments: comments, count: count]"/>
                </div>
                <g:if test="${(config.comment_restriction == 'A') || (config.comment_restriction = 'R' && session.customer)}">
                    <div class="comment-post-panel">
                        <form controller="blogPage" action="${app.relativeBaseUrl()}blogPage/saveBlogComment" id="blog-post-create-comment-panel" onsubmit="return false">
                            <h2 class="tab-content-title"><g:message code="write.a.comment"/></h2>
                            <input type="hidden" value="${post.id}" name="postId">
                            <div class="form-row ${config.comment_name == 'true' ? 'mandatory' : ''}">
                                <label><g:message code="name"/></label>
                                <input type="text" maxlength="100" validation="rangelength[2,100] ${config.comment_name == 'true' ? 'required' : ''}"class="large" name="name">
                    </div>
                            <div class="form-row ${config.comment_email == 'true' ? 'mandatory' : ''}">
                                <label><g:message code="email"/></label>
                                <input type="text" class="large" validation="maxlength[50] ${config.comment_email == 'true' ? 'required' : ''} email" name="email">
                            </div>
                            <div class="form-row mandatory">
                                <label><g:message code="comment"/></label>
                                <span class="max-character"><g:message code="max.characters" args="${[1000]}"/></span>
                                <textarea class="large" validation="required maxlength[1000]" maxlength="1000" name="content"></textarea>
                            </div>
                            <g:if test="${config.captcha == 'true'}">
                                <ui:captcha/>
                            </g:if>
                            <div class="form-row">
                                <label></label>
                                <button type="submit"><g:message code="post.comment"/></button>
                            </div>
                        </form>
                    </div>
                </g:if>
                <g:else>
                    <g:set var="url" value="${"/blog/" + post.url}"></g:set>
                    <span class="loginForComment"><span><g:message code="to.comment"/></span>&nbsp;<a href="${app.relativeBaseUrl() + "customer/login?referer=" + url}"><g:message code="log.in"/></a></span>
                </g:else>
            </g:if>
        </div>
    </div>
</license:allowed>
<license:otherwise>
    <div class="service-disabled">
        <g:message code="service.temporarily.disabled"/>
    </div>
</license:otherwise>
<g:if test="${request.page && !request.addthis_loaded}">
    <%
        request.cache("js").push("//s7.addthis.com/js/300/addthis_widget.js")
        request.addthis_loaded = true
    %>
</g:if>