<form class="create-edit-form edit-popup-form" method="post" action="${app.relativeBaseUrl()}album/updateImages" enctype="multipart/form-data">
    <input type="hidden" name="id" value="${albumId}">
    <div class="form-row drop-file thicker-row">
        <input type="file" name="albumImage" file-type="image" queue="album-image-previewer" multiple="multiple" validation="drop-file-required">
    </div>
    <div id="album-image-previewer" class="multiple-image-queue">
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="upload"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>



