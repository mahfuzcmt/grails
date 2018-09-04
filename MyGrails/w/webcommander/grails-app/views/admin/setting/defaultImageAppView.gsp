<%@ page import="com.webcommander.common.ImageService; com.webcommander.constants.DomainConstants; com.webcommander.util.StringUtil; com.webcommander.util.AppUtil;" %>
<form action="${app.relativeBaseUrl()}setting/saveDefaultImageSettings" onsubmit="return false" method="post" enctype="multipart/form-data" class="create-edit-form">
    <div class="form-section">
        <g:each in="${DomainConstants.DEFAULT_IMAGES}" var="defaultImage">
            <g:set var="type" value="${defaultImage.key}"/>
            <g:set var="imgSrc" value="${appResource.getDefaultImageURL(entity: type)}"/>
            <div class="form-section-info">
                <h3><g:message code="${type.toString().replace("-", ".")}"/></h3>
                <div class="info-content"><g:message code="section.text.setting.${type.toString().replace("-", ".")}"/></div>
            </div>
            <div class="form-section-container">
                <div class="form-row">
                    <div class="form-image-block">
                        <input type="file" name="defaultImages.${type}" file-type="image" previewer="${type}-image-preview" reset-support="true" class="medium">
                        <div class="preview-image">
                            <img id="${type}-image-preview" class="image-preview" src="${imgSrc}">
                        </div>
                    </div>
                </div>
                <div class="form-row reset-to-default">
                    <input type="hidden" class="reset-image" name="resetImages.${type}" value="0">
                    <button type="button" class="reset-icon" reset-type="${type}"><g:message code="reset.to.default"/></button>
                </div>
            </div>
            <div class="form-section-separator"></div>
        </g:each>
        <div class="form-row">
            <button class="submit-button" type="submit"><g:message code="update"/></button>
        </div>
    </div>
</form>