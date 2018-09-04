<%@ page import="com.webcommander.util.AppUtil" %>
<div class="payment-option collapsible ${ request.discountCouponStatusMsg ? "active" : ""}">
    <div class="header"><g:message code="discount.coupon"/></div>
    <div class="body">
        <div class="code-submit-form" validation-attr="validation-rule">
            <g:if test="${request.discountCouponStatusMsg}"><span class='message-block ${request.discountCouponStatusType}'>${request.discountCouponStatusMsg}</span></g:if>
            <input type="text" name="couponCode" validation-rule="required" autocomplete="off">
            <button type="button" class="code-submit-form-submit"><g:message code="apply"/></button>
        </div>
    </div>
</div>
