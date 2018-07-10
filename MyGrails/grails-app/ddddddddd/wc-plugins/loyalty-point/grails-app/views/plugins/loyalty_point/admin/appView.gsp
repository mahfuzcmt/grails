<%@ page import="com.webcommander.plugin.loyalty_point.SpecialPointRule; com.webcommander.plugin.loyalty_point.constants.DomainConstants" %>
<form class="loyalty-points-config-form create-edit-form" id="loyaltyPointsConfigForm"  action="${app.relativeBaseUrl()}loyaltyPointAdmin/saveConfigs">
    <input type="hidden" name="type" value="loyalty-point">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="loyalty.point.config"/></h3>
            <div class="info-content"><g:message code="section.text.loyalty.point.config.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="configuration-panel nested-panel">
                <div class="form-row">
                    <input type="checkbox" class="single" name="loyalty-point.is_enabled" uncheck-value="false" value="true" ${configs.is_enabled == "true" ? "checked='checked'" : ""} toggle-target="loyalty-point-settings">
                    <span><g:message code="enable.loyalty.point"/></span>
                </div>
                <div class="loyalty-point-settings nested-content">
                    <div class="form-row">
                        <input type="checkbox" name="loyalty-point.apply_by_default" uncheck-value="false" value="true" ${configs.apply_by_default == "true" ? "checked='checked'" : ""}>
                        <span><g:message code="use.by.default"/></span>
                    </div>
                    <div class="earning-panel nested-panel">
                        <div class="form-row">
                            <input type="checkbox" name="loyalty-point.earning_enabled" uncheck-value="false" value="true" ${configs.earning_enabled == "true" ? "checked='checked'" : ""} toggle-target="earning-enabled-target">
                            <span><g:message code="enable.earning"/></span>
                        </div>
                        <div class="form-row earning-enabled-target nested-content">
                            <input type="checkbox" name="loyalty-point.show_in_cart" uncheck-value="false" value="true" ${configs.show_in_cart == "true" ? "checked='checked'" : ""}>
                            <span><g:message code="show.loyalty.point.in.cart"/></span>
                        </div>
                    </div>
                    <div class="redeem_panel">
                        <plugin:hookTag hookPoint="loyaltyPointConvertSettings">
                            <div class="form-row">
                                <input type="checkbox" value="true" uncheck-value="false" name="loyalty-point.enable_store_credit" ${configs.enable_store_credit == "true" ? "checked='checked'" : ""}>
                                <span><g:message code="redeem.store.credit"/></span>
                            </div>
                            <div class="form-row redeem_store_credit">
                                <span><g:message code="conversion.rate"/></span>
                                <input type="text" class="small" name="loyalty-point.conversion_rate_store_credit" value="${configs.conversion_rate_store_credit}" maxlength="12" validation="required@if{self::visible} number maxlength[12] maxprecision[9,2]" restrict="decimal">
                                <span class="note"><g:message code="for.each.n.loyalty.points" args="[100]"/><span class="suggestion"><g:message code="suggestion.conversion.rate"/></span></span>
                            </div>
                        </plugin:hookTag>
                    </div>
                    <div class="point-expire-panel nested-panel">
                        <div class="form-row chosen-wrapper">
                            <input type="checkbox" name="loyalty-point.enable_expire" uncheck-value="false" value="true" ${configs.enable_expire == "true" ? "checked='checked'" : ""} toggle-target="expire">
                            <span><g:message code="enable.point.expiry"/></span>
                        </div>
                        <div class="form-row expire-after_period mandatory expire nested-content">
                            <span><g:message code="expire.in"/></span>
                            <input type="text" class="small" maxlength="9" validation="number required@if{self::visible} gt[0] maxlength[9]" name="loyalty-point.expire_in_value" value="${configs.expire_in_value}" restrict="numeric">
                            <loyaltyPoint:namedSelection class="small" name="loyalty-point.expire_in_offset" target="${DomainConstants.EXPIRE_IN_OFFSET}" value="${configs.expire_in_offset}"/>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="section-separator"></div>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="basic.point.earn.policy"/></h3>
            <div class="info-content"><g:message code=""/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row chosen-wrapper">
                <label><g:message code="point.earn.policy"/><span class="suggestion"><g:message code="suggestion.point.earn.policy"/></span></label>
                <loyaltyPoint:namedSelection class="large" name="loyalty-point.point_policy" target="${DomainConstants.POINT_POLICY}" value="${configs.point_policy}" toggle-target="policy"/>
            </div>
            <div class="form-row policy-specified_conversion_rate">
                <label><g:message code="conversion.rate.earning"/></label>
                <input type="text" validation="skip@if{self::hidden} required number gt[0] maxprecision[9,2]" maxlength="12" class="large" name="loyalty-point.conversion_rate_earning" value="${configs.conversion_rate_earning}" restrict="decimal" >
            </div>
        </div>
    </div>
    <div class="section-separator"></div>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="other.point.earning.policy"/></h3>
            <div class="info-content"><g:message code=""/></div>
        </div>
        <div class="form-section-container">
            <div class="purchase-panel">
                <div class="double-input-row nested-panel">
                    <div class="form-row chosen-wrapper">
                        <input type="checkbox" class="single" name="loyalty-point.enable_purchase_point" uncheck-value="false" value="true" ${configs.enable_purchase_point == "true" ? "checked='checked'" : ""} toggle-target="on-purchase">
                        <span><g:message code="on.purchase"/></span>
                    </div>
                    <div class="form-row on-purchase nested-content">
                        <input type="text" class="small" maxlength="9" validation="number required@if{self::visible}" name="loyalty-point.on_purchase_amount" value="${configs.on_purchase_amount}" restrict="numeric">
                    </div>
                </div>
                <div class="form-row on-purchase nested-content">
                    <input type="radio" name="loyalty-point.purchase_valid_for" value="every" ${configs["purchase_valid_for"] == "every" ? "checked" : ""}>
                    <span><g:message code="every.time"/></span>
                    <input type="radio" name="loyalty-point.purchase_valid_for" value="first" ${configs["purchase_valid_for"] == "first" ? "checked" : ""}>
                    <span><g:message code="first"/></span>
                    <input type="text" class="small" maxlength="9" validation="number required@if{self::visible}" name="loyalty-point.on_first_purchase_amount" value="${configs.on_first_purchase_amount}" restrict="numeric">
                    <span><g:message code="time"/></span>
                </div>
            </div>
            <div class="review-panel double-input-row nested-panel">
                <div class="form-row">
                    <input type="checkbox" class="single" name="loyalty-point.enable_product_review_point" uncheck-value="false" value="true" ${configs.enable_product_review_point == "true" ? "checked='checked'" : ""} toggle-target="on-product-review">
                    <span><g:message code="product.review"/></span>
                </div>
                <div class="form-row on-product-review nested-content">
                    <input type="text" class="small" maxlength="9" validation="number required@if{self::visible}" name="loyalty-point.on_product_review_amount" value="${configs.on_product_review_amount}" restrict="numeric">
                </div>
            </div>
            <div class="signup-panel double-input-row nested-panel">
                <div class="form-row">
                    <input type="checkbox" class="single" name="loyalty-point.enable_signup_registration_point" uncheck-value="false" value="true" ${configs.enable_signup_registration_point == "true" ? "checked='checked'" : ""} toggle-target="on-signup-registration">
                    <span><g:message code="signup.registration"/></span>
                </div>
                <div class="form-row on-signup-registration nested-content">
                    <input type="text" class="small" maxlength="9" validation="number required@if{self::visible}" name="loyalty-point.on_signup_registration_amount" value="${configs.on_signup_registration_amount}" restrict="numeric">
                </div>
            </div>

            <div class="referral-panel nested-panel">
                <div class="form-row">
                    <input type="checkbox" class="single" value="true" uncheck-value="false" name="loyalty-point.enable_referral" ${configs.enable_referral == "true" ? "checked='checked'" : ""} toggle-target="referral">
                    <span><g:message code="referral"/></span>
                </div>
                <div class="referral nested-content">
                    <div class="nested-panel">
                        <div class="form-row">
                            <input type="checkbox" value="true" uncheck-value="false" name="loyalty-point.enable_refer_product" ${configs.enable_refer_product == "true" ? "checked='checked'" : ""} toggle-target="refer_product">
                            <span><g:message code="refer.a.product"/></span>
                        </div>
                        <div class="refer_product nested-content">
                            <div class="form-row">
                                <label><g:message code="on.purchase.referrer.loyalty.point"/></label>
                                <input type="text" class="small" name="loyalty-point.refer_product_on_purchase_referrer_loyalty_point" value="${configs.refer_product_on_purchase_referrer_loyalty_point}" maxlength="16" validation="required@if{self::visible} number maxlength[16] maxprecision[9,2]" restrict="decimal">
                            </div>
                            <div class="form-row">
                                <label><g:message code="on.purchase.referree.loyalty.point"/></label>
                                <input type="text" class="small" name="loyalty-point.refer_product_on_purchase_referree_loyalty_point" value="${configs.refer_product_on_purchase_referree_loyalty_point}" maxlength="16" validation="required@if{self::visible} number maxlength[16] maxprecision[9,2]" restrict="decimal">
                            </div>
                        </div>
                    </div>
                    <div class="nested-panel">
                        <div class="form-row">
                            <input type="checkbox" value="true" uncheck-value="false" name="loyalty-point.enable_refer_customer" ${configs.enable_refer_customer == "true" ? "checked='checked'" : ""}  toggle-target="refer_customer">
                            <span><g:message code="refer.a.new.customer"/></span>
                        </div>
                        <div class="refer_customer nested-content">
                            <div class="form-row">
                                <label><g:message code="on.signup.referrer.loyalty.point"/></label>
                                <input type="text" class="small" name="loyalty-point.refer_customer_on_signup_referrer_loyalty_point" value="${configs.refer_customer_on_signup_referrer_loyalty_point}" maxlength="16" validation="required@if{self::visible} number maxlength[16] maxprecision[9,2]" restrict="decimal">
                            </div>
                            <div class="form-row">
                                <label><g:message code="on.signup.referree.loyalty.point"/></label>
                                <input type="text" class="small" name="loyalty-point.refer_customer_on_signup_referree_loyalty_point" value="${configs.refer_customer_on_signup_referree_loyalty_point}" maxlength="16" validation="required@if{self::visible} number maxlength[16] maxprecision[9,2]" restrict="decimal">
                            </div>
                        </div>
                    </div>
                    <div class="nested-panel">
                        <div class="form-row">
                            <input type="checkbox" name="loyalty-point.enable_send_referral_code_with_mail" uncheck-value="false" value="true" ${configs.enable_send_referral_code_with_mail == "true" ? "checked='checked'" : ""}>
                            <span><g:message code="send.referral.code.with.mail"/></span>
                            <span class="suggestion"><g:message code="when.customer.register.send.referral.code.text"/></span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="section-separator"></div>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="social.media.share"/></h3>
            <div class="info-content"><g:message code=""/></div>
        </div>
        <div class="form-section-container">
            <div class="show-config-for-share-loyalty-point">
                <table class="content">
                    <colgroup>
                        <col style="width: 10%">
                        <col style="width: 55%">
                        <col style="width: 35%">
                    </colgroup>
                    <g:each in="${[[name:'facebook'], [name: 'twitter'], [name: 'googleplus'], [name: 'linkedin']]}" var="profile" status="i">
                        <tr>
                            <td><img src="${app.systemResourceBaseUrl()}images/social-media-icons/${profile.name}.png"></td>
                            <td><g:message code="share.on" args="${[profile.name.capitalize()]}"/></td>
                            <td><input type="text" class="smaller" validation="number required@if{self::visible} maxlength[9]" maxlength="9" name="loyalty-point.on_${profile.name}_share_amount" value="${configs."on_${profile.name}_share_amount" ?: 0}" restrict="numeric"></td>
                        </tr>
                    </g:each>
                </table>
            </div>
        </div>
    </div>
    <div class="section-separator"></div>

    <div class="form-section loyalty-point-rules">
        <div class="form-section-info">
            <h3><g:message code="special.point.rule"/></h3>
            <div class="info-content"><g:message code=""/></div>
        </div>
        <div class="form-section-container">
            <div class="preview-table">
                <loyaltyPoint:tablePreview data="${[rules: SpecialPointRule.list()]}"/>
            </div>
            <span class="add-rule link-btn"><g:message code="add.rule"/></span>
        </div>
    </div>

    <div class="form-row">
        <button type="submit" class="submit-button"><g:message code="update"/></button>
    </div>
</form>