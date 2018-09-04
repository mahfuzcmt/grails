<g:form controller="album" action="savePdf" enctype="multipart/form-data" class="edit-popup-form">
    <div class="form-row drop-file thicker-row">
        <input type="file" name="albumPdf" file-type="pdf" size-limit="10485760" validation="drop-file-required">
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="upload"/> </button>
        <button type="button" class="cancel-button"><g:message code="cancel"/> </button>
    </div>
</g:form>