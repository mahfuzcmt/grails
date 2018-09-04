<%@ page import="com.webcommander.webcommerce.ShippingClass; com.webcommander.webcommerce.TaxProfile; com.webcommander.webcommerce.ShippingProfile;com.webcommander.util.AppUtil;com.webcommander.constants.*" %>
<form action="${app.relativeBaseUrl()}setting/saveConfigurations" method="post" onsubmit="return false" class="create-edit-form">
    <input type="hidden" name="type" value="shipping">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="shipping.info"/></h3>
            <div class="info-content"><g:message code="section.text.settings.shipping"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row chosen-wrapper">
                    <label><g:message code="default.shipping.profile"/><span class="suggestion"><g:message code="suggestion.setting.shipping.default.profile"/></span></label>
                    <ui:domainSelect name="shipping.default_shipping_profile" class="medium shipping-profile-selector" domain="${ShippingProfile}"
                                     prepend="${['': g.message(code: "no.shipping")]}" value="${shippingSettings.default_shipping_profile.toLong(0)}"/>
                </div>
                <div class="form-row chosen-wrapper">
                    <label><g:message code="shipping.tax.profile"/><span class="suggestion"><g:message code="suggestion.setting.shipping.tax.profile"/></span></label>
                    <g:select name="shipping.shipping_tax_profile" class="medium tax-profile-selector" from="${taxProfiles}" noSelection="['': g.message(code: 'no.tax')]" optionValue="name" optionKey="id" value="${shippingSettings.shipping_tax_profile.toLong(0)}"/>
                </div>
            </div>
            <plugin:hookTag hookPoint="shippingSetting" attrs="${[shippingSettings: shippingSettings]}"/>
        </div>
    </div>

    <div class="section-separator"></div>

    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="shipping.class"/></h3>
            <div class="info-content"><g:message code="section.text.settings.shipping"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <input class="single" type="checkbox" name="shipping.enable_shipping_class" value="true" uncheck-value="false" ${shippingSettings.enable_shipping_class == 'true' ? 'checked':''} toggle-target="shipping-class">
                <span><g:message code="class"/></span>
            </div>
            <div class="shipping-class">
                <g:include view="admin/setting/shippingClassList.gsp"/>
            </div>
            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>