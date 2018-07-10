<%@ page import="com.webcommander.plugin.snippet.constants.SnippetConstants" %>
<form class="edit-popup-form snippet-template-upload-form" method="post" action="${app.relativeBaseUrl()}snippetTemplate/save" enctype="multipart/form-data">
    <input type="hidden" name="uuid" value="${info.uuid}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="snippet.template.info"/></h3>
            <div class="info-content"><g:message code="section.text.snippet.template.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row">
                    <label><g:message code="name"/></label>
                    <input type="text" name="name" validation="required maxlength[50]" maxlength="50" value="${info.name}">
                </div><div class="form-row">
                <label><g:message code="category"/></label>
                <ui:namedSelect key="${SnippetConstants.SNIPPET_TEMPLATE_CATEGORY_NAMES}" name="category" value="${info.category}"/>
            </div>
            </div>
            <div class="bmui-tab">
                <div class="bmui-tab-header-container top-side-header">
                    <div class="bmui-tab-header" data-tabify-tab-id="html">
                        <span class="title"><g:message code="html"/></span>
                    </div>

                    <div class="bmui-tab-header" data-tabify-tab-id="css">
                        <span class="title"><g:message code="css"/></span>
                    </div>
                </div>
                <div class="bmui-tab-body-container">
                    <div id="bmui-tab-html">
                        <textarea class="code-mirror-editor no-auto-size xx-larger data-html52" name="html" toolbar-type="advanced" style="height: 400px;">${info.html}</textarea>
                    </div>
                    <div id="bmui-tab-css">
                        <textarea class="code-mirror-editor no-auto-size xx-larger data-css5" name="css" style="height: 350px;">${info.css}</textarea>
                    </div>
                </div>
            </div>
            <div class="form-row thicker-row">
                <label><g:message code="thumbnail.image"/></label>
                <div class="form-image-block">
                    <input type="file" name="thumb" file-type="image" remove-option-name="remove-image" size-limit="2097152" previewer="snippet-template-thumb-preview" validation="${info.uuid ? "" : "drop-file-required"}">
                    <div class="preview-image">
                        <g:set var="src" value="${info.thumb ?: appResource.getSnippetThumbDefaultImage()}"/>
                        <img id="snippet-template-thumb-preview" src="${src}">
                    </div>
                </div>
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="update"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</form>