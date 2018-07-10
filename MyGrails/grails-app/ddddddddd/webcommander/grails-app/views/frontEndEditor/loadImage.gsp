<div class="fee-widget-config-panel image-widget">
    <g:uploadForm controller="frontEndEditor" action="saveWidget" class="config-form">
        <input type="hidden" name="widgetType" value="${widget.widgetType}">
        <div class="fee-pu-content-body image-widget-container">
            <div class="fee-form-row fee-multi-input-row">
                <div class="fee-form-element">
                    <label for="title"><g:message code="widget.title"/></label>
                    <input type="text" class="medium" name="title" id="title" validation="maxlength[100]" maxlength="100" value="${widget.title}">
                </div>
                <div class="fee-form-element">
                    <label for="hypeUrl"><g:message code="hyperlink.url"/></label>
                    <input type="text" class="medium" name="hype_url" id="hypeUrl" validation="maxlength[255]" maxlength="255" value="${widgetParams.hype_url}">
                </div>
            </div>
            <div class="fee-form-row">
                <label><g:message code="image"/></label>
                <div class="form-image-block">
                    <input type="file" name="localImage" validation="${widget ? '' : 'drop-file-required'} " file-type="image" previewer="image-preview-0">
                </div>
                <div class="preview-image" style="display: none">
                    <div class="image-wrapper">
                        <img id="image-preview-0" src="${widget ? widget?.content : app.relativeBaseUrl() + "setting/loadDefaultImage?type=category"}">
                    </div>
                    <div class="image-description-wrapper">
                        <p class="image-name"></p>
                        <span class="image-size"></span>
                    </div>
                    <div class="image-edit-button">
                        <button type="button" class="fee-btn edit-button">Edit</button>
                    </div>
                </div>
            </div>

        </div>
        <div class="fee-button-wrapper fee-pu-content-footer">
            <button class="fee-back-button fee-pu-button fee-cancel" type="button"><g:message code="cancel"/></button>
            <button class="fee-save fee-pu-button fee-save" type="submit"><g:message code="save"/></button>
        </div>
    </g:uploadForm>
</div>


<!--
<div class="fee-widget-config-panel">
    <g:uploadForm controller="frontEndEditor" action="saveWidget" class="config-form">
        <input type="hidden" name="widgetType" value="${widget.widgetType}">
        <div class="fee-config-body fee-padding-30">
            <div class="fee-row">
                <div class="fee-col fee-col-50 fee-form-row">
                    <label for="title"><g:message code="widget.title"/></label>
                    <input type="text" class="medium" name="title" id="title" validation="maxlength[100]" maxlength="100" value="${widget.title}">
                </div>
                <div class="fee-col fee-col-50 fee-form-row fee-paddingLeft-10">
                    <label for="hypeUrl"><g:message code="hyperlink.url"/></label>
                    <input type="text" class="medium" name="hype_url" id="hypeUrl" validation="maxlength[255]" maxlength="255" value="${widgetParams.hype_url}">
                </div>
            </div>
            <div class="form-row fee-noMargin fee-noPadding">
                <label><g:message code="image"/></label>
                <div class="form-image-block">
                    <input type="file" name="localImage" validation="${widget ? '' : 'drop-file-required'} " file-type="image" previewer="image-preview-0">
                    <div class="preview-image">
                        <img id="image-preview-0" src="${widget ? widget?.content : app.relativeBaseUrl() + "setting/loadDefaultImage?type=category"}">
                    </div>
                </div>
            </div>
        </div>
        <div class="fee-button-wrapper fee-config-footer">
            <button class="fee-save" type="submit"><g:message code="save"/></button>
            <button class="fee-cancel fee-common" type="button"><g:message code="cancel"/></button>
        </div>
    </g:uploadForm>
</div>

--->
