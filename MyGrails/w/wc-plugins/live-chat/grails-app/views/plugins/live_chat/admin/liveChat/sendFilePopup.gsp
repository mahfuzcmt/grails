<form class="edit-popup-form" action="${app.baseUrl()}liveChatAdmin/sendFile" method="post" enctype="multipart/form-data">
    <input type="hidden" name="chatId" value="${params.chatId}">
    <div class="form-row drop-file thicker-row">
        <input type="file" name="file" validation="drop-file-required" text-helper="no" size-limit="5242880">
    </div>
    <div class="button-line">
        <button class="submit-button" type="submit"><g:message code="upload"/> </button>
        <button class="cancel-button" type="button"><g:message code="cancel"/> </button>
    </div>
</form>