<%@ page import="com.webcommander.plugin.loyalty_point.constants.NamedConstants; com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<form action="${app.relativeBaseUrl()}loyaltyPointAdmin/loadReportView" class="edit-popup-form create-edit-form">
    <div class="form-row">
        <label><g:message code="customer.name" args="${[(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customer":"Member"]}"/></label>
        <input type="text" class="large" name="name" value="${params.searchText}"/>
    </div>
    <div class="form-row">
        <g:set var="sourceList" value="${[all: "all.sources"] + NamedConstants.POINT_HISTORY_TYPE}"/>
        <g:set var="keyList" value="${[all: ""] + NamedConstants.POINT_HISTORY_TYPE}"/>
        <label><g:message code="source"/></label>
        <g:select class="large" name="source" from='${sourceList.values().collect {g.message(code: it)}}' keys="${keyList.values()}"/>
    </div>
    <div class="double-input-row">
        <div class="form-row chosen-wrapper">
            <label><g:message code="minimum.points"/></label>
            <input type="text" class="large" name="min_point" maxlength="9" validation="number maxlength[9]" restrict="numeric"/>
        </div><div class="form-row chosen-wrapper">
        <label><g:message code="maximum.points"/></label>
        <input type="text" class="large" name="max_point" maxlength="9" validation="number maxlength[9]" restrict="numeric"/>
    </div>
    </div>
    <div class="form-row datefield-between">
        <label><g:message code="earned.between"/></label>
        <input type="text" class="datefield-from smaller" name="earnedFrom"><span class="date-field-separator">-</span><input type="text" class="datefield-to smaller" name="earnedTo"/>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="search"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>