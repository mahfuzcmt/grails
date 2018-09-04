<%@ page import="com.webcommander.webcommerce.TaxCode" %>
<form class="google-product-settings-from" action="${app.relativeBaseUrl()}setting/saveConfigurations">
    <input type="hidden" name="type" value="get_price">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="getprice.info"/></h3>
            <div class="info-content"><g:message code="section.text.setting.getprice.product"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row">
                    <input type="checkbox" class="single" name="get_price.show_out_of_stock_product" value="true" uncheck-value="false" ${config["show_out_of_stock_product"] == "true" ? "checked" : ""}>
                    <span><g:message code="show.outofstock.product"/></span>
                </div><div class="form-row">
                    <label><g:message code="tax.code"/></label>
                    <ui:domainSelect domain="${TaxCode}" name="get_price.tax_code" value="${config['tax_code']?.toLong()}" prepend="${['0': g.message(code: 'none')]}"/>
                </div>
            </div>
            <plugin:isInstalled identifier="variation">
                <div class="form-row">
                    <input type="checkbox" class="single" name="get_price.submit_variations" value="true" uncheck-value="false" ${config["submit_variations"] == "true" ? "checked" : ""}>
                    <span><g:message code="submit.variation"/></span>
                </div>
            </plugin:isInstalled>
            <div class="form-row">
                <button type="submit" class="submit-button e-commerce-setting-form-submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>