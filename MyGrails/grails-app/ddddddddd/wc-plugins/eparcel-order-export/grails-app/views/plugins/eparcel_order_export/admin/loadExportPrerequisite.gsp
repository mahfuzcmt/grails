<form action="${app.relativeBaseUrl()}eparcelOrderExport/export" no-ajax="" method="post" target="_blank" class="edit-popup-form">
    <div class="form-row mandatory">
        <label><g:message code="order.ids"/></label>
        <div class="multitxtchosen large" data-placeholder="<g:message code="enter.order.ids"/>" chosen-validation="match[(^\d{1,9}(-\d{1,9})?$)]" validation="chosen-required" name="orderId"></div>
    </div>
    %{--<div class="form-row mandatory">--}%
        %{--<label><g:message code="auspost.account.number"/></label>--}%
        %{--<input type="text" class="large" name="auspostAccNum" validation="required">--}%
    %{--</div>--}%
    <div class="form-row mandatory">
        <label><g:message code="charge.code"/></label>
        <input type="text" class="large" name="chargeCode" validation="required">
    </div>
    <div class="form-row">
        <label><g:message code="email.notification"/></label>
        <ui:namedSelect class="large" name="emailNotificationType"
                        key="['NONE': g.message(code: 'none'), 'DESPATCH': g.message(code: 'despatch'), 'TRACKADV': g.message(code: 'track.advice')]"/>
    </div>
    <div class="form-row">
        <label><g:message code="delivery.instruction"/></label>
        <textarea class="large" name="deliveryInstruction"></textarea>
    </div>
    <div class="form-row">
        <label></label>
        <input type="checkbox" class="single" name="requireSignature" value="1"><span><g:message code="require.signature"/></span>
    </div>
    <div class="form-row">
        <label></label>
        <input type="checkbox" name="addToAddressBook" class="single" value="1"><span><g:message code="add.to.address.book"/></span>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="export"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>