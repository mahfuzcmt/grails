<div class="toolbar-share">
    <div class="toolbar toolbar-right ">
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<%@ page import="com.webcommander.admin.Zone" %>
<form action="${app.relativeBaseUrl()}myob/saveConfigurations" method="post" class="create-edit-form">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="tax.code.setting.info"/></h3>
            <div class="info-content"><g:message code="section.text.myob.tax.code"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row group-row">
                <input type="hidden" name="type" value="myob">
                <label><g:message code="tax.integration.settings"/></label>
            </div>
            <div class="form-row">
                <input type="hidden" name="booleans" value="update_tax">
                <label><g:message code="update.tax"/></label>
                <input type="checkbox" class="single" name="myob.update_tax" value="true" ${config.update_tax == "true" ? "checked='checked'" : ""}>
            </div>
            <div class="form-row">
                <input type="hidden" name="booleans" value="update_tax">
                <label><g:message code="default.zone"/></label>
                <ui:domainSelect domain="${Zone}" name="myob.tax_default_zone" value="${config.tax_default_zone}" />
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>