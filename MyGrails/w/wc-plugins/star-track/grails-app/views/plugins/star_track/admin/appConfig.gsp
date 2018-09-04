<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.plugin.star_track.constants.Constants" %>
<%--
  Created by IntelliJ IDEA.
  User: sajedur
  Date: 5/28/2015
  Time: 3:46 PM
--%>
<form action="${app.relativeBaseUrl()}setting/saveConfigurations" method="POST" class="create-edit-form">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="star.track"/></h3>
            <div class="info-content"><g:message code="section.text.star.track.settings"/></div>
        </div>
        <g:set var="type" value="${DomainConstants.SITE_CONFIG_TYPES.STAR_TRACK}"/>
        <div class="form-section-container">
            <input type="hidden" name="type" value="${type}">
            <div class="double-input-row">
                <div class="form-row chosen-wrapper">
                    <label><g:message code="mode"/></label>
                    <ui:namedSelect key="${Constants.MODES}" name="${type}.mode" value="${config.mode}" />
                </div><div class="form-row">
                    <label><g:message code="source"/></label>
                    <input type="text" name="${type}.source" value="${config.source}" validation="required">
                </div>
            </div>
            <div class="double-input-row">
               <div class="form-row">
                    <label><g:message code="account.no"/></label>
                    <input type="text" name="${type}.account_no" value="${config.account_no}" validation="required">
                </div><div class="form-row">
                    <label><g:message code="user.access.key"/></label>
                    <input type="text" name="${type}.user_access_key" value="${config.user_access_key}" validation="required">
                </div>
            </div>
            <div class="double-input-row">
               <div class="form-row">
                    <label><g:message code="user.name"/></label>
                    <input type="text" name="${type}.user_name" value="${config.user_name}" validation="required">
                </div><div class="form-row">
                    <label><g:message code="password"/></label>
                    <input type="text" name="${type}.password" value="${config.password}" validation="required">
                </div>
            </div>
            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>