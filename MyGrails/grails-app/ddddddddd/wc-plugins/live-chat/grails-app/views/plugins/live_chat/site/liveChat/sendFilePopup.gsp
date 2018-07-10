<div class="live-chat-popup chat-helper-popup send-file-popup">
    <div class="header">
        <span class="title"><g:message code="live.chat.support"/></span>
        <span class="btn close"></span>
    </div>
    <div class="content-wrap">
        <div class="content send-file">
            <form class="send-file-form" action="${app.relativeBaseUrl()}liveChat/sendFile" enctype="multipart/form-data" method="post" error-position="parent-after">
                <label><g:message code="file.to.upload"/>:</label>
                <div class="error-message"></div>
                <div class="uploader">
                    <input name="file" type="file" class="medium" validation="required drop-file-required">
                    <div class="uploader-input">
                        <span class="file-name"></span>
                        <span class="file-size"></span>
                    </div>
                </div>

                <div class="button-row">
                    <button class="submit-button send" type="submit"><g:message code="send"/></button>
                    <button class="cancel-button" type="button"><g:message code="cancel"/></button>
                </div>
            </form>
        </div>
    </div>
</div>