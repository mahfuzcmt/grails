<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil; com.webcommander.plugin.blog.content.BlogCategory" %>
<form action="${app.relativeBaseUrl()}blogAdmin/savePost" method="post" class="edit-popup-form create-edit-form" enctype="multipart/form-data">
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
                <input type="hidden" name="id" value="${post.id}">
                <div class="form-section">
                    <div class="form-section-info">
                        <h3><g:message code="blog.post.info"/></h3>
                        <div class="info-content"><g:message code="section.text.blog.post.info"/></div>
                    </div>
                    <div class="form-section-container">
                        <div class="double-input-row">
                            <div class="form-row mandatory">
                                <label><g:message code="title"/><span class="suggestion"><g:message code="suggestion.blog.title"/></span></label>
                                <input type="text" class="large unique" name="name" value="${post.name.encodeAsBMHTML()}" validation="required rangelength[2,100]" maxlength="100" unique-action="isPostUnique">
                            </div><div class="form-row half">
                            <label><g:message code="url.identifier"/><span class="suggestion">  e.g. post-name</span></label>
                            <input name="url" type="text" class="form-full-width unique" value="${post.url.encodeAsBMHTML()}" validation="maxlength[100] url_folder" maxlength="100" unique-action="isPostUrlUnique">
                            </div>
                        </div>
                        <div class="double-input-row">
                            <div class="form-row chosen-wrapper">
                                <label><g:message code="categories"/><span class="suggestion"> e.g. Blog 1</span></label>
                                <g:set var="categories" value="${BlogCategory.findAllWhere(isDisposable: false)}"/>
                                <g:select multiple="true" name="categories" from="${categories}" data-placeholder="${g.message(code: 'select.categories')}"
                                          optionKey="id" optionValue="name" value="${selectedCategories.id}"/>
                            </div><div class="form-row mandatory">
                                <label><g:message code="date"/><span class="suggestion"><g:message code="suggestion.blog.date"/></span></label>
                                <input name="date" type="text" class="large datefield" validation="required" value="${post.date ? post.date.toZone(session.timezone).toFormattedString("yyyy-MM-dd", false, "", false, session.timezone) : ""}">
                            </div>
                        </div>
                        <div class="double-input-row">
                            <div class="form-row">
                                <label><g:message code="status"/><span class="suggestion"> e.g. Published</span></label>
                                <g:select class="large" name="isPublished" from="${[g.message(code: "published"), g.message(code: "unpublished")]}" keys="${["true", "false"]}" value="${post.isPublished}"></g:select>
                            </div><div class="form-row">
                                <label><g:message code="visibility"/><span class="suggestion"> e.g. Open</span> </label>
                                <g:select class="large" toggle-target="restricted-visibility-row" toggle-anim="slide" name="visibility" from="${[g.message(code: "open"), g.message(code: "restricted"), g.message(code: "hidden")]}" keys="${["open", "restricted", "hidden"]}" value="${post.visibility}"></g:select>
                            </div>
                        </div>
                        <div class="form-row restricted-visibility-row-restricted">
                            <label><g:message code="visible.to"/></label>
                            <input type="radio" name="visibleTo" class="radio" value="all" ${post.visibleTo ? (post.visibleTo == "all"? "checked" : "") : "checked"}>
                            <g:message code="all.customer" args="${[(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customer":"Member"]}"/>&nbsp;&nbsp;
                            <input type="radio" name="visibleTo" class="radio" value="selected"  ${post.visibleTo ? (post.visibleTo == "selected" ? "checked" : "") : ""}>
                            <g:message code="selected.customers.or.groups"/>
                            <span class="tool-icon choose choose-customer"></span>
                        </div>
                        <g:each in="${post.customers}" var="customer">
                            <input type="hidden" name="customer" value="${customer.id}">
                        </g:each>
                        <g:each in="${post.groups}" var="group">
                            <input type="hidden" name="customerGroup" value="${group.id}">
                        </g:each>
                        <div class="form-row drop-file thicker-row">
                            <label><g:message code="image"/><span class="suggestion"><g:message code="suggestion.blog.image"/></span></label>
                            <div class="form-image-block">
                                <input type="file" name="postImage" file-type="image" size-limit="2097152" previewer="blog-post-image-preview" ${post.image ? 'remove-support="true"' : 'reset-support="true"'} class="large"
                                       remove-option-name="remove-image">
                                <div class="preview-image">
                                    <g:set var="imagePath" value="${appResource.getBlogPostImageUrl(image: post, sizeOrPrefix: "450")}"/>
                                    <img id="blog-post-image-preview" src="${imagePath}">
                                </div>
                            </div>
                        </div>
                        <div class="form-row tinymce-container">
                            <label><g:message code="content"/><span class="suggestion"><g:message code="suggestion.blog.content"/></span></label>
                            <textarea class="wceditor no-auto-size xx-larger" toolbar-type="advanced" maxlength="65000" validation="maxlength[65000]"  name="content">${post.content}</textarea>
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
                        <g:include view="/admin/metatag/metaTagEditor.gsp" model="${[metaTags: post.metaTags]}"/>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="form-row wcui-horizontal-tab-button">
        <button type="submit" class="submit-button"><g:message code="${post.id ? "update" : "save"}"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>