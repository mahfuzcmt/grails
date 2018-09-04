<%@ page import="com.webcommander.plugin.myob.constants.LinkComponent" %>
<div class="toolbar-share">
    <div class="toolbar toolbar-right ">
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<form action="${app.relativeBaseUrl()}myob/saveConfigurations" method="post" class="edit-popup-form">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="product.integration.settings.info"/></h3>
            <div class="info-content"><g:message code="section.text.myob.product"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row group-row">
                <input type="hidden" name="type" value="myob">
                <label><g:message code="product.integration.settings"/></label>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <input type="hidden" name="booleans" value="update_product_name">
                    <label><g:message code="update.product.name"/></label>
                    <input type="checkbox" class="single" name="myob.update_product_name" value="true" ${config.update_product_name == "true" ? "checked='checked'" : ""}>
                </div><div class="form-row">
                    <input type="hidden" name="booleans" value="update_product_description">
                    <label><g:message code="update.product.description"/></label>
                    <input type="checkbox" class="single" name="myob.update_product_description" value="true" ${config.update_product_description == "true" ? "checked='checked'" : ""}>
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <input type="hidden" name="booleans" value="update_product_tax">
                    <label><g:message code="update.product.tax"/></label>
                    <input type="checkbox" class="single" name="myob.update_product_tax" value="true" ${config.update_product_tax == "true" ? "checked='checked'" : ""}>
                </div><div class="form-row">
                    <input type="hidden" name="booleans" value="update_product_baseprice">
                    <label><g:message code="update.product.baseprice"/></label>
                    <input type="checkbox" class="single" name="myob.update_product_baseprice" value="true" ${config.update_product_baseprice == "true" ? "checked='checked'" : ""}>
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <input type="hidden" name="booleans" value="update_product_costprice">
                    <label><g:message code="update.product.costprice"/></label>
                    <input type="checkbox" class="single" name="myob.update_product_costprice" value="true" ${config.update_product_costprice == "true" ? "checked='checked'" : ""}>
                </div><div class="form-row">
                    <input type="hidden" name="booleans" value="update_product_category">
                    <label><g:message code="update.product.category"/></label>
                    <input type="checkbox" class="single" name="myob.update_product_category" value="true" ${config.update_product_category == "true" ? "checked='checked'" : ""}>
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <input type="hidden" name="booleans" value="update_product_manufacturer">
                    <label><g:message code="update.product.manufacturer"/></label>
                    <input type="checkbox" class="single" name="myob.update_product_manufacturer" value="true" ${config.update_product_manufacturer == "true" ? "checked='checked'" : ""}>
                </div><div class="form-row">
                    <input type="hidden" name="booleans" value="update_product_stock">
                    <label><g:message code="update.product.stock"/></label>
                    <input type="checkbox" class="single" name="myob.update_product_stock" value="true" ${config.update_product_stock == "true" ? "checked='checked'" : ""}>
                </div>
            </div>
            <div class="form-row">
                <input type="hidden" name="booleans" value="update_product_inventory">
                <label><g:message code="update.product.inventory"/></label>
                <input type="checkbox" class="single" name="myob.update_product_inventory" value="true" ${config.update_product_inventory == "true" ? "checked='checked'" : ""}>
            </div>

            %{--Commented Codes are required For product Export do not remove it --}%

            %{--<div class="double-input-row">--}%
                %{--<div class="form-row chosen-wrapper">--}%
                    %{--<label><g:message code="default.tax"/></label>--}%
                    %{--<myob:taxSelector class="medium" name="myob.product_default_tax" value="${config.product_default_tax}"/>--}%
                %{--</div><div class="form-row chosen-wrapper">--}%
                    %{--<label><g:message code="default.expense.account"/></label>--}%
                    %{--<myob:accountSelector classification="Expense" class="medium" name="myob.product_expense_account" value="${config.product_expense_account}"/>--}%
                %{--</div>--}%
            %{--</div>--}%
            %{--<div class="double-input-row">--}%
                %{--<div class="form-row chosen-wrapper">--}%
                    %{--<label><g:message code="default.costofsale.account"/></label>--}%
                    %{--<myob:accountSelector classification="CostOfSales" class="medium" name="myob.product_costofsale_account" value="${config.product_costofsale_account}"/>--}%
                %{--</div><div class="form-row chosen-wrapper">--}%
                    %{--<label><g:message code="default.income.account"/></label>--}%
                    %{--<myob:accountSelector classification="Income" class="medium" name="myob.product_income_account" value="${config.product_income_account}"/>--}%
                %{--</div>--}%
            %{--</div>--}%
            %{--<div class="double-input-row">--}%
                %{--<div class="form-row chosen-wrapper">--}%
                    %{--<label><g:message code="default.asset.account"/></label>--}%
                    %{--<myob:accountSelector classification="Asset" class="medium" name="myob.product_asset_account" value="${config.product_asset_account}"/>--}%
                %{--</div><div class="form-row chosen-wrapper">--}%
                    %{--<label><g:message code="default.purchase.account"/></label>--}%
                    %{--<myob:accountSelector classification="Expense" class="medium" name="myob.product_purchase_account" value="${config.product_purchase_account}"/>--}%
                %{--</div>--}%
            %{--</div>--}%
        </div>
        <div class="form-section">
            <div class="form-section-info">
                <h3><g:message code="myob.field.mapping"/></h3>
                <div class="info-content"><g:message code="section.text.myob.product"/></div>
            </div>
            <div class="form-section-container">
                <div class="form-row group-row">
                    <input type="hidden" name="type" value="myob">
                    <label><g:message code="myob.field.mapping"/></label>
                </div>
                <div class="double-input-row">
                    <div class="form-row">
                        <label><g:message code="custom.list.1"/></label>
                        <ui:namedSelect name="myob.mapping_custom_list_1" key="${LinkComponent.CUSTOME_ATTR_MAPPING_NAMED_CONSTANT}" value="${config.mapping_custom_list_1}"/>
                    </div><div class="form-row">
                        <label><g:message code="custom.list.2"/></label>
                        <ui:namedSelect name="myob.mapping_custom_list_2" key="${LinkComponent.CUSTOME_ATTR_MAPPING_NAMED_CONSTANT}" value="${config.mapping_custom_list_2}"/>
                    </div>
                </div>
                <div class="double-input-row">
                    <div class="form-row">
                        <label><g:message code="custom.list.3"/></label>
                        <ui:namedSelect name="myob.mapping_custom_list_3" key="${LinkComponent.CUSTOME_ATTR_MAPPING_NAMED_CONSTANT}" value="${config.mapping_custom_list_3}"/>
                    </div><div class="form-row">
                        <label><g:message code="custom.field.1"/></label>
                        <ui:namedSelect name="myob.mapping_custom_field_1" key="${LinkComponent.CUSTOME_ATTR_MAPPING_NAMED_CONSTANT}" value="${config.mapping_custom_field_1}"/>
                    </div>
                </div>
                <div class="double-input-row">
                    <div class="form-row">
                        <label><g:message code="custom.field.2"/></label>
                        <ui:namedSelect name="myob.mapping_custom_field_2" key="${LinkComponent.CUSTOME_ATTR_MAPPING_NAMED_CONSTANT}" value="${config.mapping_custom_field_2}"/>
                    </div><div class="form-row">
                        <label><g:message code="custom.field.3"/></label>
                        <ui:namedSelect name="myob.mapping_custom_field_3" key="${LinkComponent.CUSTOME_ATTR_MAPPING_NAMED_CONSTANT}" value="${config.mapping_custom_field_3}"/>
                    </div>
                </div>
                <div class="form-row">
                    <button type="submit" class="submit-button"><g:message code="update"/></button>
                </div>
            </div>
        </div>
    </div>

</form> 