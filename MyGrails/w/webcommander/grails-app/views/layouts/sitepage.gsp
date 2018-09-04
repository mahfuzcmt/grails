<%@ page import="com.webcommander.constants.ResourceList; grails.util.Environment; com.webcommander.manager.PathManager; com.webcommander.tenant.TenantContext; com.webcommander.constants.NamedConstants; com.webcommander.manager.LicenseManager; grails.converters.JSON; com.webcommander.plugin.PluginManager; grails.util.Holders; com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil; org.springframework.web.servlet.support.RequestContextUtils" %>
<!DOCTYPE HTML>
<html>
<head>
    <%
        if (request.js_cache == null) {
            request.js_cache = []
        }
        if (request.css_cache == null) {
            request.css_cache = []
        }
        if (request.js_cache_flag == null) {
            request.js_cache_flag = [:]
            request.css_cache_flag = [:]
        }
    %>
    <g:set var="wcAppVersion" value="${Holders.config.webcommander.version.number}"/>
    <g:set var="page" value="${request.page}"/>
    <g:set var="templateUUID" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "template_uuid")}"/>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <g:set var="isResponsive" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.RESPONSIVE, "is_responsive") == "true"}"/>
    <g:if test="${isResponsive}">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
    </g:if>
    <title>${page.title.encodeAsBMHTML()}</title>
    <g:if test="${!editMode && !params.viewMode}">
        <g:each in="${page.metaTags}" var="meta">
            <meta name="${meta.name.encodeAsBMHTML()}" content="${meta.value.encodeAsBMHTML()}"/>
        </g:each>
        %{--<g:if test="${!session.timezone || session.reset_timezone}">
            <script type="text/javascript">
                document.cookie = "timezone=" + new Date().getTimezoneOffset() + "; expires=" + new Date(new Date().getTime() + 30 * 24 * 60 * 60 * 1000).toUTCString()
                location.href = '${app.currentURL().encodeAsJavaScript()}' + (window.location.hash ? "#" + window.location.hash : "")
            </script>
        </g:if>--}%
    </g:if>
    <g:if test="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, 'favicon_enabled') == 'true'}">
        <link rel="shortcut icon" href="${appResource.getFaviconURL(isEnable: true)}">
    </g:if>

    <!--region CORE CSS-->
    %{--<g:if test="${Environment.current == Environment.DEVELOPMENT}">--}%
        <g:each in="${ResourceList.fixedSiteCss}" var="css">
            <app:stylesheet href="${css}"/>
        </g:each>
    %{--</g:if>
    <g:else>
        <app:stylesheet href="production-minified/site-fixed.css"/>
    </g:else>--}%
    <!--endregion-->

    <g:if test="${editMode}">
        <app:editorStyleSheets/>
    </g:if>
    <g:if test="${isResponsive}">
        <app:stylesheet href="css/site/site-responsive-base.css"/>
    </g:if>
    <g:else>
        <app:stylesheet href="css/site/site-non-responsive-base.css"/>
    </g:else>
    <app:embeddableCss/>

    <!--region PLUGIN CSS-->
    <plugin:siteCSSs/>
    <!--endregion-->

    <app:activeTemplateCSSs/>
    <g:if test="${!editMode}">
        <g:if test="${page.layout ? page.layout.id : page.id}">
            <link rel="stylesheet" type="text/css" href="${app.relativeBaseUrl()}pagecss/${page.id ? 'pageAll' : 'layoutAll'}/${page.id ?: page.layout.id}/style.css?id=${templateUUID}${cssFromContent ? '&autocontent=' + cssFromContent : ''}">
        </g:if>
    </g:if>
    <g:else>
        <app:embedCss layoutId="${page.layout?.id}" pageId="${page.id}"/>
    </g:else>
    <script type="application/javascript">
        var app = {
            tenant: '${TenantContext.currentTenant.encodeAsJavaScript()}',
            baseUrl: '${app.relativeBaseUrl().encodeAsJavaScript()}',
            systemResourceUrl: '${app.systemResourceBaseUrl().encodeAsJavaScript()}',
            is_front_end: true,
            routes: {
                "([^\\\\/]+)[\\\\/]([^\\\\/]+)$": {
                    controller: "{1}",
                    action: "{2}"
                }
            },
            controllers: {}
        }
    </script>

    <!--region CORE JS-->
    %{--<g:if test="${Environment.current.equals(Environment.PRODUCTION)}">--}%
        %{--<app:javascript src="production-minified/site-fixed-core.js"/>--}%
    %{--</g:if>--}%
    %{--<g:else>--}%
        <g:each in="${ResourceList.fixedSiteCoreJs}" var="js">
            <app:javascript src="${js}"/>
        </g:each>
    %{--</g:else>--}%

    <script type="text/javascript">
        bm.show_response_status = false;
        $.form_no_textarea_autosize = true;
        $.scrollbar_horizontal_without_bar_step = "auto";
        <license:active>
        app.licenses = ${(LicenseManager.licenses as JSON).toString(false)}
            </license:active>
            $.extend($.paginator_config, {
                next: '${g.message(code: "front.end.next.page").encodeAsJavaScript()}',
                prev: '${g.message(code: "front.end.previous.page").encodeAsJavaScript()}',
                first: '${g.message(code: "front.end.first.page").encodeAsJavaScript()}',
                last: '${g.message(code: "front.end.last.page").encodeAsJavaScript()}'
            })
    </script>
    <appResource:frontEndConfigJS/>
    %{--<g:if test="${Environment.current.equals(Environment.PRODUCTION)}">--}%
        %{--<app:javascript src="production-minified/site-fixed-ui.js"/>--}%
    %{--</g:if>--}%
    %{--<g:else>--}%
        <g:each in="${ResourceList.fixedSiteUIJs}" var="js">
            <app:javascript src="${js}"/>
        </g:each>
    %{--</g:else>--}%
    <!--endregion-->

    <!--region PLUGIN JS-->
    <plugin:siteJSs/>
    <!--endregion-->
    <g:if test="${!editMode && !params.viewMode}">
        <app:autoPageJS/>
        <app:autoPageCSS/>
        <g:if test="${page.id ? true : page.layout.id}">
            <script type="text/javascript" src="${app.relativeBaseUrl()}pagejs/${page.id ? 'page' : 'layout'}/${page.id ?: page.layout.id}/page.js?id=${templateUUID}${jsFromContent ? '&autocontent=' + jsFromContent : ''}"></script>
        </g:if>
        <g:set var="tracking_enabled" value="${!adminView && AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.WEBTOOL, 'tracking_enabled').toBoolean()}"/>
        <g:set var="tracking_id" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.WEBTOOL, 'tracking_id')}"/>
        <g:if test="${tracking_enabled}">
            <script type="text/javascript">
                window.GoogleAnalyticsObject = "ga";
                window.ga = function () {
                    (ga.q = ga.q || []).push(arguments)
                };
                ga.l = 1 * new Date();
                ga('create', '${tracking_id}', 'auto');
                <g:if test="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.WEBTOOL, 'page_tracking').toBoolean() && !page.disableGooglePageTracking && !request.disableGooglePageTracking}">
                (window._ga || (window._ga = {})).beforePageSend = function () {
                    var ind = ga.q.find("this[0] == 'send' && this[1] == 'pageview'")
                    ga.q.splice(ind, 0, arguments)
                };
                ga('send', 'pageview');
                </g:if>
                <%
                        def gEventTrackConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GOOGLE_EVENT_TRACKING);
                        Map googleEventConfig = (gEventTrackConfig?.tracking == "on") ? [
                            events: gEventTrackConfig,
                            labels: NamedConstants.EVENT_TRACKING_TYPE.collectEntries {[(it.key): g.message(code: it.value)]}
                        ] : [:];
                    %>
                var eTrack = app.event_tracking = ${googleEventConfig as JSON};
                if (eTrack && eTrack.events) {
                    var events = eTrack.events;
                    var labels = eTrack.labels;
                    var category = [];
                    $.each(events, function (k, v) {
                        if (k.startsWith("event_")) {
                            category.push(k);
                        }
                    });
                    $(document).ready(function () {
                        $.each(category, function (i, value) {
                            var last = value.substr(value.lastIndexOf("_") + 1);
                            attachEventTracking(last);
                        })
                    });

                    function attachEventTracking(key) {
                        var clazz = "";
                        var event = [];
                        $.each(events, function (k, v) {
                            if (k.startsWith(key + "_") && v == "on") {
                                event.push(k)
                            }
                        });
                        $.each(event, function (i, v) {
                            clazz += (i ? ", " : "") + ".et_" + v;
                        });
                        $(document.body).on("click", clazz, function (evt) {
                            var target = $(evt.target);
                            ga('send', 'event', target.attr("et-category") || target.prop("tagName").toLowerCase(), 'click', labels[key], 1);
                        });
                    }
                }
            </script>
        </g:if>
    </g:if>
    <g:if test="${editMode}">
        <app:editorJSs/>
        <script type="text/javascript">
            function pageLoaded() {
                var $ = window.parent.$;
                <g:if test="${params.editorFrameId}">
                $("#${params.editorFrameId}").trigger("sure-load")
                </g:if>
            }
        </script>
    </g:if>
    <g:if test="${request.js}">
        <script type="text/javascript">
            ${request.js}
        </script>
    </g:if>
    <g:set var="headerCode" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.FRONTEND_PAGES, "header_code")}"/>
    <g:if test="${headerCode && !editMode}">${headerCode}</g:if>
    <plugin:hookTag hookPoint="layoutHead" attrs="${[:]}"/>
