<%@ page import="com.webcommander.webcommerce.Product; com.webcommander.admin.Customer" %>
<div class="toolbar-share">
    <div class="toolbar toolbar-right ">
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<form action="${app.relativeBaseUrl()}setting/saveConfigurations" method="post" class="create-edit-form">
    <input type="hidden" name="type" value="xero">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="order.integration.settings"/></h3>
            <div class="info-content"><g:message code="section.text.xero.order"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row chosen-wrapper">
                <label><g:message code="guest.customer"/></label>
                <ui:domainSelect domain="${Customer}" name="xero.guest_customer" value="${config.guest_customer ? config.guest_customer.toLong(): null}" text="fullName"/>
            </div>
            <div class="form-row chosen-wrapper">
                <label><g:message code="default.customer"/></label>
                <g:select from="${linkedCustomerList}" noSelection="${['':"${g.message(code: 'select.customer')}"]}" optionKey="id" optionValue="firstName" name="xero.default_customer" value="${config.default_customer ? config.default_customer.toLong(): null}"/>
            </div>
            <div class="form-row chosen-wrapper">
                <label><g:message code="default.product"/></label>
                <g:select from="${linkedProductList}" optionKey="id" noSelection="${['':"${g.message(code: 'select.product')}"]}" optionValue="name" name="xero.default_product" value="${config.default_product ? config.default_product.toLong(): null}"/>
            </div>
            <div class="form-row chosen-wrapper">
                <label><g:message code="default.account"/></label>
                <g:select from="${accounts}" optionKey="code" noSelection="${['':"${g.message(code: 'select.account')}"]}" optionValue="name" name="xero.default_account" value="${config.default_account}"/>
            </div>
            <div class="form-row chosen-wrapper">
                <label><g:message code="shipping.account"/></label>
                <g:select from="${shippingAccounts}" optionKey="code" noSelection="${['':"${g.message(code: 'select.account')}"]}" optionValue="name" name="xero.shipping_account" value="${config.shipping_account}"/>
            </div>
            <div class="form-row">
                <input type="checkbox" class="single" name="xero.enable_surcharge_sync" value="true" toggle-target="select-surcharge-account" ${config.enable_surcharge_sync == "true" ? "checked='checked'" : ""}>
                <span><g:message code="enable.surcharge.sync"/></span>
            </div>
            <div class="form-row chosen-wrapper select-surcharge-account">
                <label><g:message code="surcharge.account"/></label>
                <g:select from="${accounts}" noSelection="${['':"${g.message(code: 'select.account')}"]}"  optionKey="code" optionValue="name" name="xero.surcharge_account" value="${config.surcharge_account}"/>
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>