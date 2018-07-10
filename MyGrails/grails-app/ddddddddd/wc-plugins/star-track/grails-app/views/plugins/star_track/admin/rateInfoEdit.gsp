<%@ page import="com.webcommander.plugin.star_track.constants.Constants" %>
<%--
  Created by IntelliJ IDEA.
  User: sajedur
  Date: 5/28/2015
  Time: 4:49 PM
--%>
<div class="ca-starTrack">
    <div class="form-row">
        <label><g:message code="service"/></label>
        <ui:namedSelect name="starTrackServiceCode" key="${Constants.SERVICES}" value="${condition?.apiServiceType}"/>
    </div>
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="include.fuel.surcharge"/></label>
            <input type="checkbox" name="includeFuelSurcharge" value="true"
                   class="single" ${extension.includeFuelSurcharge ? "checked" : ""}>
        </div><div class="form-row">
            <label><g:message code="include.security.surcharge"/></label>
            <input type="checkbox" name="includeSecuritySurcharge" value="true" class="single" ${extension.includeSecuritySurcharge ? "checked" : ""}>
        </div>
    </div>

    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="include.transit.warranty"/></label>
            <input type="checkbox" name="includeTransitWarranty" value="true" class="single" ${extension.includeTransitWarranty ? "checked" : ""} toggle-target="transit-value">
        </div><div class="form-row transit-value">
            <label><g:message code="include.security.surcharge"/></label>
            <input type="text" name="transitWarrantyValue" value="${extension.transitWarrantyValue}" validation="required@if{self::visible} number maxlength[5]" restrict="decimal" maxlength="5">
        </div>
    </div>
</div>
