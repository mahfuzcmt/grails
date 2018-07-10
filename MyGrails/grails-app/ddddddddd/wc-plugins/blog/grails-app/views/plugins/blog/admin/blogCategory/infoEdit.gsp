<%@ page import="com.webcommander.plugin.blog.content.BlogCategory" %>
<form action="${app.relativeBaseUrl()}blogAdmin/saveCategory" method="post" class="edit-popup-form create-edit-form" enctype="multipart/form-data">
    <input type="hidden" name="id" value="${category.id}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="blog.category.info"/></h3>
            <div class="info-content"><g:message code="section.text.blog.category.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="name"/><span class="suggestion"> e.g. Blog 1</span></label>
                    <input type="text" class="large unique" name="name" value="${category.name.encodeAsBMHTML()}" validation="required rangelength[2,100]" maxlength="100">
                </div><div class="form-row half">
                <label><g:message code="url.identifier"/><span class="suggestion">  e.g. category-name</span></label>
                <input name="url" type="text" class="form-full-width unique" value="${category.url.encodeAsBMHTML()}" validation="maxlength[100] url_folder" maxlength="100" unique-action="isBlogCategoryUrlUnique">
                </div>
            </div>
            <div class="form-row drop-file thicker-row">
                <label><g:message code="image"/><span class="suggestion"> <g:message code="suggestion.blog.category.image"/></span></label>
                <div class="form-image-block">
                    <input type="file" name="image" file-type="image" size-limit="2097152" previewer="blog-category-image-preview" ${category.image ? 'remove-support="true"' : 'reset-support="true"'} class="large"
                           remove-option-name="remove-image">
                    <div class="preview-image">
                        <g:set var="imagePath" value="${appResource.getBlogCategoryImageUrl(image: category, sizeOrPrefix: "450")}"/>
                        <img id="blog-category-image-preview" src="${imagePath}">
                    </div>
                </div>
            </div>
            <div class="form-row tinymce-container">
                <label><g:message code="description"/><span class="suggestion"> <g:message code="suggestion.blog.category.description"/></span></label>
                <textarea class="wceditor no-auto-size xx-larger" toolbar-type="advanced" maxlength="2000" validation="maxlength[2000]"  name="description">${category.description}</textarea>
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="${category.id ? "update" : "save"}"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</form>