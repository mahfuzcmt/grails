<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants" %>
<form action="${app.relativeBaseUrl()}dashboard/saveLatestConfig" method="post" class="edit-popup-form two-column">
    <input type="hidden" name="idx" value="${params.idx}">
    <g:set var="ecommerce" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce")}"/>
    <g:radioGroup name="reportGroup" values="${dashletItemList.id}" labels="${dashletItemList.title}" value="${activeStat.id}">
        <div class="form-row">
            ${it.radio}<label class="value">${message(code: (ecommerce == 'false') ? it.label.replace("customer","member") : it.label)}</label>
        </div>
    </g:radioGroup>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="save"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>