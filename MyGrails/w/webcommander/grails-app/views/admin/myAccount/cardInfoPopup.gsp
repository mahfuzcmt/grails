<form class="edit-popup-form" action="<app:relativeBaseUrl/>myAccount/upOrDownByOrderInvoicePayment" method="post">
    <input type="hidden" name="packageId" value="${params.packageId}">
    <div class="form-row mandatory">
        <label><g:message code="card.holder.name"/></label>
        <input type="text" name="cardName" autocomplete="off" validation="required">
    </div>
    <div class="double-input-row">
        <div class="form-row  mandatory">
            <label><g:message code="card.number"/></label>
            <input type="text" name="cardNumber" autocomplete="off" validation="required creditcard">
        </div><div class="form-row mandatory">
            <label><g:message code="cvv"/></label>
            <input type="text" name="cardCVV" autocomplete="off" validation="required cvv">
        </div>
    </div>
    <div class="double-input-row">
        <div class="form-row mandatory">
            <label><g:message code="card.expiry.month" /></label>
            <g:select from="${['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12']}"  name="cardMonth"></g:select>
        </div><div class="form-row mandatory">
            <label><g:message code="card.expiry.year"/></label>
            <g:set var="currentYear" value="${Calendar.getInstance().get(Calendar.YEAR)}"></g:set>
            <g:select from="${currentYear..2099}" keys="${currentYear..2099}"  name="cardYear"></g:select>
        </div>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="purchase"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>