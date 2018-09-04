<%--
  Created by IntelliJ IDEA.
  User: sajed
  Date: 3/3/14
  Time: 11:43 AM
--%>
<input type="hidden" name="id" value="${question.id}">
<div class="info-row">
    <label><g:message code="product"/></label>
    <span> ${question.product.name.encodeAsBMHTML()}</span>
</div>
<div class="info-row">
    <label><g:message code="question"/></label>
</div>
<div class="view-content-block description-view-block">
    ${question.question.encodeAsBMHTML()}
</div>
<div class="info-row">
    <label><g:message code="date"/></label>
    <span>${question.created.toAdminFormat(true, false, session.timezone)}</span>
</div>
<div class="info-row">
    <label><g:message code="email"/></label>
    <span>${question.email.encodeAsBMHTML()}</span>
</div>

<div class="button-line">
    <button type="button" class="submit-button" name="answer"><g:message code="reply"/></button>
</div>
