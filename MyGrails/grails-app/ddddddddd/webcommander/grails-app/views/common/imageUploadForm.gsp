<form class="edit-popup-form image-upload" method="post" action="${app.relativeBaseUrl()}app/uploadWceditorImage" enctype="multipart/form-data">
    <div class="form-row">
        <label><g:message code="image"/></label>
        <div class="form-image-block">
            <input type="file" name="file" file-type="image" remove-option-name="remove-image" size-limit="2097152" previewer="upload-image-preview" validation="drop-file-required">
            <div class="preview-image">
                <img id="upload-image-preview" src="">
            </div>
        </div>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="upload"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>