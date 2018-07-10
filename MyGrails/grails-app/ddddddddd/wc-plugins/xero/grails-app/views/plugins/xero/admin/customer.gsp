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
            <h3><g:message code="customer.integration.settings.info"/></h3>
            <div class="info-content"><g:message code="section.text.xero.customer"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row group-row">
                <input type="hidden" name="type" value="xero">
                <label><g:message code="customer.integration.settings"/></label>
            </div><div class="double-input-row">
                <div class="form-row">
                    <input type="hidden" name="booleans" value="update_customer">
                    <label><g:message code="update.existing.customer"/></label>
                    <input type="checkbox" class="single" name="xero.update_customer" value="true" ${config.update_customer == "true" ? "checked='checked'" : ""}>
                </div>
            </div>

            <div class="double-input-row">
                <div class="form-row ">
                    <label><g:message code="address"/></label>
                    <g:select class="medium" name="xero.address" from="${["${g.message(code: 'xero.street.address')}": "Street", "${g.message(code: 'xero.postal.address')}": "Postal"]}" optionKey="value" optionValue="key" value="${config.address}"/>
                </div><div class="form-row ">
                    <label><g:message code="shipping.address"/></label>
                    <g:select class="medium" name="xero.activeShippingAddress" from="${["${g.message(code: 'xero.street.address')}": "Street", "${g.message(code: 'xero.postal.address')}": "Postal"]}" optionKey="value" optionValue="key" value="${config.activeShippingAddress}"/>
                </div>
            </div>
            <div class="form-row ">
                <label><g:message code="billing.address"/></label>
                <g:select class="medium" name="xero.activeBillingAddress" from="${["${g.message(code:'select')}":"", "${g.message(code: 'xero.street.address')}": "Street", "${g.message(code: 'xero.postal.address')}": "Postal"]}" optionKey="value" optionValue="key" value="${config.activeBillingAddress}"/>
            </div>
            <div class="double-input-row">
                <div class="form-row ">
                    <label><g:message code="phone"/></label>
                    <g:select class="medium" name="xero.phone" from="${["${g.message(code:'select')}":"", "${g.message(code: 'phone')}": "Phone", "${g.message(code: 'mobile')}": "Mobile", "${g.message(code: 'fax')}": "Fax"]}" optionKey="value" optionValue="key" value="${config.phone}"/>
                </div><div class="form-row ">
                    <label><g:message code="mobile"/></label>
                    <g:select class="medium" name="xero.mobile" from="${["${g.message(code:'select')}":"", "${g.message(code: 'phone')}": "Phone", "${g.message(code: 'mobile')}": "Mobile", "${g.message(code: 'fax')}": "Fax"]}" optionKey="value" optionValue="key" value="${config.mobile}"/>
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row ">
                    <label><g:message code="fax"/></label>
                    <g:select class="medium" name="xero.fax" from="${["${g.message(code:'select')}":"", "${g.message(code: 'phone')}": "Phone", "${g.message(code: 'mobile')}": "Mobile", "${g.message(code: 'fax')}": "Fax"]}" optionKey="value" optionValue="key" value="${config.fax}"/>
                </div><div class="form-row ">
                    <label><g:message code="email"/></label>
                    <g:select class="medium" name="xero.email" from="${["${g.message(code:'select')}":"", "${g.message(code: 'xero.primary.person')}": "PrimaryPerson", "${g.message(code: 'xero.another.person.1')}": "Another1", "${g.message(code: 'xero.another.person.2')}": "Another2", "${g.message(code: 'xero.another.person.3')}": "Another3", "${g.message(code: 'xero.another.person.4')}": "Another4"]}" optionKey="value" optionValue="key" value="${config.email}"/>
                </div>
            </div>

            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>