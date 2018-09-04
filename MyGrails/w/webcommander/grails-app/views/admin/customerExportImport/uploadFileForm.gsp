<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants" %>
<g:form controller="customerExportImport" action="initImport" method="post" enctype="multipart/form-data" class="edit-popup-form">
    <div class="form-row drop-file thicker-row">
        <input type="file" name="importFile" validation="drop-file-required" size-limit="104857600" class="medium">
    </div>
    <div class="message-block warning">
        <g:message code="customer.file.upload.warning" args="${[(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customer":"Member"]}"/>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="upload"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</g:form>