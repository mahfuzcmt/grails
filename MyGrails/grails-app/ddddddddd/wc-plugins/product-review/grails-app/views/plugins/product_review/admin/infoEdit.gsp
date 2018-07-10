<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<form action="${app.relativeBaseUrl()}productReview/save" method="post" class="edit-popup-form review-edit-form">
    <input type="hidden" name="id" value="${review.id}"/>
    <div class="form-row">
        <label><g:message code="product.name" /></label>
        <span>${review.product.name.encodeAsBMHTML()}</span>
    </div>
    <g:if test="${review.customer}">
        <div class="form-row">
            <input name="customerId" value="${review.customer.id}" type="hidden">
            <label><g:message code="customer.name" args="${[(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customer":"Member"]}"/></label>
            ${review.customer.firstName.encodeAsBMHTML() + review.customer.lastName.encodeAsBMHTML()}
        </div>
    </g:if>
    <g:else>
        <div class="form-row">
            <label><g:message code="name"/></label>
            <input type="text" name="name" validation="required rangelength[3,50]" value="${review.name}"/>
        </div>
        <div class="form-row">
            <label><g:message code="email"/></label>
            <input type="text" name="email" validation="required email" value="${review.email}"/>
        </div>
    </g:else>
    <div class="form-row">
        <label><g:message code="review" /></label>
        <textarea name="review" validation="required rangelength[50,200]">${review.review}</textarea>
    </div>
    <div class="form-row">
        <label><g:message code="rating"/></label>
        <div class="rating" score="${review.rating}"></div>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="update"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>

</form>