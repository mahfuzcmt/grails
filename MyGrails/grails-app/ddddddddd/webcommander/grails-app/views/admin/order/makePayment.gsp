<%@ page import="com.webcommander.constants.DomainConstants" %>
<form action="${app.relativeBaseUrl()}order/savePayment" method="post" class="edit-popup-form">
    <input type="hidden" name="id" value="${payment?.id}">

    <input type="hidden" name="orderId" value="${order.id}">
    <div class="form-row mandatory mandatory-chosen-wrapper">
        <label><g:message code="payment.method"/></label>
        <g:select from="${paymentGateways.collect{g.message(code: it.name)}}" class="medium" name="paymentGateway"
                   keys="${paymentGateways.code}" value="${payment ? payment.gatewayCode : ""}"/>
    </div>
    <div class="form-row mandatory">
        <label><g:message code="payment.date"/></label>
        <input name="date" type="text" class="medium timefield" show-select-today validation="required" validate-on="call-only"
               value="${payment ? payment.payingDate.toDatePickerFormat(true, session.timezone) : ""}">
    </div>
    <div class="form-row">
        <label><g:message code="track.info"/></label>
        <input type="text" name="trackInfo" class="medium" value="${payment?.trackInfo}">
    </div>
    <div class="form-row">
        <label><g:message code="payer.info"/></label>
        <input type="text" name="payerInfo" class="medium" value="${payment?.payerInfo}">
    </div>
    <div class="form-row">
        <label><g:message code="due"/></label>
        <input type="text"  class="medium" value="${order.due.toPrice(false)}" disabled/>
    </div>
    <div class="form-row mandatory">
        <label><g:message code="amount"/></label>
        <input type="text" name="amount" class="medium" restrict="decimal" validation="number required gt[0]" maxlength="9" value="${payment?payment.amount.toPrice(false):''}">
    </div>
    <div class="button-line">
        <button type="submit" class="edit-popup-form-submit submit-button"><g:message code="submit"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>