<g:form class="oder-comment-form site-popup-form" controller="customer" action="sendCustomerComment">
    <input type="hidden" name="orderId" value="${order.id}">
    <div class="form-row mandatory">
        <textarea name="message" class="msg" validation="required maxlength[2000]"
                  placeholder="${g.message([code: "add.message.to.store.admin"])}" maxlength="2000"></textarea>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="send"/></button>
    </div>
</g:form>
<div class="comment-area">
    <g:each var="comment" in="${comments}">
        <div class="comment-row  ${comment.isAdmin ? 'admin' : 'customer'}">
            <span class="name">
                <g:if test="${comment.isAdmin}">
                    ${comment.adminName.encodeAsBMHTML()}
                </g:if>
                <g:else>
                    <g:message code="${order.customerName ? order.customerName.encodeAsBMHTML() : 'customer'}"/>
                </g:else>
            </span>
            <span class="date-time-row">
                <span class="date-time">${comment.created.toSiteFormat(true, false, session.timezone)}</span>
                <span class="show-comment">${comment.content.encodeAsBMHTML()}</span>
            </span>
        </div>
    </g:each>
</div>