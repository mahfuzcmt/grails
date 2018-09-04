<%@ page import="com.webcommander.util.AppUtil" %>
<g:if test="${loyaltyPointPayment}">
    <div class="payment-option collapsible">
        <div class="header"><g:message code="loyalty.point"/></div>
        <div class="body">
            <div class="row">
                <div class="info"><g:message code="you.have.x.to.spend" args="[AppUtil.siteCurrency.symbol + availableBalance.toPrice()]"/></div>
                <div class="price-row">
                    <span class="label"><g:message code="use.for.this.order"/></span>
                    <input type="text" class="default-payment-amount" name="loyaltyPointPayment" value="${loyaltyPointPayment.amount}" restrict="decimal"/>
                </div>
            </div>
        </div>
    </div>
</g:if>
<div class="payment-option collapsible ${request.referralStatusMsg ? "active" : ""}">
    <div class="header"><g:message code="referral.code"/></div>
    <div class="body">
        <div class="code-submit-form" validation-attr="validation-rule">
            <g:if test="${request.referralStatusMsg}">
                <span class='message-block ${request.referralStatusMsgType}'>${request.referralStatusMsg}</span>
            </g:if>
            <input type="text" name="referralCode" validation-rule="required" autocomplete="off">
            <button type="button" class="code-submit-form-submit"><g:message code="apply"/></button>
        </div>
    </div>
</div>
