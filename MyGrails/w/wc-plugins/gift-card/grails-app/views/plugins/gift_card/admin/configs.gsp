<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.StringUtil; com.webcommander.webcommerce.TaxProfile;" %>
<%@ page import="com.webcommander.plugin.gift_card.constants.DomainConstants as DC; com.webcommander.plugin.gift_card.constants.NamedConstants as NC;" %>
<form action="${app.relativeBaseUrl()}setting/saveConfigurations" method="post" class="create-edit-form">
    <input type="hidden" name="type" value="${type}"/>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="gift.card"/></h3>
            <div class="info-content"><g:message code="section.text.gift.card.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row">
                    <input type="checkbox" class="single" value="true" name="${type}.is_enabled"  uncheck-value="false" ${configs.is_enabled == "true" ? "checked" : ""}>
                    <span><g:message code="enable"/></span>
                </div>
            </div>

            <div class="form-row">
                <label></label>
                <input type="checkbox" class="single" name="${type}.is_expiry_threshold_enabled" toggle-target="expiry-threshold" uncheck-value="0" value="1" ${configs.is_expiry_threshold_enabled == '1' ? 'checked="checked"': ''}>
                <span><g:message code="set.expiry.threshold"/></span>
            </div>
            <div class="double-input-row mandatory expiry-threshold expiry-threshold-1">
                <div class="form-row">
                    <label><g:message code="will.expire.after"/></label>
                    <input type="text" class="small" name="${type}.expiry_threshold" value="${configs.expiry_threshold}" restrict="numeric" maxlength="6" validation="skip@if{self::hidden} required range[1,9999999]">
                </div><div class="form-row chosen-wrapper">
                    <label>&nbsp;</label>
                    <ui:namedSelect class="tiny" key="${NC.GIFT_CARD_EXPIRY_THRESHOLD_UNITS}" name="${type}.expiry_threshold_unit" value="${configs.expiry_threshold_unit}"/>
                </div>
            </div>
            <div class="form-row">
                <label></label>
                <input type="checkbox" class="single" uncheck-value="0" value="1" name="${DomainConstants.SITE_CONFIG_TYPES.GIFT_CARD}.is_send_post_enabled" ${configs.is_send_post_enabled == "1" ? "checked" : ""}>
                <span><g:message code="enable.send.in.post"/></span>
            </div>
            <div class="form-row mandatory">
                <label><g:message code="gift.card.code.prefix"/><span class="suggestion">e.g. GCRD-</span></label>
                <input type="text" class="single" name="${DomainConstants.SITE_CONFIG_TYPES.GIFT_CARD}.gc_code_prefix" validation="required rangelength[2,50] match[(^([a-zA-Z0-9]]*-[a-zA-Z0-9]]*){1}$),(999)] " value="${configs.gc_code_prefix}">
            </div>
            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>