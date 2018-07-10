<div class='invite-friend'>
    <div class="header">
        <span class="close-popup close-icon"></span>
        <span class="status-message"><g:message code="invite.a.friend"/></span>
    </div>
    <g:form controller="loyaltyPoint" action="sendInvite" class="invite-friend-popup">
        <div class="body">
            <div class="message-container"></div>
            <div class="form-row mandatory">
                <p><g:message code="from"/>: <span>${customer.name} < ${customer.userName} ></span></p>
            </div>
            <div class="form-row mandatory">
                <label><site:message code="s:to"/>: <span><site:message code="s:email.address.braces"/></span></label>
                <input class="to-field input-box" type="text" validation="required email" name="receiver">
            </div>

            <div  class="form-row">
                <label><site:message code="s:note"/>: <span><site:message code="s:optional.braces"/></span></label>
                <textarea class="msg-field input-box" validation="maxlength[500]" name="message"></textarea>
                <div class="limit-label"><small>500 character limit</small></div>
            </div>
        </div>
        <div class="popup-bottom footer">
            <button class="submit-button send-email" type="submit"><g:message code="send"/></button>
            <button class="form-reset close-popup" onclick="return false"><g:message code="cancel"/></button>
        </div>
    </g:form>
</div>