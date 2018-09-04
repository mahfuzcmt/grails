<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants" %>
<g:set var="compareProduct" value="${DomainConstants.SITE_CONFIG_TYPES.COMPARE_PRODUCT}"/>
<form action="${app.relativeBaseUrl()}setting/saveConfigurations" method="POST" class="create-edit-form">
    <input type="hidden" name="type" value="${compareProduct}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="compare.product"/></h3>
            <div class="info-content"><g:message code="section.text.setting.compare.product"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row mandatory">
                <label><g:message code="maximum.product"/><span class="suggestion">e.g. 5</span></label>
                <input type="text" class="medium" name="${compareProduct}.maximum_product" value="${config["maximum_product"]}" validation="required number max[5] min[2]" restrict="numeric">
            </div>
            <div class="form-row">
                <label><g:message code="show.hide"/></label>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <input type="checkbox" class="single" name="${compareProduct}.add_to_cart_active" value="true"
                           uncheck-value="false" ${config["add_to_cart_active"] == "true" ? "checked" : ""}>
                    <span><g:message code="add.to.cart"/></span>
                </div><div class="form-row">
                    <input type="checkbox" class="single" name="${compareProduct}.image_active" value="true"
                           uncheck-value="false" ${config["image_active"] == "true" ? "checked" : ""}>
                    <span><g:message code="image"/></span>
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <input type="checkbox" class="single" name="${compareProduct}.name_active" value="true"
                           uncheck-value="false" ${config["name_active"] == "true" ? "checked" : ""}>
                    <span><g:message code="name"/></span>
                </div><div class="form-row">
                    <input type="checkbox" class="single" name="${compareProduct}.sku_active" value="true"
                           uncheck-value="false" ${config["sku_active"] == "true" ? "checked" : ""}>
                    <span><g:message code="sku"/></span>
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <input type="checkbox" class="single" name="${compareProduct}.price_active" value="true"
                           uncheck-value="false" ${config["price_active"] == "true" ? "checked" : ""}>
                    <span><g:message code="price"/></span>
                </div><div class="form-row">
                    <input type="checkbox" class="single" name="${compareProduct}.summary_active" value="true"
                           uncheck-value="false" ${config["summary_active"] == "true" ? "checked" : ""}>
                    <span><g:message code="summary"/></span>
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <input type="checkbox" class="single" name="${compareProduct}.description_active" value="true"
                           uncheck-value="false" ${config["description_active"] == "true" ? "checked" : ""}>
                    <span><g:message code="description"/></span>
                </div>
            </div>
            <div class="double-input-row">
               <div class="form-row">
                    <input type="checkbox" class="single" name="${compareProduct}.model_active" value="true"
                           uncheck-value="false" ${config["model_active"] == "true" ? "checked" : ""}>
                    <span><g:message code="model"/></span>
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <input type="checkbox" class="single" name="${compareProduct}.width_active" value="true"
                           uncheck-value="false" ${config["width_active"] == "true" ? "checked" : ""}>
                    <span><g:message code="width"/></span>
                </div><div class="form-row">
                    <input type="checkbox" class="single" name="${compareProduct}.height_active" value="true"
                           uncheck-value="false" ${config["height_active"] == "true" ? "checked" : ""}>
                    <span><g:message code="height"/></span>
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <input type="checkbox" class="single" name="${compareProduct}.length_active" value="true"
                           uncheck-value="false" ${config["length_active"] == "true" ? "checked" : ""}>
                    <span><g:message code="length"/></span>
                </div><div class="form-row">
                    <input type="checkbox" class="single" name="${compareProduct}.weight_active" value="true"
                           uncheck-value="false" ${config["weight_active"] == "true" ? "checked" : ""}>
                    <span><g:message code="weight"/></span>
                </div>
            </div>
            <div class="form-row">
                <label><g:message code="label.for.call.for.price"/></label>
                <input type="text" name="${compareProduct}.label_for_call_for_price" value="${config.label_for_call_for_price}" validation="required">
            </div>
            <plugin:hookTag hookPoint="compareProductConfigFieldsEnd" attrs="[configType: compareProduct, configs: config]"/>
            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>
