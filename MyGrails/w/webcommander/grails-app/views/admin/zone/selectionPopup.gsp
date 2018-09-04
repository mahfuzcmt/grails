<form class="edit-popup-form">
    <g:include view="/admin/zone/selection.gsp" model="${[zones: zones, fieldName: fieldName, preventSort: true]}"/>
    <div class="button-line">
        <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="done"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>