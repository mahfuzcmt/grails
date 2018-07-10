%{--todo: if not need diferent referral tab delete this file also delete commented this functionality from loyalty plugin --}%
<%@ page import="com.webcommander.plugin.loyalty_point.constants.DomainConstants" %>
<form class="referral-config-form create-edit-form" id="referralConfigForm"  action="${app.relativeBaseUrl()}loyaltyPointAdmin/saveConfigs">
    <input type="hidden" name="type" value="referral">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="referral.config"/></h3>
            <div class="info-content"><g:message code="section.text.referral.config.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <input type="checkbox" class="single" name="referral.enable_send_email_signup_link" uncheck-value="false" value="true" ${configs.enable_send_email_signup_link == "true" ? "checked='checked'" : ""}>
                <label><g:message code="send.with.email.members.signup.link"/>. (<span class="small"><g:message code="when.customer.register.send.signup.link.text"/></span>)</label>
            </div>

            <div class="form-row">
                <input type="checkbox" class="single" value="true" uncheck-value="false" name="referral.enable_ask_customer_where_he_hear" ${configs.enable_ask_customer_where_he_hear == "true" ? "checked='checked'" : ""}>
                <label><g:message code="ask.your.customer.at.registration.where.did.you.here.about.us"/></label>
            </div>

            <div class="form-row policy-specified_conversion_rate">
                <label><g:message code="referral.user.will.get.loyalty.point.on.his.first"/></label>
                <input type="text" validation="skip@if{self::hidden} required number gt[0] maxlength[9]" maxlength="9" class="small" name="referral.number_of_time_loyalty_point_get" value="${configs.number_of_time_loyalty_point_get}" restrict="decimal" >
                <label><g:message code="successful.purchase"/></label>
            </div>

            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>