<%@ page import="com.webcommander.constants.DomainConstants" %>
<div class="toolbar-share">
    <div class="toolbar toolbar-right ">
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<form action="${app.relativeBaseUrl()}setting/saveConfigurations" method="POST" class="create-edit-form">
    <g:set var="type" value="${DomainConstants.SITE_CONFIG_TYPES.XERO}"/>
    <input name="type" type="hidden" value="${type}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="xero.product.configuration.info"/></h3>
            <div class="info-content"><g:message code="section.text.xero.product.config"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row">
                    <label><g:message code="update.name"/></label>
                    <input type="checkbox" class="single" name="${type}.update_item_name" value="true" uncheck-value="false" ${config["update_item_name"] ==  "true" ? "checked" : ""}>
                </div><div class="form-row">
                <label><g:message code="update.description"/></label>
                <input type="checkbox" class="single" name="${type}.update_item_description" value="true" uncheck-value="false" ${config["update_item_description"] ==  "true" ? "checked" : ""}>
            </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <label><g:message code="update.cost.price"/></label>
                    <input type="checkbox" class="single" name="${type}.update_item_base_price" value="true" uncheck-value="false" ${config["update_item_cost_price"] ==  "true" ? "checked" : ""}>
                </div><div class="form-row">
                <label><g:message code="update.sale.price"/></label>
                <input type="checkbox" class="single" name="${type}.update_item_cost_price" value="true" uncheck-value="false" ${config["update_item_cost_price"] ==  "true" ? "checked" : ""}>
            </div>
            </div>
            <div class="form-row">
                <label><g:message code="update.tax"/></label>
                <input type="checkbox" class="single" name="${type}.update_item_tax" value="true" disabled="disabled" uncheck-value="false" ${config["update_item_tax"] ==  "true" ? "checked" : ""}>
            </div>
            %{--<div class="double-input-row">
                <div class="form-row">
                    <label><g:message code="default.purchase.account"/></label>
                    <g:select name="${type}.default_item_purchase_details_account" from="${accounts}" class="medium" optionKey="code" optionValue="name" value="${config["default_item_purchase_details_account"]}"/>
                </div><div class="form-row">
                <label><g:message code="default.sales.account"/></label>
                <g:select name="${type}.default_item_sales_details_account" from="${accounts}" class="medium" optionKey="code" optionValue="name" value="${config["default_item_sales_details_account"]}"/>
            </div>
            </div>--}%
            <div class="form-row">
                <button class="submit-button"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>