<%@ page import="com.webcommander.admin.Country; com.webcommander.constants.NamedConstants; com.webcommander.webcommerce.TaxProfile; com.webcommander.constants.DomainConstants" %>
<form action="${app.relativeBaseUrl()}setting/saveTaxAndCurrency" method="post"  class="create-edit-form">
    <input type="hidden" name="type" value="${DomainConstants.SITE_CONFIG_TYPES.CURRENCY}">
    <input type="hidden" name="type" value="${DomainConstants.SITE_CONFIG_TYPES.TAX}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="currency"/></h3>
            <div class="info-content"><g:message code="section.text.setting.currency"/></div>
        </div>
        <div class="form-section-container">
            <div class="base-currency-section">
                <g:include controller="currencyAdmin" action="loadCurrencyForSettings" params="[base: 'true']"/>
            </div>
            <div class="currency-section">
                <g:include controller="currencyAdmin" action="loadCurrencyForSettings"/>
            </div>
        </div>
    </div>
    <div class="section-separator"></div>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="tax"/></h3>
            <div class="info-content"><g:message code="section.text.setting.tax"/></div>
        </div>
        <div class="form-section-container">

            <div class="double-input-row">
                <div class="form-row chosen-wrapper">
                    <label><g:message code="tax.configuration"/><span class="suggestion"><g:message code="suggestion.setting.tax.price.entered.tax"/></span></label>
                    %{--<input type="checkbox" name="tax.configuration_type" class="single" value="true" uncheck-value="false" toggle-target="tax-config-row-${DomainConstants.TAX_CONFIGURATION_TYPE.MANUAL}" ${taxSettings.configuration_type == DomainConstants.TAX_CONFIGURATION_TYPE.MANUAL ? "checked" : ""}/>--}%
                    <ui:namedSelect name="tax.configuration_type" class="medium" key="${NamedConstants.TAX_CONFIGURATION}" value="${taxSettings.configuration_type}" toggle-target="tax-config-row"/>
                </div>
                <div class="form-row chosen-wrapper tax-config-row-${DomainConstants.TAX_CONFIGURATION_TYPE.DEFAULT}" >
                    <label><g:message code="tax.default.country"/><span class="suggestion"><g:message code="suggestion.setting.tax.price.entered.tax"/></span></label>
                    <ui:namedSelect name="tax.default_country" class="medium " key="${NamedConstants.TAX_DEFAULT_COUNTRY_CUSTOM}" value="${taxSettings.default_country}"/>
                    %{--<span class="link-btn customize-tax ${taxSettings.configuration_type == "manual" ? "active" : ""}"><g:message code="customize"/></span>--}%
                </div>
                <div class="form-row chosen-wrapper tax-config-row-${DomainConstants.TAX_CONFIGURATION_TYPE.MANUAL}">
                    <label><g:message code="default.tax.profile"/><span class="suggestion"><g:message code="suggestion.setting.tax.profile"/></span></label>
                    %{--<ui:domainSelect name="tax.default_tax_profile" class="medium tax-profile-selector" domain="${TaxProfile}" prepend="${['': g.message(code: "no.tax")]}" value="${taxSettings.default_tax_profile.toLong(0)}"/>--}%
                    <g:select name="tax.default_tax_profile" class="medium tax-profile-selector" from="${profiles}" noSelection="['': g.message(code: 'none')]" optionValue="name" optionKey="id" value="${taxSettings.default_tax_profile.toLong(0)}"/>
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row chosen-wrapper">
                    <label><g:message code="tax.message"/><span class="suggestion"><g:message code="suggestion.setting.tax.message"/></span></label>
                    <input type="text" class="medium" name="tax.tax_message" value="${taxSettings.tax_message.encodeAsBMHTML()}" validation="maxlength[500]" maxlength="500">
                    <span class="note"><g:message code="supported.macros.are"/> %code%, %rate% and %amount%</span>
                </div>
                <div class="form-row thicker-row">
                    <label><g:message code="show.price.with.tax"/><span class="suggestion"><g:message code="suggestion.setting.tax.show.price.tax"/></span></label>
                    <g:select name="tax.show_price_with_tax" class="medium" from="${[g.message(code: "yes"), g.message(code: "no")]}" keys="${["true", "false"]}" value="${taxSettings.show_price_with_tax}"/>
                </div>
            </div>
            <div class="triple-input-row">
                <div class="form-row chosen-wrapper">
                    <label><g:message code="price.entered.with.tax"/><span class="suggestion"><g:message code="suggestion.setting.tax.price.entered.tax"/></span></label>
                    <g:select name="tax.is_price_with_tax" class="medium" from="${[g.message(code: "yes"), g.message(code: "no")]}" keys="${["true", "false"]}" value="${taxSettings.is_price_with_tax}" toggle-target="price-with-tax"/>
                </div>
                <div class="form-row chosen-wrapper price-with-tax-true">
                    <label><g:message code="tax.default.code"/><span class="suggestion"></span></label>
                    %{--<ui:domainSelect name="tax.default_tax_code" class="medium tax-profile-selector" domain="${com.webcommander.webcommerce.TaxCode}" text="label" key="name" value="${taxSettings.default_tax_code}" />--}%
                    <g:select name="tax.default_tax_code" class="medium tax-profile-selector" from="${codes}" optionValue="label" optionKey="name" value="${taxSettings.default_tax_code}"/>
                </div>
                <div class="form-row chosen-wrapper price-with-tax-true">
                    <label><g:message code="base.price.rounding"/><span class="suggestion"></span></label>
                    <ui:namedSelect name="tax.base_price_rounding" class="medium" key="${NamedConstants.ROUNDING_TYPE}" value="${taxSettings.base_price_rounding}"/>
                </div>
            </div>

            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>