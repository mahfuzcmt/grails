<div class='tell-friend'>
    <div class="header">
        <span class="close-popup close-icon"></span>
        <span class="status-message"><g:message code="email.friend"/></span>
    </div>
    <g:form controller="shop" action="sendMailToFriend" class="tell-friend-popup">
        <input name="id" type="hidden" value="${params.productId}">
        <div class="body">
            <div class="message-container"></div>
            <div class="form-row mandatory">
                <label><site:message code="s:to"/>: <span><site:message code="s:email.address.braces"/></span></label>
                <input class="to-field input-box" type="text" validation="required email" name="receiver">
            </div>
            <div class="form-row mandatory">
                <label><site:message code="s:from"/>: <span><site:message code="s:email.address.braces"/></span></label>
                <input type="text" class="from-field input-box" validation="required email" name="sender">
            </div>
            <div  class="form-row">
                <label><site:message code="s:note"/>: <span><site:message code="s:optional.braces"/></span></label>
                <textarea class="msg-field input-box" validation="maxlength[255]" name="message"></textarea>
                <div class="limit-label"><small>255 character limit</small></div>
            </div>
        </div>
        <div class="popup-bottom footer">
            <button class="submit-button send-email" type="submit"><g:message code="send"/></button>
            <button class="form-reset close-popup" onclick="return false"><g:message code="cancel"/></button>
        </div>
    </g:form>
</div>