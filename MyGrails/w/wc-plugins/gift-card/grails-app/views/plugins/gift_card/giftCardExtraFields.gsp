<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants; com.webcommander.webcommerce.Product" %>
<g:set var="isSendPostEnabled" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GIFT_CARD, "is_send_post_enabled")}"/>
<div class="gift-card-fields">
    <g:if test="${isSendPostEnabled == "1"}">
        <div class="form-header">
            <span class="title"><g:message code="how.would.you.like.to.send.your.gift.card"/></span>
            <div class="form-row">
                <input type="radio" name="gift_card.sendingType" value="email" toggle-target="sending-type-by-email" ${cardData?.sendingType == 'post' ? '' : 'checked'}>
                <label><g:message code="by.email"/> </label>
            </div>
            <div class="form-row">
                <input type="radio" name="gift_card.sendingType" value="post" toggle-target="sending-type-by-post" ${cardData?.sendingType == 'post' ? 'checked' : ''}>
                <label><g:message code="by.post"/> </label>
            </div>
        </div>
    </g:if>
    <g:else>
        <input type="hidden" name="gift_card.sendingType" value="email">
    </g:else>
    <div class="form-content">
        <div class="double-input-row">
            <div class="form-row mandatory">
                <label><g:message code="recipient.first.name"/><span class="suggestion">e.g. Alex</span></label>
                <input type="text" class="medium" name="gift_card.firstName" value="${cardData?.firstName}" validation="skip@if{self::hidden} required rangelength[1,100]" maxlength="100" placeholder="<g:message code="recipient.first.name"/>">
            </div><div class="form-row">
            <label><g:message code="recipient.last.name"/><span class="suggestion">e.g. Smith</span></label>
            <input type="text" class="medium" name="gift_card.lastName" value="${cardData?.lastName}" placeholder="<g:message code="recipient.last.name"/>">
        </div>
        </div>
        <div class="double-input-row">
            <div class="form-row mandatory">
                <label><g:message code="recipient.email"/><span class="suggestion">e.g. alex@gmail.com</span></label>
                <input type="text" class="medium" name="gift_card.email" value="${cardData?.email}" validation="skip@if{self::hidden} required email" placeholder="<g:message code="recipient.email"/>">
            </div><div class="form-row">
            <label><g:message code="sender.name"/><span class="suggestion">e.g. Alex Smith</span></label>
            <input type="text" class="medium" name="gift_card.senderName" value="${cardData?.senderName}" placeholder="<g:message code="sender.name"/>">
        </div>
        </div>
        <g:if test="${isSendPostEnabled == "1"}">
            <div class="sending-type-by-post">
                <div class="double-input-row">
                    <div class="form-row">
                        <label><g:message code="phone"/><span class="suggestion">e.g. +61 3 9999 9999</span></label>
                        <input type="text" class="medium" name="gift_card.phone" value="${cardData?.phone}" placeholder="<g:message code="phone"/>">
                    </div><div class="form-row">
                    <label><g:message code="mobile"/><span class="suggestion">e.g. +61 400 000 000</span></label>
                    <input type="text" class="medium" name="gift_card.mobile" value="${cardData?.mobile}" placeholder="<g:message code="mobile"/>">
                </div>
                </div>
                <div class="form-row mandatory">
                    <label><g:message code="recipient.address.line"/><span class="suggestion">e.g. 123 Melbourne St.</span></label>
                    <input type="text" class="medium" name="gift_card.address" value="${cardData?.address}" validation="skip@if{self::hidden} required" placeholder="<g:message code="recipient.address.line"/>">
                </div>
                <div class="form-row mandatory city-selector-row">
                    <label><g:message code="city"/><span class="suggestion">e.g. Richmond</span></label>
                    <g:include controller="app" action="loadCities" params="${[validation: "skip@if{self::hidden} required", fieldName: "gift_card.city", state: cardData?.stateId, postCode: cardData?.postCode, city: cardData?.city]}"/>
                </div>
                <div class="form-row mandatory">
                    <label><g:message code="post.code"/><span class="suggestion">e.g. 3000</span></label>
                    <input type="text" class="medium" name="gift_card.postCode" value="${cardData?.postCode}" validation="skip@if{self::hidden} required" placeholder="<g:message code="post.code"/>">
                </div>
                <div class="form-row country-row">
                    <label><g:message code="country"/><span class="suggestion">e.g. Australia</span></label>
                    <ui:countryList name="gift_card.countryId" id="countryId" validation="skip@if{self::hidden} required" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "default_country").toLong()}"/>
                </div>
                <g:include view="/admin/customer/stateFormFieldView.gsp" params="[stateName: 'gift_card.stateId']"/>
            </div>
        </g:if>

        <div class="form-row">
            <label><g:message code="message"/></label>
            <textarea type="text" class="medium" name="gift_card.message" placeholder="<g:message code="message"/>">${cardData?.message}</textarea>
        </div>
    </div>
</div>