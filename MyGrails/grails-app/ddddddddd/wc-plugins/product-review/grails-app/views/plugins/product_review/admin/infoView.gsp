<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<div class="info-row">
    <label><g:message code="product.name" /></label>
    <span>${review.product.name.encodeAsBMHTML()}</span>
</div>

<g:if test="${review.customer}">
    <div class="info-row">
        <label><g:message code="customer.name" args="${[(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customer":"Member"]}"/></label>
        <span class="value">${review.customer.firstName.encodeAsBMHTML() +" "+ review.customer.lastName.encodeAsBMHTML()}</span>
    </div>
</g:if>
<g:else>
    <div class="info-row">
        <label><g:message code="name"/></label>
        <span class="value">${review.name.encodeAsBMHTML()}</span>
    </div>

    <div class="info-row">
        <label><g:message code="email"/></label>
        <span class="value">${review.email.encodeAsBMHTML()}</span>
    </div>
</g:else>
<div class="info-row">
    <label><g:message code="rating"/></label>
    <div class="rating" score="${review.rating}"></div>
</div>
<h4 class="group-label"><g:message code="review"/></h4>
<div class="view-content-block description-view-block">${review.review.encodeAsBMHTML()}</div>
