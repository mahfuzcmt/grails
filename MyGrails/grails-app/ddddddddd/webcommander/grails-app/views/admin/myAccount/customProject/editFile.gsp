<form class="edit-popup-form" action="${app.relativeBaseUrl()}myAccount/addProjectDetailsFile" enctype="multipart/form-data" method="post">
    <input type="hidden" name="detailsId" value="${detailsId}" />
    <div class="form-row">
        <label><g:message code="add.file"/></label>
        <input name="file" type="file" validation="required">
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="save"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>