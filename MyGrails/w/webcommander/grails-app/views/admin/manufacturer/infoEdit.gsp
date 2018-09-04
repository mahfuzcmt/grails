<form action="${app.relativeBaseUrl()}manufacturerAdmin/save" method="post" class="create-edit-form" enctype="multipart/form-data">
    <div class="bmui-tab">
        <div class="bmui-tab-header-container top-side-header">
            <div class="bmui-tab-header" data-tabify-tab-id="general">
                <span class="title"><g:message code="general"/></span>
            </div>

            <div class="bmui-tab-header" data-tabify-tab-id="metatag">
                <span class="title"><g:message code="metatag"/></span>
            </div>
        </div>
        <div class="bmui-tab-body-container">
            <div id="bmui-tab-general">
                <input type="hidden" name="id" value="${manufacturer.id}">
                <div class="form-section">
                    <div class="form-section-info">
                        <h3><g:message code="manufacturer.info"/> </h3>
                        <div class="info-content"><g:message code="section.text.manufacturer.info"/></div>
                    </div>
                    <div class="form-section-container">
                        <div class="form-row mandatory">
                            <label><g:message code="manufacturer.name"/><span class="suggestion">e.g. WebAlive</span></label>
                            <input type="text" class="medium unique" name="name" value="${manufacturer.name.encodeAsBMHTML()}" validation="required rangelength[2,100]" maxlength="100">
                        </div>
                        <div class="form-row">
                            <label><g:message code="url"/><span class="suggestion">e. g. http://www.abc.com</span></label>
                            <input type="text" class="medium" name="manufacturerUrl" value="${manufacturer.manufacturerUrl.encodeAsBMHTML()}" validation="url maxlength[100]" maxlength="100">
                        </div>
                        <div class="form-row trash-row" style="display: none;">
                            <label><g:message code="what.to.do"/></label>
                            <span><a onclick="return false" class="trash-duplicate-restore fake-link"><g:message code="restore"/></a> <g:message code="restore.and.close.window"/> <g:message code="or"/></span>
                            <input type="checkbox" name="deleteTrashItem.name" class="trash-duplicate-delete"> <span><g:message code="delete.and.save"/></span>
                        </div>
                        <div class="form-row">
                            <label><g:message code="description"/><span class="suggestion">e.g. This Company Creates Websites</span></label>
                            <textarea class="medium" name="description" maxlength="500" validation="maxlength[500]">${manufacturer.description}</textarea>
                        </div>
                        <div class="form-row drop-file thicker-row">
                            <label><g:message code="manufacturer.logo"/></label>
                            <div class="form-image-block">
                                <input type="file" name="manufacturerLogo" file-type="image" size-limit="2097152" previewer="manufacturer-logo-preview" class="medium"
                                    ${manufacturer.image ? 'remove-support="true"' : 'reset-support="true"'} remove-option-name="remove-image">
                                <div class="preview-image">
                                    <img id="manufacturer-logo-preview" src="${appResource.getManufacturerImageURL(image: manufacturer, sizeOrPrefix: "thumb")}" class="preview-image">
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div id="bmui-tab-metatag">
                <div class="form-section">
                    <div class="form-section-info">
                        <h3><g:message code="meta.tag.info"/></h3>
                        <div class="info-content"><g:message code="section.text.meta.tag.info"/></div>
                    </div>
                    <div class="form-section-container">
                        <g:include view="/admin/metatag/metaTagEditor.gsp" model="${[metaTags: manufacturer.metaTags]}"/>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="form-row">
        <button type="submit" class="submit-button"><g:message code="${manufacturer.id ? "update" : "save"}"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>