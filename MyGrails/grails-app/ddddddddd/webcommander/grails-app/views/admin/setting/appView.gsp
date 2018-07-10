<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil; com.webcommander.manager.LicenseManager; com.webcommander.admin.ConfigService" %>
<div class="header multi-tab-shared-header">
    <span class="header-title"></span>
    <div class="toolbar toolbar-right">
        <span class="tool-group toolbar-btn save save-all"><g:message code="save.close"/></span>
        <span class="tool-group toolbar-btn cancel"><g:message code="cancel"/></span>
    </div>
</div>
<div class="app-tab-content-container bmui-tab left-side-header" ${params.active ? "active='" + params.active + "'" : ""}>
    <g:set var="tabs" value="${ConfigService.tabs}"/>
    <div class="bmui-tab-header-container">
        <div class="toolbar">
            <form class="search-form tool-group">
                <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
            </form>
        </div>
        <g:each in="${tabs}" var="tab">
            <license:allowed id="${tab.value.license}">
                <g:set var="ecommerce" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce")}"/>
                <g:if test="${((ecommerce == 'true') && (tab.value.ecommerce == true)) || (tab.value.ecommerce == null)}">
                    <div class="bmui-tab-header" data-tabify-tab-id="${tab.key}" data-tabify-url="${app.relativeBaseUrl() + tab.value.url}">
                        <span class="title"><g:message code="${tab.value.message_key}" args="${[(ecommerce == 'true')?"Customer":"Member"]}"/></span>
                    </div>
                </g:if>
            </license:allowed>
        </g:each>
    </div>
    <div class="bmui-tab-body-container">
        <g:each in="${tabs}" var="tab">
            <license:allowed id="${tab.value.license}">
                <div id="${"bmui-tab-" + tab.key}"></div>
            </license:allowed>
        </g:each>
    </div>
</div>