<%@ page import="org.apache.commons.io.FilenameUtils; com.webcommander.util.FileUtil" %>
<form action="${app.relativeBaseUrl()}productAdmin/updateFile" method="post" class="create-edit-form" enctype="multipart/form-data">
    <input type="hidden" name="id" value="${product.id}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="product.file"/></h3>
            <div class="info-content"><g:message code="section.text.product.file"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <input name="productFile" type="file"class="large" text-helper="no">
            </div>
            <div class="form-row">
                <label></label>
                <div class="product-file-block">
                    <g:if test="${product?.productFile?.name}">
                        <span class="file ${FilenameUtils.getExtension(product?.productFile?.name)}">
                            <span class="tree-icon"></span>
                        </span>
                        <span class="name"><a target="_blank" href="${app.relativeBaseUrl()}productAdmin/downloadProductFile?id=${product.id}">${product?.productFile?.name}</a></span>
                        <span class="tool-icon remove" file-name="${product?.productFile?.name}"></span>
                    </g:if>
                </div>
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>