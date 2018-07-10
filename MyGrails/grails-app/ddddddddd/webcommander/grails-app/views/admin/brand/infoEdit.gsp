<form action="${app.relativeBaseUrl()}brandAdmin/save" method="post" class="create-edit-form" enctype="multipart/form-data">
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
                <input type="hidden" name="id" value="${brand.id}">
                <div class="form-section">
                    <div class="form-section-info">
                        <h3><g:message code="brand.info"/></h3>
                        <div class="info-content"><g:message code="section.text.brand.info"/></div>
                    </div>
                    <div class="form-section-container">
                        <div class="double-input-row">
                            <div class="form-row mandatory">
                                <label><g:message code="brand.name"/></label>
                                <input type="text" class="medium unique" name="name" value="${brand.name.encodeAsBMHTML()}" validation="required rangelength[2,100]" maxlength="100">
                            </div><div class="form-row">
                                <label><g:message code="url"/><span class="suggestion">e. g. http://www.abc.com</span></label>
                                <input type="text" class="medium" name="brandUrl" value="${brand.brandUrl.encodeAsBMHTML()}" validation="url maxlength[200]" maxlength="200">
                            </div>
                        </div>
                        <div class="form-row trash-row" style="display: none;">
                            <label><g:message code="what.to.do"/></label>
                            <div>
                                <a onclick="return false" class="trash-duplicate-restore fake-link"><g:message code="restore"/></a> <g:message code="restore.and.close.window"/>
                                <br/>
                                <g:message code="or"/>
                                <br/>
                                <input type="checkbox" name="deleteTrashItem.name" class="trash-duplicate-delete single"> &nbsp;<g:message code="delete.and.save"/>
                            </div>
                        </div>
                        <div class="form-row">
                            <label><g:message code="description"/><span class="suggestion"></span></label>
                            <textarea class="medium" name="description" maxlength="500" validation="maxlength[500]">${brand.description}</textarea>
                        </div>
                        <div class="form-row drop-file thicker-row">
                            <label><g:message code="brand.logo"/><span class="suggestion"> Upload image for this brand to display it in your store in diverse outlook.</span> </label>
                            <div class="form-image-block">
                                <input type="file" name="brandLogo" file-type="image" size-limit="2097152" previewer="brand-logo-preview" ${brand.image ? 'remove-support="true"' : 'reset-support="true"'} class="medium"
                                       remove-option-name="remove-image">
                                <div class="preview-image">
                                    <img id="brand-logo-preview" src="${appResource.getBrandImageURL(image: brand, sizeOrPrefix: "thumb")}">
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
                        <g:include view="/admin/metatag/metaTagEditor.gsp" model="${[metaTags: brand.metaTags]}"/>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="form-row wcui-horizontal-tab-button">
        <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="${brand.id ? "update" : "save"}"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>