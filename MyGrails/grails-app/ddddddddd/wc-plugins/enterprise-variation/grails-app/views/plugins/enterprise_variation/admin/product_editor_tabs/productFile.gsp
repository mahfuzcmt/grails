<%@ page import="org.apache.commons.io.FilenameUtils; com.webcommander.util.FileUtil" %>
<form action="${app.relativeBaseUrl()}enterpriseVariation/updateFile" method="post" class="create-edit-form edit-popup-form" enctype="multipart/form-data">
    <input type="hidden" name="id" value="${details.id}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="product.file"/></h3>
            <div class="info-content"><g:message code="section.text.product.file"/></div>
        </div>
        <input type="checkbox" class="multiple active-check" disable-also="productFile-disable" value="true" ${details?.productFile?.name ? 'checked' : ''}>
        <div class="form-section-container variation-product-file">
            <div class="overlay-panel ${details?.productFile?.name ? '' : 'disabled'}"></div>
            <div class="form-row">
                <input name="productFile" type="file"class="large" text-helper="no">
            </div>
            <div class="form-row">
                <label></label>
                <div class="product-file-block">
                    <g:set var="productFileName" value="${details?.productFile?.name ?: product?.productFile?.name}"/>
                    <g:if test="${productFileName}">
                        <span class="file ${FilenameUtils.getExtension(productFileName)}">
                            <span class="tree-icon"></span>
                        </span>
                        <span class="name"><a target="_blank" href="${app.relativeBaseUrl()}productAdmin/downloadProductFile?id=${details.id}&isVariationFile=${true}">${productFileName}</a></span>
                        <span class="tool-icon remove" file-name="${productFileName}"></span>
                    </g:if>
                </div>
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button productFile-disable"  ${details?.productFile?.name ? '' : 'disabled="disabled"'}><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>