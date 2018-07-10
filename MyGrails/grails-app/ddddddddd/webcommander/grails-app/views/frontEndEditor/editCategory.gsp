<%@ page import="com.webcommander.content.Navigation;com.webcommander.webcommerce.Category; com.webcommander.constants.DomainConstants" %>
<div class="fee-form-content">
    <form action="${app.relativeBaseUrl()}frontEndEditor/saveCategory" method="post" enctype="multipart/form-data" class="create-edit-form">
        <div class="fee-padding-30 fee-body">
            <div class="fee-row">
                <div class="fee-col fee-col-50 form-row mandatory">
                    <label for="categoryName"><g:message code="category.name"/> <span class="suggestion"><g:message code="suggestion.category.name"/></span></label>
                    <input type="text" name="name" id="categoryName" class="medium unique" value="${category.name.encodeAsBMHTML()}" validation="required maxlength[255]" maxlength="255" composite-unique="parent">
                </div>
                <div class="fee-col fee-col-50 form-row mandatory">
                    <label for="categorySku"><g:message code="sku"/><span class="suggestion">(Stock Keeping Unit)</span></label>
                    <input name="sku" id="categorySku" type="text" class="form-full-width" value="${category.sku.encodeAsBMHTML()}" validation="required maxlength[40]" maxlength="40">
                </div>
            </div>
            <div class="fee-row">
                <div class="fee-col fee-col-50 form-row">
                    <label for="categoryHeading"><g:message code="url.identifier"/><span class="suggestion">e.g. my-category</span></label>
                    <input type="text" name="url" id="categoryHeading" class="medium unique" value="${category.url.encodeAsBMHTML()}" validation="maxlength[100]" maxlength="100">
                </div>
                <div class="fee-col fee-col-50 form-row">
                    <label for="categoryTitle"><g:message code="title"/><span class="suggestion">e.g. Detailed Name of Category</span></label>
                    <input type="text" name="title" id="categoryTitle" class="medium" value="${category.title.encodeAsBMHTML()}" validation="maxlength[200]" maxlength="200">
                </div>
            </div>

            <div class="form-row thicker-row">
                <label><g:message code="image"/></label>
                <div class="form-image-block">
                    <input type="file" name="image" file-type="image" previewer="category-image-preview-${category.id}" style="display: none;">
                    <div class="preview-image">
                        <img id="category-image-preview-${category.id}" src="${app.relativeBaseUrl() + "setting/loadDefaultImage?type=category"}">
                    </div>
                </div>
            </div>
            <div class="form-row">
                <label for="summary"><g:message code="summary"/><span class="suggestion"><g:message code="suggestion.category.edit.summary"/></span></label>
                <textarea class="form-full-width" name="summary" id="summary" maxlength="500" validation="maxlength[500]">${category.summary}</textarea>
            </div>
            <div class="form-row">
                <label for="categoryDescription"><g:message code="description"/><span class="suggestion"><g:message code="suggestion.category.edit.description"/></span></label>
                <textarea class="wceditor no-auto-size form-full-width" toolbar-type="advanced" name="description" id="categoryDescription" maxlength="2000" validation="maxlength[2000]">${category.description}</textarea>
            </div>
        </div>
        <div class="fee-button-wrapper fee-config-footer">
            <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="${category.id ? "update" : "save"}"/></button>
            <button type="button" class="cancel-button fee-common"><g:message code="cancel"/></button>
        </div>
    </form>
</div>