</head>
<g:set var="templateContainerClass" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "template_container_class")}"/>
<body id="webcommander-page" class="${isResponsive ? 'responsive' : 'non-responsive'} ${request.isAutoPage ? "auto-page page-" + page.name.replace('.', '-') : (page.id ? ('page-' + page.url) : '')} ${templateContainerClass}${editMode && isFrontEndEditor ? ' fee' : ''}" onload="${editMode ? 'pageLoaded()' : ''}">
<g:set var="beginningCode" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.FRONTEND_PAGES, "body_beginning_code")}"/>
<g:if test="${beginningCode && !editMode}">
    <div class="custom-code beginning" section="custom-code">${beginningCode}</div>
</g:if>
<g:if test="${editMode && page.layout?.id}">
    <input type="hidden" class="layout-id" value="${page.layout.id}"/>
    <input type="hidden" class="layout-name" value="${page.layout.name}"/>
</g:if>
<g:if test="${editMode}">
    <input type="hidden" class="page-id" value="${page.id}"/>
</g:if>
<div class="header" section="header">
    <div class="widget-container">
        <render:renderPageContent value="${page.layout ? page.layout.headerWidgets : page.headerWidgets}"/>
    </div>
</div>
<div class="body" section="body">
    <g:if test="${error}">
        <div class="server-message server-error error-message message-block">${error.encodeAsBMHTML()}</div>
    </g:if>
    <g:if test="${message}">
        <div class="server-message server-info info-message message-block">${message.encodeAsBMHTML()}</div>
    </g:if>
    <render:renderPageContent value="${page.layout ? page.layout.body : page.body}"/>
