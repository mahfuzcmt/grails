<%@ page import="com.webcommander.plugin.ebay_listing.ebay_api.EbayApiService" %>
<form action="${app.relativeBaseUrl()}ebayListingAdmin/updateSettings" method="post">
    <input type="hidden" name="profileId" value="${profile.id}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="setting.info"/></h3>
            <div class="info-content"><g:message code="section.text.ebay.settings.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <label><g:message code="use.default.settings"/></label>
                <input type="checkbox" class="use-default-setting single" name="useDefaultSetting" uncheck-value="false" value="true" ${profile.useDefaultSetting ? "checked" : ""}
                       toggle-target="ebay-default-settings">
            </div>
            <div class="ebay-default-settings" do-reverse-toggle>
                <g:set var="setting" value="${profile.setting}" />
                <div class="double-input-row">
                    <div class="form-row">
                        <label><g:message code="mode"/><span class="suggestion"><g:message code="suggestion.ebay.mode"/></span></label>
                        <ui:namedSelect class="large" name="mode" value="${setting?.mode}"
                                        key="['sandbox': g.message(code: 'sandbox'), 'production': g.message(code: 'production')]"/>
                    </div><div class="form-row">
                        <label><g:message code="ebay.site"/><span class="suggestion"><g:message code="suggestion.ebay.site"/></span></label>
                        <ui:namedSelect class="large" name="ebaySite" value="${setting?.ebaySite}" key="${EbayApiService.getEbaySites().sort {it.value}}"/>
                    </div>
                </div>
                <div class="double-input-row">
                    <div class="form-row mandatory">
                        <label><g:message code="devid"/><span class="suggestion"><g:message code="suggestion.ebay.devid"/></span></label>
                        <input type="text" class="large" name="devId" value="${setting?.devId}" validation="skip@if{self::hidden} required">
                    </div><div class="form-row mandatory">
                        <label><g:message code="appid"/><span class="suggestion"><g:message code="suggestion.ebay.appid"/></span></label>
                        <input type="text" class="large" name="appId" value="${setting?.appId}" validation="skip@if{self::hidden} required">
                    </div>
                </div>
                <div class="form-row mandatory">
                    <label><g:message code="certid"/><span class="suggestion"><g:message code="suggestion.ebay.certid"/></span></label>
                    <input type="text" class="large" name="certId" value="${setting?.certId}" validation="skip@if{self::hidden} required">
                </div>
                <div class="form-row mandatory">
                    <label><g:message code="user.token"/></label>
                    <textarea class="large" name="userToken" validation="skip@if{self::hidden} required">${setting?.userToken}</textarea>
                </div>
            </div>
            <div class="form-row">
                <button type="submit" class="button submit-button"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>