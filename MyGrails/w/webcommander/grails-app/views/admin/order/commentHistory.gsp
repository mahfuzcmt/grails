<div class="comment-area">
    <g:each in="${comments}" var="comment">
        <div class="comment-row ${comment.isAdmin ? 'admin' : 'customer'}">
            <g:if test="${comment.isAdmin}">
                <span class="name">${comment.adminName}</span>
                <span class="date-time-row">
                    <span class="date-time">${comment.created.toAdminFormat(true, false, session.timezone)}</span>
                    <span class="show-comment">${comment.content.encodeAsBMHTML()}</span>
                </span>
            </g:if>
            <g:else>
                <span class="name"><g:message code="${order.customerName ?: 'customer'}"/></span>
                <span class="date-time-row">
                    <span class="date-time">${comment.created.toAdminFormat(true, false, session.timezone)}</span>
                    <span class="show-comment">${comment.content.encodeAsBMHTML()}</span>
                </span>
            </g:else>
        </div>
    </g:each>
</div>
<g:form class="edit-popup-form order-comment-form" controller="order" action="saveComment">
    <input type="hidden" name="orderId" value="${order.id}">
    <div class="form-row mandatory">
        <textarea name="message" class="text-area" validation="required maxlength[2000]" placeholder="<g:message code="type.a.message"/>"
                  maxlength="2000"></textarea>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="add.comment"/></button>
        <button type="button" class="save-and-send"><g:message code="add.comment.and.notify"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</g:form>