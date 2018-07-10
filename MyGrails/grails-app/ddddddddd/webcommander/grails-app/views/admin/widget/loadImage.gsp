<%@ page import="com.webcommander.util.StringUtil" %>
<g:form class="create-edit-form" controller="widget" action="saveImageWidget" enctype="multipart/form-data">
    <span class="configure-btn" title="<g:message code="configuration"/> "><i></i></span>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="image.selection.info"/></h3>
            <div class="info-content"><g:message code="section.text.iamge.selection"/></div>
        </div>
        <div class="form-section-container">
            <div class="widget-config-panel">
                <div class="form-row">
                    <label><g:message code="title"/></label>
                    <input type="text" class="medium" name="title" value="${widget.title}">
                </div>
                <div class="form-row">
                    <label><g:message code="alt.text"/></label>
                    <input type="text" class="medium" name="alt_text" value="${config.alt_text}">
                </div>
                <div class="form-row">
                    <label><g:message code="hyperlink.url"/></label>
                    <input type="text" class="medium" name="hype_url" value="${config.hype_url}">
                </div>
                <div class="form-row">
                    <label><g:message code="link.target"/></label>
                    <g:select class="medium" name="link_target" from="${['_self', '_blank', '_parent', '_top']}" keys="${['_self', '_blank', '_parent', '_top']}" value="${config.link_target}" />
                </div>
            </div>
            <g:set var="ref1" value="${StringUtil.uuid}"/>
            <g:set var="ref2" value="${StringUtil.uuid}"/>
            <g:set var="ref3" value="${StringUtil.uuid}"/>
            <g:set var="ref4" value="${StringUtil.uuid}"/>
            <div class="form-row">
                <span class="label-block asset-depend local-depend">
                    <input type="radio" id="${ref1}" name="upload_type" class="direct-upload" value="direct" ${config.upload_type == "direct" ? "checked" : ""}>
                    <span><g:message code="direct.url"/></span>
                </span>
                <input type="text" class="medium" name="direct_url" value="${config.upload_type == "direct" ? widget.content : ""}" validation="skip@not{global:#${ref1}:checked} required partial_url" depends=".direct-depend">
            </div>
            <div class="form-row">
                <span class="label-block direct-depend local-depend">
                    <input type="radio" id="${ref2}" name="upload_type" id="image-widget-upload-type-asset-library" class="asset-library-upload" value="asset_library" ${config.upload_type == "asset_library" ? "checked" : ""}>
                    <span><g:message code="asset.library"/></span>
                </span>
                <input type="text" validation="required@if{global:#${ref2}:checked}" readonly class="medium" name="asset_library_url" value="${config.upload_type == "asset_library" ? widget.content : ""}" depends=".asset-depend">
                <span class="tool-icon select-from-asset-library" title="<g:message code="asset.library"/> "></span>
            </div>
            <div class="form-row">
                <span class="label-block direct-depend asset-depend">
                    <input type="radio" id="${ref3}" class="local-image-upload" name="upload_type" value="local" ${config.upload_type == "local" ? "checked" : ""}>
                    <span><g:message code="local.file"/></span>
                </span>
                <input type="hidden" class="${config.upload_type == "local" ? "has_url" : ""}" name="local_url" value="${config.upload_type == "local" ? widget.content : ""}" id="${ref4}">
                <input depends=".local-depend" type="file" name="localImage" file-type="image" previewer="widget-image-preview" validation="skip@if{global:#${ref4}.has_url} drop-file-required@if{global:#${ref3}:checked}">
            </div>
            <fieldset>
                <legend><g:message code="preview"/> </legend>
                <div class="img-preview">
                    <img id="widget-image-preview" src="${widget.content}" ${widget.content ? "" : "style='display: none'"}>
                </div>
            </fieldset>
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="update"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</g:form>