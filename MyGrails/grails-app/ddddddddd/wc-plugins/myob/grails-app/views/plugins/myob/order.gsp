<%@ page import="com.webcommander.webcommerce.Product; com.webcommander.admin.Customer" %>
<div class="toolbar-share">
    <div class="toolbar toolbar-right ">
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<form action="${app.relativeBaseUrl()}myob/saveConfigurations" method="post" class="create-edit-form">
    <input type="hidden" name="type" value="myob">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="order.integration.settings"/></h3>
            <div class="info-content"><g:message code="section.text.myob.order"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row chosen-wrapper">
                <label><g:message code="guest.customer"/></label>
                <ui:domainSelect domain="${Customer}" name="myob.guest_customer" value="${config.guest_customer?.toLong()}" text="fullName"/>
            </div>
            <div class="form-row chosen-wrapper">
                <label><g:message code="default.customer"/></label>
                <g:select from="${linkedCustomerList}" noSelection="${['':"${g.message(code: 'select.customer')}"]}" optionKey="id" optionValue="firstName" name="myob.default_customer" value="${config.default_customer}"/>
            </div>
            <div class="form-row chosen-wrapper">
                <label><g:message code="default.product"/></label>
                <g:select from="${linkedProductList}" optionKey="id" noSelection="${['':"${g.message(code: 'select.product')}"]}" optionValue="name" name="myob.default_product" value="${config.default_product}"/>
            </div>
            <div class="form-row">
                <input type="checkbox" class="single" name="myob.enable_surcharge_sync" value="true" toggle-target="select-surcharge-account" ${config.enable_surcharge_sync == "true" ? "checked='checked'" : ""}>
                <span><g:message code="enable.surcharge.sync"/></span>
            </div>
            <div class="form-row chosen-wrapper">
                <label><g:message code="surcharge.line.product"/></label>
                <g:select from="${linkedProductList}" optionKey="id" noSelection="${['':"${g.message(code: 'select.product')}"]}" optionValue="name" name="myob.default_surcharge_line_product" value="${config.default_surcharge_line_product}"/>
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>