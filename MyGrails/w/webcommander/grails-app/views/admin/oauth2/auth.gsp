<%--
  Created by IntelliJ IDEA.
  User: sajedur
  Date: 24-02-2015
  Time: 15:32
--%>
<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants; org.springframework.web.servlet.support.RequestContextUtils" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
        <title><g:message code="webcommander.oAuth.authentication.consent"/></title>
        <link rel="stylesheet" type="text/css" href="${app.systemResourceBaseUrl()}css/common/app-base.css">
        <link rel="stylesheet" type="text/css" href="${app.systemResourceBaseUrl()}css/admin/auth.css">
        <link rel="shortcut icon" href="${app.systemResourceBaseUrl()}images/favicon/control_panel.ico">
        <link rel="stylesheet" type="text/css" href="${app.systemResourceBaseUrl()}css/admin/theme-${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "theme")}.css">
    </head>
    <body>
        <form class="consent-form" action="${app.relativeBaseUrl()}oauth2/consent" method="post">
            <p><g:message code="asking.for.permission.access.account" args="${[client.displayName]}"/></p>
            <input type="hidden" name="${DomainConstants.OAUTH_CONSTANTS.CLIENT_ID}" value="${client.clientId}">
            <input type="hidden" name="${DomainConstants.OAUTH_CONSTANTS.REDIRECT_URI}" value="${client.redirectUrl}">
            <input type="hidden" name="${DomainConstants.OAUTH_CONSTANTS.RESOURCE_OWNER_TYPE}" value="${resourceOwnerType}">
            <input type="hidden" name="csrf_token" value="${session.csrf_token}">
            <div class="form-row">
                <input type="submit" name="allowed" value="<g:message code="allow"/>">
                <input type="submit" name="denied" value="<g:message code="deny"/>">
            </div>
        </form>
    </body>
</html>