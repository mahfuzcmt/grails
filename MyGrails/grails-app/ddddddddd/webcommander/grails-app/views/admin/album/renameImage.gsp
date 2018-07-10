<form class="edit-popup-form rename-iamge-form" action="${app.relativeBaseUrl()}album/saveImageName" method="post">
    <input type="hidden" name="id" value="${id}">
    <div class="form-row">
        <label><g:message code="name"/></label>
        <input type="text" name="name" class="medium" validation="required validateName[${name}] maxlength[255]" maxlength="255" value="${name?:''}">
    </div>
    <div class="button-line wcui-horizontal-tab-button">
        <button type="submit" class="submit-button rename-image-form-submit"><g:message code="save"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>