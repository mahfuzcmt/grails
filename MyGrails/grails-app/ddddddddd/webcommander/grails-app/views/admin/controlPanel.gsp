<%@ page import="com.webcommander.constants.ResourceList; com.webcommander.manager.PathManager; com.webcommander.tenant.TenantContext; grails.util.Holders; com.webcommander.manager.LicenseManager; grails.converters.JSON; com.webcommander.constants.NamedConstants; com.webcommander.config.SiteConfig; com.webcommander.constants.DomainConstants; com.webcommander.models.DashletFlowLayout; com.webcommander.plugin.PluginManager; com.webcommander.util.AppUtil; org.springframework.web.servlet.support.RequestContextUtils; grails.util.Environment"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html charset=utf-8"/>
        <title><g:message code="webcommander.control.panel"/></title>
        %{--<g:if test="${!session.timezone || session.reset_timezone}">
            <script type="text/javascript">
                document.cookie = "timezone=" + new Date().getTimezoneOffset() + " expires=" + new Date(new Date().getTime() + 30 * 24 * 60 * 60 * 1000).toUTCString()
                location.href = '${request.forwardURI.encodeAsJavaScript()}'
            </script>
        </g:if>--}%
        <link rel="shortcut icon" href="${app.systemResourceBaseUrl()}images/favicon/control_panel.ico">
        <g:set var="wcAppVersion" value="${Holders.config.webcommander.version.number}"/>
        <!--region CORE CSS-->
        %{--<g:if test="${Environment.current == Environment.DEVELOPMENT}">--}%
            <g:each in="${ResourceList.fixedAdminCss}" var="css">
                <app:stylesheet href="$css"/>
            </g:each>
        %{--</g:if>
        <g:else>
            <app:stylesheet href="production-minified/admin-fixed.css"/>
        </g:else>--}%
        <g:set var="wcAppVersion" value="${Holders.config.webcommander.version.number}"/>
        <app:stylesheet href="css/admin/theme-${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "theme")}.css"/>
        <!--endregion-->

        <!--region PLUGIN CSS-->
        <plugin:adminCSSs/>
        <!--endregion-->

        <script type="text/javascript">
            <%
                Locale locale = RequestContextUtils.getLocale(request)
                String languageFile = locale.language
            %>

            var app = {
                tenancy: ${TenantContext.multiTenantEnabled ? '\'multi\'' : '\'single\''},
                isSoftInstallUninstallEnable: ${PluginManager.isSoftInstallUninstallEnable()},
                tenant: '${TenantContext.currentTenant.encodeAsJavaScript()}',
                baseUrl: '${app.relativeBaseUrl().encodeAsJavaScript()}',
                siteBaseUrl: '${app.siteBaseUrl().encodeAsJavaScript()}',
                fullURL: '${app.baseUrl().encodeAsJavaScript()}',
                admin_id: ${admin.id},
                login_email: '${admin.email}',
                is_front_end: false,
                systemResourceUrl: '${app.systemResourceBaseUrl().encodeAsJavaScript()}',
                customResourceUrl: '${app.customResourceBaseUrl().encodeAsJavaScript()}',
                pubUrl: '${appResource.getPubUrl().encodeAsJavaScript()}',
                systemPubUrl: '${appResource.getSystemPubUrl().encodeAsJavaScript()}',
                config: {},
                isProvisionActive: ${LicenseManager.isProvisionActive()},
                instanceIdentity: "${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.LICENSE, "licenseCode")}",
                maxPricePrecision: ${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "max_precision") ?: "6"},
                taxConfigType:  '${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.TAX, "configuration_type")}',
                ecommerce:  '${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce")}',
                routes: {
                    "([^\\\\/]+)[\\\\/]([^\\\\/]+)$": {
                        controller: "{1}",
                        action: "{2}"
                    }
                },
                controllers: {},
                languageFile: '${languageFile}'
            }
            <license:active>
                app.licenses = ${(LicenseManager.licenses as JSON).toString(false)}
            </license:active>
            <license:allowed id="${NamedConstants.LICENSE_KEYS.ACL}">
                <g:if test="${!session.super_vendor}">
                    app.permissions = ${permissions}
                </g:if>
            </license:allowed>
        </script>

        <!--region CORE JS-->
        %{--<g:if test="${Environment.current.equals(Environment.PRODUCTION)}">--}%
            %{--<app:javascript src="production-minified/admin-fixed.js"/>--}%
        %{--</g:if>--}%
        %{--<g:else>--}%
            <g:each in="${ResourceList.fixedAdminJs}" var="js">
                <app:javascript src="${js}"/>
                <g:if test="${js.contains("jquery.i18n.properties")}">
                    <script type="text/javascript">
                        $.i18n.properties({
                            name: 'messages',
                            path: app.systemResourceUrl + 'js/i18n/',
                            mode: 'map',
                            language: '<%=languageFile%>',
                            cache: true
                        })
                    </script>
                </g:if>
            </g:each>
        %{--</g:else>--}%
        <!--endregion-->

        <script type="text/javascript">
            %{--<g:if test="${Environment.current == Environment.PRODUCTION}">
                $.i18n.properties({
                    name: 'messages',
                    path: app.systemResourceUrl + 'production-minified/${TenantContext.currentTenant}/',
                    mode: 'map',
                    language: '<%=languageFile%>',
                    cache: true
                })
            </g:if>
            <g:else>--}%
            <plugin:each filter="${{plugin -> new File(PathManager.getSystemResourceRoot("plugins/$plugin.identifier/js/i18n")).exists()}}">
            $.i18n.properties({
                name: 'messages',
                path: app.systemResourceUrl + 'plugins/${plugin.identifier}/js/i18n/',
                mode: 'map',
                language: '<%=languageFile%>',
                cache: true
            })
            </plugin:each>
            %{--</g:else>--}%
        </script>

        <!--region PLUGIN JS-->
        %{--TODO: blocked as  generated file uploading to cloud process not decided--}%
        %{--<g:if test="${Environment.current.equals(Environment.PRODUCTION)}">
            <app:javascript src="production-minified/${TenantContext.currentTenant}/plugin-admin-fixed.js"/>
        </g:if>
        <g:else>--}%
            <app:allPluginFeatureJSs/>
            <app:allPluginEditorsJSs/>
            <app:allPluginWidgetJSs/>
        %{--</g:else>--}%
        <plugin:hookTag hookPoint="adminJss" attrs="${[:]}"/>
        <!--endregion-->

        <!--region TEST JS & CSS-->
        <app:javascript src="js/test/jasmine.js"/>
        <app:javascript src="js/test/jasmine-html.js"/>
        <app:javascript src="js/test/test.js"/>
        <app:allTestJS/>
        <app:stylesheet href="css/js-test/jasmine.css"/>
        <!--endregion-->
    </head>
    <body class="dashboard-env">
        <div class="top-component-bar" id="top-component-bar">
            <span class="commander-logo">
                <img src="${app.systemResourceBaseUrl()}images/admin/commander-logo2.png" class="login-logo2">
            </span>
            <div class="component-navigators">
                <span class="component dashboard" type="dashboard">
                    <span class="icon"></span>
                    <span class="title"><g:message code="dashboard"/></span>
                </span>
                <span class="component web-design" type="web_design">
                    <span class="icon"></span>
                    <span class="title"><g:message code="web.design"/></span>
                </span>
                <span class="component web-content" type="web_content">
                    <span class="icon"></span>
                    <span class="title"><g:message code="web.content"/></span>
                </span>
                <g:if test="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true'}" >
                    <span class="component web-commerce" type="web_commerce">
                        <span class="icon"></span>
                        <span class="title"><g:message code="web.commerce"/></span>
                    </span>
                </g:if>
                <span class="component web-marketing" type="web_marketing">
                    <span class="icon"></span>
                    <span class="title"><g:message code="web.marketing"/></span>
                </span>
                <span class="component administration" type="administration">
                    <span class="icon"></span>
                    <span class="title"><g:message code="administration"/></span>
                </span>
                <g:if test="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true'}" >
                    <span class="component report" type="report">
                        <span class="icon"></span>
                        <span class="title"><g:message code="report"/></span>
                    </span>
                </g:if>
            </div>
            <span class="administrator admin-menu action-tool">
                <span class="user-name">
                    ${admin.fullName.encodeAsBMHTML()}
                </span>
                <span class="action-dropper collapsed"></span>
            </span>
        </div>
        <div id="workspace">
            <div class="dashboard-container">
                <g:if test="${!get_started_wizard_passed}">
                    <div class="dashboard-toggle"><span class="title"><g:message code="dashboard"/></span></div>
                </g:if>
                <div class="dashlet-wrapper">
                    <g:set var="ecommerce" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce")}"/>
                    <g:if test="${ecommerce == 'true'}">
                        <g:include view="admin/dashboard/dashlet.gsp" model="${[dashlet: reportDashlet]}"/>
                    </g:if>
                    <g:if test="${!get_started_wizard_passed}">
                        <g:include view="admin/dashboard/gettingStarted/gettingStartedSetup.gsp"/>
                    </g:if>
                    <div class="dashlet-toggle-panel" ${!get_started_wizard_passed ? 'style="display: none"' : ''}>
                        <g:each in="${dashlets}" var="dashlet"><g:if test="${((DomainConstants.ECOMMERCE_DASHLET_CHECKLIST[dashlet.uniqueName] == true) && (ecommerce == 'true')) || (DomainConstants.ECOMMERCE_DASHLET_CHECKLIST[dashlet.uniqueName] == null)}"><g:include view="admin/dashboard/dashlet.gsp" model="${[dashlet: dashlet]}"/></g:if></g:each>
                    </div>
                </div>
            </div>
            <div class="content-tabs-container">
                <div id="tab-header-line">
                    <div class="left-scroller scroll-navigator" style="display: none"></div>
                    <div class="right-scroller scroll-navigator" style="display: none"></div>
                    <div class="tab-header-wrapper one-line-scroll-content"></div>
                </div>
            </div>
        </div>

        <div id="status-bar">
            <div class="status-block hotlink settings right-side">
                <span class="title"><g:message code="settings"/></span>
                <span class="icon"></span>
            </div>
            <div class="status-block version right-side">
                <span class="title"><span><g:message code="version"/>&nbsp</span><span>${wcAppVersion}</span></span>
            </div>
            <div class="status-block copyright right-side">
                <span class="title"><g:message code="copyright"/></span>
            </div>
        </div>
        <g:if test="${notifications}">
            <div id="administrative-notification-wrapper">
                <g:each in="${notifications}" var="notification">
                    <div class="administrative-notification notification-${notification.msgType}">
                        <div class="title"><g:message code="${notification.msgType}"/></div>
                        <div class="message"><license:message notification="${notification}"/></div>
                    </div>
                </g:each>
            </div>
        </g:if>
    </body>
</html>