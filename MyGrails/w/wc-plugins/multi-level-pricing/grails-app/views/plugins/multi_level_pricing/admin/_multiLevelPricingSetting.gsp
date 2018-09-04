<%@ page import="com.webcommander.plugin.multi_level_pricing.constants.Constants" %>
<div class="section-separator"></div>
<div class="form-section">
    <input type="hidden" name="type" value="multi_level_pricing">
    <div class="form-section-info">
        <h3><g:message code="multi.level.pricing"/></h3>
        <div class="info-content"><g:message code="multi.level.pricing.message"/></div>
    </div>
    <div class="form-section-container">
        <div class="form-row">
            <input type="checkbox" class="medium single" name="multi_level_pricing.is_enabled" value="true" ${configs.is_enabled == "true" ? "checked" : ""} uncheck-value="false" toggle-target="use-lowest-highest">
            <span><g:message code="enable.multi.level.pricing"/></span>
        </div>
        <div class="form-row mandatory chosen-wrapper use-lowest-highest">
            <label><g:message code="if.a.customer.belongs.to.multiple.price.group.use.the.lowest.or.highest.price"/></label>
            <ui:namedSelect class="medium" key="${Constants.MULTIPLE_PRICE_OPTION_NAMES}" name="multi_level_pricing.lowest_or_highest_price" value="${configs.lowest_or_highest_price}"/>
        </div>
    </div>
</div>