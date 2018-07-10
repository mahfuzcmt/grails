<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants" %>
<g:set var="customerProfile" value="${DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_PROFILE_PAGE}"></g:set>
<g:set var="configs" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_PROFILE_PAGE)}"></g:set>
<div class="form-section">
    <div class="form-section-info">
        <h3><g:message code="welcome.message"/></h3>
        <div class="info-content"><g:message code="welcome.message.details"/></div>
    </div>
    <div class="form-section-container">
        <div class="form-row">
            <textarea class="wceditor" name="${customerProfile}.page_html">${configs.page_html}</textarea>
        </div>
    </div>
</div>