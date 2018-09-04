<%@ page import="com.webcommander.webcommerce.TaxCode; com.webcommander.constants.DomainConstants; com.webcommander.webcommerce.TaxProfile" %>
<form class="create-edit-form" action="${app.relativeBaseUrl()}dashboard/updateTax">
    <input type="hidden" name="type" value="${DomainConstants.SITE_CONFIG_TYPES.TAX}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="tax.info"/></h3>
            <div class="info-content"><g:message code="section.text.tax.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row chosen-wrapper">
                    <label><g:message code="default.currency"/><span class="suggestion"><g:message code="suggestion.setting.default.currency"/></span></label>
                    <ui:domainSelect name="default_currency" domain="${com.webcommander.webcommerce.Currency}" prepend="${["": g.message(code: "select")]}" value="${currency.id}"/>
                </div><div class="form-row chosen-wrapper">
                    <label><g:message code="default.tax.profile"/><span class="suggestion"><g:message code="suggestion.setting.tax.profile"/></span></label>
                    <ui:domainSelect name="tax.default_tax_profile" domain="${TaxProfile}" value="${taxSettings.default_tax_profile.toLong(0)}" custom-attrs="${['toggle-target': "tax-free"]}"/>
                </div>
            </div>
            <div class="double-input-row tax-free-2 tax-free-" do-reverse-toggle>
                <div class="form-row">
                    <label><g:message code="price.entered.with.tax"/><span class="suggestion"><g:message code="suggestion.setting.tax.price.entered.tax"/></span></label>
                    <input type="checkbox" name="tax.is_price_with_tax" value="true" class="single" uncheck-value="false"${taxSettings.is_price_with_tax == "true" ? " checked" : ""}/>
                </div><div class="form-row">
                    <label><g:message code="show.price.with.tax"/><span class="suggestion"><g:message code="suggestion.setting.tax.show.price.tax"/></span></label>
                    <input type="checkbox" name="tax.show_price_with_tax" value="true" class="single" uncheck-value="false"${taxSettings.show_price_with_tax == "true" ? " checked" : ""}/>
                </div>
            </div>
            %{--block for future--}%
            %{--<div class="form-row">
                <label><g:message code="message"/><span class="suggestion"><g:message code="suggestion.setting.tax.message"/></span></label>
                <textarea name="message" name="tax.tax_message" validation="maxlength[500]" maxlength="500">${taxSettings.tax_message.encodeAsBMHTML()}</textarea>
            </div>--}%
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="update"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</form>