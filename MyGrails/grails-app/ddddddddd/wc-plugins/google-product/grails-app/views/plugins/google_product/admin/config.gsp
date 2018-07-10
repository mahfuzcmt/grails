<%@ page import="com.webcommander.webcommerce.TaxCode" %>
<form class="google-product-settings-from" action="${app.relativeBaseUrl()}googleProductAdmin/saveConfigurations">
    <input type="hidden" name="type" value="google_product">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="google.product.info"/></h3>
            <div class="info-content"><g:message code="section.text.setting.google.product"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row">
                    <label><g:message code="id.mapping"/></label>
                    <input type="radio" name="google_product.id_mapping" value="sku" ${config["id_mapping"] == "sku" ? "checked" : ""}>
                    <span class="value"><g:message code="sku"/></span>
                    <input type="radio" name="google_product.id_mapping" value="id" ${config["id_mapping"] == "id" ? "checked" : ""}>
                    <span class="value"><g:message code="id"/></span>
                </div><div class="form-row">
                    <label><g:message code="description"/></label>
                    <input type="radio" name="google_product.description_mapping" value="text" ${config["description_mapping"] == "text" ? "checked" : ""}>
                    <span class="value"><g:message code="text"/></span>
                    <input type="radio" name="google_product.description_mapping" value="html" ${config["description_mapping"] == "html" ? "checked" : ""}>
                    <span class="value"><g:message code="html"/></span>
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <input type="checkbox" class="single" name="google_product.submit_additional_image" value="true" uncheck-value="false" ${config["submit_additional_image"] == "true" ? "checked" : ""}>
                    <span><g:message code="submit.additional.images"/></span>
                </div><div class="form-row">
                    <input type="checkbox" class="single" name="google_product.submit_sale_price" value="true"  uncheck-value="false"  ${config["submit_sale_price"] == "true" ? "checked" : ""}>
                    <span><g:message code="submit.sale.price"/></span>
                </div>
            </div>
            <div class="form-row">
                <label><g:message code="if.product.identifier.rules.not.satisfiable"/></label>
                <input type="radio" name="google_product.if_identifier_rules_fail" value="reject" ${config["if_identifier_rules_fail"] == "reject" ? "checked" : ""}>
                <span class="value"><g:message code="reject" /></span>
                <input type="radio" name="google_product.if_identifier_rules_fail" value="send_false" ${config["if_identifier_rules_fail"] == "send_false" ? "checked" : ""}>
                <span class="value"><g:message code="send.false"/></span>
            </div>

            <div class="form-row">
                <input type="checkbox" class="single" name="google_product.submit_feed_expiry_date" value="true"  uncheck-value="false"  ${config["submit_feed_expiry_date"] == "true" ? "checked" : ""} toggle-target="feed-expiry-date">
                <span><g:message code="submit.feed.expiry.date"/></span>
            </div><div class="form-row feed-expiry-date chosen-wrapper">
                <label><g:message code="feed.expiry.date"/></label>
                <g:select class="smaller" name="google_product.feed_expiry_date"  from="${1..30}" value="${config["feed_expiry_date"]}"/>
            </div>
            <plugin:isInstalled identifier="variation">
                <div class="form-row">
                    <input type="checkbox" class="single" name="google_product.submit_variations" value="true"  uncheck-value="false"  ${config["submit_variations"] == "true" ? "checked" : ""} toggle-target="variation-mapping">
                    <span><g:message code="submit.variation"/></span>
                </div>
            </plugin:isInstalled>
            <plugin:hookTag hookPoint="googleProductConfigContribution"/>
            <div class="form-row">
                <input type="checkbox" class="single" name="google_product.exclude_out_of_stock_product" value="true"  uncheck-value="false"  ${config["exclude_out_of_stock_product"] == "true" ? "checked" : ""}>
                <span><g:message code="exclude.out.of.stock.products"/></span>
            </div>

            <div class="form-row">
                <input type="checkbox" name="google_product.add_tax" class="single" value="true" uncheck-value="false" ${config["add_tax"] == "true" ? "checked" : ""} toggle-target="tax-code-row">
                <span><g:message code="tax.for.product.feed"/></span>
            </div><div class="form-row tax-code-row chosen-wrapper">
                <label><g:message code="tax.code"/></label>
                <ui:domainSelect domain="${TaxCode}" name="google_product.tax_code" class="medium" value="${config["tax_code"]} "/>
            </div>

            <div class="form-row">
                <input type="checkbox" class="single" name="google_product.submit_shipping_height" value="true"  uncheck-value="false"  ${config["submit_shipping_height"] == "true" ? "checked" : ""}>
                <span><g:message code="submit.shipping.height"/></span>
            </div>

            <div class="form-row">
                <input type="checkbox" class="single" name="google_product.submit_shipping_width" value="true"  uncheck-value="false"  ${config["submit_shipping_width"] == "true" ? "checked" : ""}>
                <span><g:message code="submit.shipping.width"/></span>
            </div>

            <div class="form-row">
                <input type="checkbox" class="single" name="google_product.submit_shipping_length" value="true"  uncheck-value="false"  ${config["submit_shipping_length"] == "true" ? "checked" : ""}>
                <span><g:message code="submit.shipping.length"/></span>
            </div>

            <div class="form-row">
                <input type="checkbox" class="single" name="google_product.submit_shipping_weight" value="true"  uncheck-value="false"  ${config["submit_shipping_weight"] == "true" ? "checked" : ""}>
                <span><g:message code="submit.shipping.weight"/></span>
            </div>

            <div class="form-row">
                <input type="checkbox" class="single" name="google_product.submit_gtin" value="true"  uncheck-value="false"  ${config["submit_gitn"] == "true" ? "checked" : ""}>
                <span><g:message code="submit.gtin"/></span>
            </div>

            <div class="form-row">
                <input type="checkbox" class="single" name="google_product.submit_mpn" value="true"  uncheck-value="false"  ${config["submit_mpn"] == "true" ? "checked" : ""}>
                <span><g:message code="submit.mpn"/></span>
            </div>

            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>