<%@ page import="com.webcommander.config.StoreDetail" %>
<div class="referral">
    <div class="referral-invite-link-section">
        <div class="title">
            <g:message code="earn.loyalty.points.for.each.invite"/>
        </div>
        <div class="link">
            <input type="text" class="invite-link" disabled="disabled" value="${app.baseUrl() + 'customer/register?referralCode=' + customer.referralCode}">
            <span class="button referral-copy-button"><g:message code="copy.link"/></span>
        </div>
        <p class="referral-code"><g:message code="referral.code"/>: <strong>${customer.referralCode}</strong></p>
    </div>

    <div class="social-media-wrapper invite-link-social-media">
        <span class="social-media-share facebook" type="facebook" url="${app.baseUrl() + 'customer/register?referralCode=' + customer.referralCode}"></span>
        <span class="social-media-share twitter" type="twitter" url="${app.baseUrl() + 'customer/register?referralCode=' + customer.referralCode}"></span>
        <span class="social-media-share google-plus" type="google-plus" url="${app.baseUrl() + 'customer/register?referralCode=' + customer.referralCode}"></span>
        <span class="social-media-share linkedin" type="linkedin" url="${app.baseUrl() + 'customer/register?referralCode=' + customer.referralCode}"></span>
        <span class="social-media-share email" type="email" url="${app.baseUrl() + 'customer/register?referralCode=' + customer.referralCode}"></span>
    </div>

    <div class="info-section-info-block">
        <div class="info-box">
            <div class="image share"></div>
            <span class="info-title">
                <g:message code="you.share"/>
            </span>
            <div class="info-text">
                <g:message code="you.share.text"/> ${StoreDetail.first().name}
            </div>
        </div>
        <div class="info-box">
            <div class="image sign-up"></div>
            <span class="info-title">
                <g:message code="friends.signup"/>
            </span>
            <div class="info-text">
                <g:message code="friends.signup.text"/>
            </div>
        </div>
        <div class="info-box">
            <div class="image purchase"></div>
            <span class="info-title">
                <g:message code="friends.purchase"/>
            </span>
            <div class="info-text">
                <g:message code="friends.purchase.text"/>
            </div>
        </div>
        <div class="info-box">
            <div class="image earn-credit"></div>
            <span class="info-title">
                <g:message code="you.earn.credit"/>
            </span>
            <div class="info-text">
                <g:message code="you.earn.credit.text"/>
            </div>
        </div>
    </div>
</div>
