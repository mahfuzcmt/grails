<div class="toolbar-share">
    <div class="toolbar toolbar-right ">
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<form action="${app.relativeBaseUrl()}myob/saveConfigurations" method="post" class="create-edit-form">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="customer.integration.settings.info"/></h3>
            <div class="info-content"><g:message code="section.text.myob.customer"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row group-row">
                <input type="hidden" name="type" value="myob">
                <label><g:message code="customer.integration.settings"/></label>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <input type="hidden" name="booleans" value="update_customer">
                    <label><g:message code="update.existing.customer"/></label>
                    <input type="checkbox" class="single" name="myob.update_customer" value="true" ${config.update_customer == "true" ? "checked='checked'" : ""}>
                </div><div class="form-row chosen-wrapper">
                    <label><g:message code="phone"/></label>
                    <g:select class="medium" name="myob.customer_phone" from="${["Phone1", "Phone2", "Phone3"]}" value="${config.customer_phone}"/>
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row chosen-wrapper">
                    <label><g:message code="mobile"/></label>
                    <g:select class="medium" name="myob.customer_mobile" from="${["Phone1", "Phone2", "Phone3"]}" value="${config.customer_mobile}"/>
                </div><div class="form-row chosen-wrapper">
                    <label><g:message code="default.customer.tax"/></label>
                    <myob:taxSelector class="medium" name="myob.customer_tax" value="${config.customer_tax}"/>
                </div>
            </div>

            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>