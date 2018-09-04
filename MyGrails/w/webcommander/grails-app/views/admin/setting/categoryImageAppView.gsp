<form id="frmCategoryImageSetting" class="create-edit-form" action="${app.relativeBaseUrl()}setting/saveConfigurations" method="POST">
    <div class="form-section">
        <div class="form-section-info">
            <input type="hidden" name="type" value="category_image">
            <h3><g:message code="admin.panel.images"/></h3>
            <div class="info-content"><g:message code="section.text.setting.catagory.image.admin"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row mandatory">
                <label><g:message code="admin.image"/><span class="suggestion"><g:message code="suggestion.setting.image.size.admin"/></span></label>
                <div class="twice-input-row">
                    <input type="text" name="category_image.admin_width" class="smaller" validation="required digits max[220] min[48] maxlength[9]" maxlength="9" restrict="numeric" value="${sizes.admin_width}"><span>x</span><input type="text" name="category_image.admin_height" class="smaller" validation="required digits max[220] min[48] maxlength[9]" maxlength="9" restrict="numeric" value="${sizes.admin_height}">
                </div>
            </div>
        </div>
        <div class="section-separator"></div>
        <div class="form-section-info">
            <h3><g:message code="store.front.images"/></h3>
            <div class="info-content"><g:message code="section.text.setting.catagory.image.store"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="grid.view"/><span class="suggestion"><g:message code="suggestion.setting.image.size.site.grid"/></span></label>
                    <div class="twice-input-row">
                        <input type="text" name="category_image.gridview_width" class="smaller" validation="required digits max[950] min[48] maxlength[9]" maxlength="9" restrict="numeric" value="${sizes.gridview_width}"><span>x</span><input type="text" name="category_image.gridview_height" class="smaller" validation="required digits max[950] min[48] maxlength[9]" maxlength="9" restrict="numeric" value="${sizes.gridview_height}">
                    </div>
                </div><div class="form-row mandatory">
                    <label><g:message code="list.view"/><span class="suggestion"><g:message code="suggestion.setting.image.size.site.list"/></span></label>
                    <div class="twice-input-row">
                        <input type="text" name="category_image.listview_width" class="smaller" validation="required digits max[950] min[48] maxlength[9]" maxlength="9" restrict="numeric" value="${sizes.listview_width}"><span>x</span><input type="text" name="category_image.listview_height" class="smaller" validation="required digits max[950] min[48] maxlength[9]" maxlength="9" restrict="numeric" value="${sizes.listview_height}">
                    </div>
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="pop.up.image"/><span class="suggestion"><g:message code="suggestion.setting.image.size.site.popup"/></span></label>
                    <div class="twice-input-row">
                        <input type="text" name="category_image.popup_width" class="smaller" validation="required digits max[900] min[64] maxlength[9]" maxlength="9" restrict="numeric" value="${sizes.popup_width}"><span>x</span><input type="text" name="category_image.popup_height" class="smaller" validation="required digits max[900] min[64] maxlength[9]" maxlength="9" restrict="numeric" value="${sizes.popup_height}">
                    </div>
                </div><div class="form-row">
                    <label><g:message code="use.original.image"/></label>
                    <input type="checkbox" class="use-original single" name="category_image.popup_use_original" ${sizes.popup_use_original == "on" ? "checked='checked'" : ""}>
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="details.image"/><span class="suggestion"><g:message code="suggestion.setting.image.size.site.details"/></span></label>
                    <div class="twice-input-row">
                        <input type="text" name="category_image.details_width" class="smaller" validation="required digits max[900] min[48] maxlength[9]" maxlength="9" restrict="numeric" value="${sizes.details_width}"><span>x</span><input type="text" name="category_image.details_height" class="smaller" validation="required digits max[600] min[48] maxlength[9]" maxlength="9" restrict="numeric" value="${sizes.details_height}">
                    </div>
                </div><div class="form-row mandatory">
                    <label><g:message code="thumbnail.image"/><span class="suggestion"><g:message code="suggestion.setting.image.size.site.thumb"/></span></label>
                    <div class="twice-input-row">
                        <input type="text" name="category_image.thumbnail_width" class="smaller" validation="required digits max[120] min[32] maxlength[9]" maxlength="9" restrict="numeric" value="${sizes.thumbnail_width}"><span>x</span><input type="text" name="category_image.thumbnail_height" class="smaller" validation="required digits max[120] min[32] maxlength[9]" maxlength="9" restrict="numeric" value="${sizes.thumbnail_height}">
                    </div>
                </div>
            </div>
            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>