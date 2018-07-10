<form method="post" class="edit-popup-form">
    <input type="hidden" name="id" value="${image.id}">
    <div class="form-row">
        <label><g:message code="alt.text"/></label>
        <input type="text" class="medium" name="altText" value="${imageAltTag ? imageAltTag : image.altText}">
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="save"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>