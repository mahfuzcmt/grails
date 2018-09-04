<%@ page import="com.webcommander.admin.Customer; com.webcommander.constants.DomainConstants" %>
<%--
  Created by IntelliJ IDEA.
  User: sajedur
  Date: 5/28/2015
  Time: 3:46 PM
--%>
<form action="${app.relativeBaseUrl()}setting/saveConfigurations" method="POST" class="create-edit-form">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="pos"/></h3>
            <div class="info-content"><g:message code="section.text.pos.settings"/></div>
        </div>
        <g:set var="type" value="${DomainConstants.SITE_CONFIG_TYPES.POS}"/>
        <div class="form-section-container">
            <input type="hidden" name="type" value="${type}">
            <div class="form-row chosen-wrapper">
                <label><g:message code="default.customer"/></label>
                <ui:domainSelect domain="${Customer}" name="${type}.default_customer" text="fullName" value="${config.default_value}"/>
            </div>
            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>