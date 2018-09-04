<%@ page import="com.webcommander.util.AppUtil" %>
<form class="edit-popup-form" action="${app.relativeBaseUrl()}customerAdmin/updateStoreCredit" method="post">
    <input name="id" type="hidden" value="${customer?.id}">

    <g:select class="action-on-selection" name="add" from="${[ g.message(code: "add"), g.message(code: "deduct")]}" keys="['true', 'false']"/>

    <div class="form-row mandatory">
        <label><g:message code="store.credit"/> (${AppUtil.baseCurrency.code + " " + AppUtil.baseCurrency.symbol})</label>
        <input id="store-credit-delta-amount" type="text" name="deltaAmount" class="medium" restrict="signed_decimal" validation="eval[$('#store-credit-delta-amount').val() != 0] required number min[0.01] max[99999999] maxprecision[9,2]">
    </div>

    <div class="form-row">
        <label><g:message code="note"/></label>
        <textarea name="adjustNote" class="medium" validation="maxlength[500]"></textarea>
    </div>

    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="update"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>

