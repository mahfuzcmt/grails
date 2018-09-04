<div class='reply-popup'>
    <div class="header">
        <span class="close-popup close-icon"></span>
        <span class="status-message"><g:message code="reply"/></span>
    </div>
    <g:form controller="blogPage" action="saveCommentReply" class="blog-comment-reply-popup">
        <div class="body">
            <div class="message-container"></div>
            <input type="hidden" value="${parent.id}" name="parent"/>
            <input type="hidden" value="${parent.postId}" name="postId"/>
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
                <div class="textarea-wrap">
                    <textarea class="reply-box" validation="required maxlength[1000]" maxlength="1000" name="content"></textarea>
                    <span class="max-character"><g:message code="max.characters" args="${[1000]}"/></span>
                </div>
            </div>
            <g:if test="${config.captcha == 'true'}">
                <ui:captcha/>
            </g:if>
        </div>
        <div class="popup-bottom footer">
            <button class="submit-button" type="submit"><g:message code="reply"/></button>
            <button class="form-reset close-popup" onclick="return false"><g:message code="cancel"/></button>
        </div>
    </g:form>
</div>