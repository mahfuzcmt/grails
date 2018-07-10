<%@ page import="com.webcommander.webcommerce.TaxCode" %>
<form class="my-shopping-settings-from create-edit-form" action="${app.relativeBaseUrl()}setting/saveConfigurations">
    <input type="hidden" name="type" value="my_shopping">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="my.shopping"/></h3>
            <div class="info-content"><g:message code="section.text.my.shopping.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row chosen-wrapper">
                <label><g:message code="tax.code"/><span class="suggestion">e.g. outside 1.1</span></label>
                <ui:domainSelect domain="${TaxCode}" key="id" name="my_shopping.tax_code" class="medium" value="${config.tax_code == "none" ? config.tax_code : config.tax_code.toLong()}" prepend="${[none: g.message(code: "none")]}"/>
            </div>
            <plugin:isInstalled name="variation">
                <div class="form-row">
                    <label><g:message code="submit.variation"/></label>
                    <input type="checkbox" class="single" name="my_shopping.submit_variations" value="true"  uncheck-value="false"  ${config["submit_variations"] == "true" ? "checked" : ""}>
                </div>
            </plugin:isInstalled>
            <div class="form-row">
                <label><g:message code="submit.combination.product"/></label>
                <input type="checkbox" class="single" name="my_shopping.submit_combination" value="true"  uncheck-value="false"  ${config["submit_combination"] == "true" ? "checked" : ""}>
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>