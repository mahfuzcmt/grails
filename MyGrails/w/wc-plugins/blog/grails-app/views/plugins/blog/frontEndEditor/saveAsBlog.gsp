<%@ page import="com.webcommander.plugin.blog.content.BlogCategory" %>
<div class="fee-widget-config-panel article">
    <g:form controller="frontEndEditor" action="saveWidget" class="config-form" onsubmit="return false;">
        <input type="hidden" name="widgetType" value="blogPost">
        <input type="hidden" name="selection" value="custom">
        <div class="fee-config-body fee-noPadding">
            <div class="fee-header-top" style="">
                <div class="fee-row">
                    <div class="fee-col fee-col-50">
                        <div class="fee-add-panel fee-form-row fee-paddingLeft-10">
                            <label for="blogTitle"><g:message code="title"/><span class="suggestion"><g:message code="suggestion.blog.title"/></span></label>
                            <input type="text" class="large unique" name="name" value="" validation="required rangelength[2,100]" maxlength="100" unique-action="isPostUnique">
                        </div>
                        <div class="fee-add-panel fee-form-row fee-paddingLeft-10">
                            <label><g:message code="categories"/><span class="suggestion"> e.g. Blog 1</span></label>
                            <g:set var="categories" value="${BlogCategory.findAllWhere(isDisposable: false)}"/>
                            <g:select multiple="true" name="categories" from="${categories}" data-placeholder="${g.message(code: 'select.categories')}"
                                      optionKey="id" optionValue="name" value=""/>
                        </div>
                        <div class="fee-add-panel fee-form-row fee-paddingLeft-10">
                            <label><g:message code="url.identifier"/><span class="suggestion">  e.g. post-name</span></label>
                            <input name="url" type="text" class="form-full-width unique" value="" validation="maxlength[100] url_folder" maxlength="100" unique-action="isPostUrlUnique">
                        </div>
                    </div>
                    <div class="fee-add-panel article-section fee-col fee-col-50 fee-form-row" style="">
                        <div class="form-row drop-file thicker-row">
                            <label><g:message code="image"/><span class="suggestion"><g:message code="suggestion.blog.image"/></span></label>
                            <div class="form-image-block">
                                <input type="file" name="postImage" file-type="image" size-limit="2097152" previewer="blog-post-image-preview" ${'reset-support="true"'} class="large"
                                       remove-option-name="remove-image">
                                <div class="preview-image">
                                    <g:set var="imagePath" value="${app.relativeBaseUrl() + "setting/loadDefaultImage?type=product"}"/>
                                    <img id="blog-post-image-preview" src="${imagePath}">
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="fee-add-panel fee-padding-30" style="display: none">
                <textarea name="content">${params.blogContent}</textarea>
            </div>
        </div>
        <div class="fee-button-wrapper fee-config-footer">
            <button class="fee-save" type="submit"><g:message code="save"/></button>
            <button class="fee-cancel fee-common" type="button"><g:message code="cancel"/></button>
        </div>
    </g:form>
</div>