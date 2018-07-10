<%@ page import="grails.util.Environment; com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants; org.springframework.web.servlet.support.RequestContextUtils" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title><g:message code="login.webcommander.control.panel"/></title>
    %{--<g:if test="${!session.timezone || session.reset_timezone}">
        <script type="text/javascript">
            document.cookie="timezone=" + new Date().getTimezoneOffset() + "; expires=" + new Date(new Date().getTime() + 30 * 24 * 60 * 60 * 1000).toUTCString()
            location.href = '${request.forwardURI.encodeAsJavaScript()}'
        </script>
    </g:if>--}%
    <link rel="shortcut icon" href="${app.systemResourceBaseUrl()}images/favicon/control_panel.ico">

    <!--region CORE CSS-->
    <app:stylesheet href="css/common/app-base.css"/>
    <app:stylesheet href="css/admin/login.css"/>
    <app:stylesheet href="css/admin/theme-${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "theme")}.css"/>
    <!--endregion-->

    <!--region CORE JS-->
    <app:javascript src="js/jquery/jquery.min.js"/>
    <app:javascript src="js/jquery/jquery.i18n.properties-min-1.0.9.js"/>
    <app:javascript src="js/jquery/jquery.hotkeys.js"/>
    <app:javascript src="js/jquery/jquery.autosize-min.js"/>
    <app:javascript src="js/utility/browser.js"/>
    <app:javascript src="js/utility/utility.js"/>
    <app:javascript src="js/utility/prototype.js"/>
    <app:javascript src="js/utility/date.js"/>
    <app:javascript src="js/utility/validation.js"/>
    <app:javascript src="js/utility/form.js"/>
    <app:javascript src="js/ui-widgets/custom-ui.js"/>
    <app:javascript src="js/admin/login.js"/>
    <!--endregion-->

    <script type="text/javascript">
        <%
            Locale locale = RequestContextUtils.getLocale(request);
            String languageFile = locale.language + "-" + locale.country;
            if(locale.variant) {
                languageFile += "-" + locale.variant;
            }
        %>
        $.i18n.properties({
            name: 'messages',
            path: '${app.systemResourceBaseUrl().encodeAsJavaScript()}' + 'js/i18n/',
            mode: 'map',
            language: '<%=languageFile%>',
            cache: true
        });
    </script>
</head>
<body>
    <div class="login-panel">
        <div class="login-panel-lt">
            <img src="${app.systemResourceBaseUrl()}images/admin/commander-logo.png" class="login-logo">
            <ul>
                <li><g:message code="powerful.ecommerce"/></li>
                <li><g:message code="feature.packed"/></li>
                <li><g:message code="easy.to.manage"/></li>
            </ul>
            <p><g:message code="take.command.of.your.business.online"/></p>
        </div>

        <div class="login-panel-rt">
            <g:if test="${type == "success"}">
                <div class="message-block success">${message.encodeAsBMHTML()}</div>
            </g:if>
            <g:elseif test="${error}">
                <div class="message-block error">${error.encodeAsBMHTML()}</div>
            </g:elseif>
            <g:layoutBody/>
        </div>

    </div>
</body>
</html>