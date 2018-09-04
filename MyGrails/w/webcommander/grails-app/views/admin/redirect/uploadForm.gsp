<g:form controller="redirect" action="initImport" method="post" enctype="multipart/form-data" class="edit-popup-form">
    <div class="form-row drop-file thicker-row">
        <input type="file" name="importFile" validation="drop-file-required" size-limit="104857600" class="medium">
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="upload"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</g:form>