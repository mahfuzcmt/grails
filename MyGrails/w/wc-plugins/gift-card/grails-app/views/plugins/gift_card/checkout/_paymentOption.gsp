<%@ page import="com.webcommander.util.AppUtil" %>
<div class="payment-option collapsible ${ request.giftCodeRedeemStatusMsg ? "active" : ""}">
    <div class="header"><g:message code="gift.card"/></div>
    <div class="body">
        <div class="code-submit-form" validation-attr="validation-rule">
            <g:if test="request.giftCodeRedeemStatusMsg"><span class='message-block ${request.giftCodeRedeemStatusMsgType}'>${request.giftCodeRedeemStatusMsg}</span></g:if>
            <input type="text" name="giftCardCode" validation-rule="required" autocomplete="off">
            <button type="button" class="code-submit-form-submit"><g:message code="apply"/></button>
        </div>
        <g:if test="${giftCardPayment}">
            <div class="row">
                <div class="info"><g:message code="you.have.x.to.spend" args="[AppUtil.siteCurrency.symbol + availableBalance.toPrice()]"/></div>
                <div class="price-row">
                    <span class="label"><g:message code="use.for.this.order"/></span>
                    <input type="text" class="default-payment-amount" name="giftCardPayment" value="${giftCardPayment.amount}" restrict="decimal"/>
                </div>
            </div>
        </g:if>
    </div>
</div>
