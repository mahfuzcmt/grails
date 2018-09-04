<%@ page import="com.webcommander.constants.NamedConstants; com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants" %>
<div class="header">
    %{--<div class="item-group">--}%
        %{--<h3><g:message code="available.plugin"/><span class="count">(${plugins.plugin.size() + plugins.installed.size() + (plugins.addon?.size() ?: 0)})</span></h3>--}%
        %{--<span class="sub-title"><g:message code="current.package"/> - ${licenseConfig.package_name}</span>--}%
    %{--</div>--}%
    <div class="toolbar toolbar-right">
        <form class="search-form tool-group">
            <input type="text" class="search-text" placeholder="<g:message code="search"/>">
        </form>
    </div>
</div>
<div class="content">
    <g:set var="ecommerce" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce")}"/>
    <div class="installed"></div>
    <g:each in="${plugins.installed}" var="plugin">
        <g:if test="${((plugin.pluginType == NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE) && (ecommerce == 'true')) || (plugin.pluginType == NamedConstants.WC_BEHAVIOUR_TYPE.CONTENT)}">
            <div class="plugin">
                <h3 class="name">${plugin.name}</h3>
                <div class="description">${plugin.description}</div>
                <button type="button" class="uninstall" plugin-id="${plugin.identifier}"><g:message code="uninstall"/></button>
            </div>
        </g:if>
    </g:each>
    <div class="available"></div>
    <g:each in="${plugins.plugin}" var="plugin">
        <g:if test="${((plugin.pluginType == NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE) && (ecommerce == 'true')) || (plugin.pluginType == NamedConstants.WC_BEHAVIOUR_TYPE.CONTENT)}">
            <div class="plugin">
                <h3 class="name">${plugin.name}</h3>
                <div class="description">${plugin.description}</div>
                <button type="button" class="install" plugin-id="${plugin.identifier}"><g:message code="install"/></button>
            </div>
        </g:if>
    </g:each>
    %{--<div class="addon"><span class="title"><g:message code="additional.plugins"/> </span></div>--}%
    %{--<g:each in="${plugins.addon}" var="plugin">--}%
        %{--<div class="plugin">--}%
            %{--<h3 class="name">${plugin.name}</h3>--}%
            %{--<div class="description">${plugin.description}</div>--}%
            %{--<button type="button" class="${plugin.is_installed ? "uninstall" : "install"}" plugin-id="${plugin.identifier}"><g:message code="${plugin.is_installed ? "uninstall" : "install"}"/></button>--}%
        %{--</div>--}%
    %{--</g:each>--}%
    %{--<div class="unauthorized"><span class="title"><g:message code="unauthorized"/> </span></div>--}%
    %{--<g:each in="${plugins.unauthorized}" var="plugin">--}%
        %{--<div class="plugin">--}%
            %{--<h3 class="name">${plugin.name}</h3>--}%
            %{--<div class="description">${plugin.description}</div>--}%
            %{--<button type="button" class="uninstall" plugin-id="${plugin.identifier}"><g:message code="uninstall"/></button>--}%
        %{--</div>--}%
    %{--</g:each>--}%
</div>
