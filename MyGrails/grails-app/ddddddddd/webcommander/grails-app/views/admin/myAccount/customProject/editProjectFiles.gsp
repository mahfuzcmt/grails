<form class="edit-popup-form" action="${app.relativeBaseUrl()}myAccount/addProjectProjectFile" enctype="multipart/form-data" method="post">
    <input type="hidden" name="projectId" value="${projectId}" />
    <div class="form-row">
        <label><g:message code="description"/></label>
        <textarea name="description" validation="required maxlength[600]" maxlength="600"></textarea>

    </div>
    <div class="form-row">
        <label><g:message code="add.file"/></label>
        <input  name="files" type="file" multiple="true" validation="drop-file-required" />
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="save"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>