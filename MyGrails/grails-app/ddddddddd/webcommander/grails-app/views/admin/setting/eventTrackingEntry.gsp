<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants" %>
<div class="event-${toggle}">
    <g:set var="tempType" value="${DomainConstants.SITE_CONFIG_TYPES[key]}"/>
    <input type="hidden" name="type" value="${tempType}">
    <g:each status="i" in="${AppUtil.getConfig(tempType)}" var="map">
        <div class="form-row">
            <input type="checkbox" class="single" name="${tempType}.${map.key}" value="on" ${map.value == "on" ? "checked='checked'" : ""} uncheck-value="off">
            <span><g:message code="${map.key?.replaceAll("_", ".")}"/></span>
        </div>
    </g:each>
</div>