<%@ page import="com.webcommander.admin.Zone" %>
<div class="toolbar-share">
    <div class="toolbar toolbar-right ">
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<form action="${app.relativeBaseUrl()}xero/saveConfigurations" method="post" class="create-edit-form">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="tax.code.setting.info"/></h3>
            <div class="info-content"><g:message code="section.text.xero.tax.code"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row group-row">
                <input type="hidden" name="type" value="xero">
                <label><g:message code="tax.integration.settings"/></label>
            </div>
            <div class="form-row">
                <input type="hidden" name="booleans" value="update_tax">
                <label><g:message code="update.tax"/></label>
                <input type="checkbox" class="single" name="xero.update_tax" value="true" ${config.update_tax == "true" ? "checked='checked'" : ""}>
            </div>
            <div class="form-row">
                <input type="hidden" name="booleans" value="update_tax">
                <label><g:message code="default.zone"/></label>
                <ui:domainSelect domain="${Zone}" name="xero.tax_default_zone" value="${config.tax_default_zone}" />
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>