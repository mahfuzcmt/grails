<form class="save-cart-init-form" action="${app.relativeBaseUrl()}saveCart/save">
    <div class="form-row">
        <label><g:message code="name"/></label>
        <input type="text" class="unique" name="name" validation="required maxlength[100]" maxlength="100">
    </div>
    <div class="form-row btn-row">
        <button type="submit" class="submit-button"><g:message code="save"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>