</div>
<div class="footer" section="footer">
    <div class="widget-container">
        <render:renderPageContent value="${page.layout ? page.layout.footerWidgets : page.footerWidgets}"/>
    </div>
</div>
<g:if test="${!editMode || (page.id == null && !request.isAutoPage)}">
    <g:each in="${page.layout?.dockableSections}" var="section">
        <div class="dockable" section="${section.uuid}" id="dock-${section.uuid}" dock-id="${section.id}">
            <div class="widget-container">
                <render:renderPageContent value="${section.widgets}"/>
            </div>
            <g:if test="${editMode}">
                <div class='dock-mask'></div>
            </g:if>
        </div>
    </g:each>
</g:if>
<g:each in="${page.dockableSections}" var="section">
    <div class="dockable" section="${section.uuid}" id="dock-${section.uuid}" dock-id="${section.id}">
        <div class="widget-container">
            <render:renderPageContent value="${section.widgets}"/>
        </div>
        <g:if test="${editMode}">
            <div class='dock-mask'></div>
        </g:if>
    </div>
</g:each>
<plugin:hookTag hookPoint="sitePageBodyBottom"/>
<g:each in="${request.js_cache}" var="js">
    <g:if test="${js.startsWith('resources')}">
        <script type="text/javascript" src="${js.startsWith("/") || js.startsWith("http") ? js : app.relativeBaseUrl() + js}" async defer></script>
    </g:if>
    <g:else>
        <script type="text/javascript" src="${js.startsWith("/") || js.startsWith("http") ? js : app.systemResourceBaseUrl() + js}" async defer></script>
    </g:else>
