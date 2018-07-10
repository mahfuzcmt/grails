<%@ page import="com.webcommander.plugin.ebay_listing.constants.DomainConstants; com.webcommander.plugin.ebay_listing.constants.NamedConstants; com.webcommander.plugin.ebay_listing.ebay_api.EbayApiService" %>
<form action="${app.relativeBaseUrl()}ebayListingAdmin/saveSettings" method="post" class="create-edit-form">
    <input type="hidden" name="type" value="${type}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="ebay.listing.profile"/></h3>
            <div class="info-content"><g:message code="section.text.setting.ebay.listing"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row mandatory-chosen-wrapper">
                    <label><g:message code="mode"/><span class="suggestion"><g:message code="suggestion.setting.ebay.listing.mode"/></span></label>
                    <ui:namedSelect class="large" name="${type}.mode" value="${configs.mode}"
                                    key="['sandbox': g.message(code: 'sandbox'), 'production': g.message(code: 'production')]"/>
                </div><div class="form-row mandatory-chosen-wrapper">
                    <label><g:message code="ebay.site"/><span class="suggestion"><g:message code="suggestion.setting.ebay.listing.ebay.site"/></span></label>
                    <ui:namedSelect class="large" name="${type}.ebay_site" value="${configs.ebay_site}" key="${EbayApiService.getEbaySites().sort {it.value}}"/>
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="devid"/><span class="suggestion"><g:message code="suggestion.setting.ebay.listing.dev.id"/></span></label>
                    <input type="text" class="large" name="${type}.devid" value="${configs.devid}" validation="required">
                </div><div class="form-row mandatory">
                    <label><g:message code="appid"/><span class="suggestion"><g:message code="suggestion.setting.ebay.listing.appid"/></span></label>
                    <input type="text" class="large" name="${type}.appid" value="${configs.appid}" validation="required">
                </div>
            </div>
            <div class="form-row mandatory">
                <label><g:message code="certid"/><span class="suggestion"><g:message code="suggestion.setting.ebay.listing.certid"/></span></label>
                <input type="text" class="large" name="${type}.certid" value="${configs.certid}" validation="required">
            </div>
            <div class="form-row mandatory">
                <label><g:message code="user.token"/><span class="suggestion"><g:message code="suggestion.setting.ebay.listing.user.token"/></span></label>
                <textarea class="large" name="${type}.user_token" validation="required">${configs.user_token}</textarea>
            </div>
        </div>
    </div>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="schedule.info"/></h3>
            <div class="info-content"><g:message code="section.text.ebay.schedule.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row ">
                <label></label>
                <input type="checkbox" class="single" name="enableScheduleListing" uncheck-value="false" value="true" ${schedule.enableScheduleListing ? 'checked' : ''} toggle-target="schedule">
                <span><g:message code="enable.schedule.listing"/></span>
            </div>
            <div class="schedule">
                <div class="form-row">
                    <label><g:message code="schedule.by"/></label>
                    <ui:namedSelect name="scheduleBy" key="${NamedConstants.SCHEDULE_BY}" value="${schedule.scheduleBy}" toggle-target="schedule-by"/>
                </div>
                <div class="double-input-row schedule-by-month">
                    <div class="form-row ">
                        <label><g:message code="months"/></label>
                        <ui:namedSelect class="large" name="months" values="${schedule.months.sort()}" multiple="multiple" key="${NamedConstants.MONTHS}"/>
                    </div><div class="form-row schedule-by-month">
                        <label><g:message code="dates"/></label>
                        <g:select class="large" name="dates" value="${schedule.dates.sort()}" multiple="multiple" from="${1..31}"/>
                    </div>
                </div>
                <div class="form-row schedule-by-week">
                    <label><g:message code="days"/></label>
                    <ui:namedSelect class="large" name="days" values="${schedule.days.sort()}" multiple="multiple" key="${NamedConstants.DAYS}"/>
                </div>
                <div class="double-input-row">
                    <div class="form-row">
                        <label><g:message code="hours"/></label>
                        <g:select class="large" name="hours" value="${schedule.hours.sort()}" multiple="multiple" from="${0..23}"/>
                    </div><div class="form-row">
                        <label><g:message code="minutes"/></label>
                        <g:select class="large" name="minutes" value="${schedule.minutes.sort()}" multiple="multiple" from="${(0..55).findAll { it % 5 == 0 }}"/>
                    </div>
                </div>

            </div>
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>