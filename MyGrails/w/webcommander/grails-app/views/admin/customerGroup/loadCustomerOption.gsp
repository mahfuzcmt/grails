<form class="edit-popup-form create-edit-form" action="${app.relativeBaseUrl()}customerGroup/assignCustomer" method="post">
<div class="form-section">

    <g:include view="/admin/customer/customerSelection.gsp"/>

    <div class="form-row">
        <button type="submit" class="submit-button"><g:message code="${"save"}"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</div>
</form>