</g:each>
<g:each in="${request.css_cache}" var="css">
    <g:if test="${css.startsWith('resources')}">
        <link rel="stylesheet" type="text/css" href="${css.startsWith("/") || css.startsWith("http") ? css : app.relativeBaseUrl() + css}">
    </g:if>
    <g:else>
        <link rel="stylesheet" type="text/css" href="${css.startsWith("/") || css.startsWith("http") ? css : app.systemResourceBaseUrl() + css}">
    </g:else>
</g:each>
<g:if test="${tracking_enabled}">
    <script type="text/javascript" src="//www.google-analytics.com/analytics.js" async defer></script>
</g:if>
<script type="text/javascript" async defer>
    <%
        Locale locale = RequestContextUtils.getLocale(request)
        String languageFile = locale.language
        Integer pluginCount = PluginManager.activePlugins.size()
    %>
    function allPropLoaded() {
        window.$_i18n_properties_loaded = true
    }
    $(function () {
        $.i18n.properties({
            name: 'messages',
            path: app.systemResourceUrl + 'js/i18n/',
            mode: 'map',
            language: '<%=languageFile%>',
            cache: true
            <g:if test="${!pluginCount}">
                , callback: allPropLoaded
            </g:if>
        })
        %{--TODO: blocked as  generated file uploading to cloud process not decided--}%
        %{--<g:if test="${Environment.current == Environment.DEVELOPMENT}">--}%
            <plugin:each filter="${{plugin -> new File(PathManager.getSystemResourceRoot() + "plugins/$plugin.identifier/js/i18n").exists()}}" status="i">
                $.i18n.properties({
                    name: 'messages',
                    path: app.systemResourceUrl + 'plugins/${plugin.identifier}/js/i18n/',
                    mode: 'map',
                    language: '<%=languageFile%>',
                    cache: true
                    <g:if test="${i + 1 == count}">
                        , callback: allPropLoaded
                    </g:if>
                });
            </plugin:each>
        %{--</g:if>
        <g:else>
            $.i18n.properties({
                name: 'messages',
                path: app.systemResourceUrl + 'production-minified/${TenantContext.currentTenant}/',
                mode: 'map',
                language: '<%=languageFile%>',
                cache: true,
                callback: allPropLoaded
            });
        </g:else>--}%
    });
</script>
<g:set var="endingCode" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.FRONTEND_PAGES, "body_ending_code")}"/>
<g:if test="${endingCode && !editMode}">
    <div class="custom-code ending" section="custom-code">${endingCode}</div>
</g:if>
<g:if test="${editMode && isFrontEndEditor && !params.mediaPreview}">
    <g:include view="layouts/frontEnd/_editorSettings.gsp"/>
</g:if>
</body>
</html>
