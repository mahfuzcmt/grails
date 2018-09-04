<%@ page import="com.webcommander.constants.NamedConstants; com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants" %>
<% int countUsed = widgets.size()%>
<g:set var="ecommerce" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce")}"/>
<div class="alert message">
    <span class="error">Following widgets and plugins should be remove first then you can switch to E-Commerce to content mode</span>
</div>
<div class="ecommerce-popup-table" >
    <div class="header">
        <span class="title">Plugins</span>
    </div>
    <table class="content plugin-table">
        <colgroup>
            <col class="name-column">
        </colgroup>
        <tr>
            <th><g:message code="name"/></th>
        </tr>
        <g:each in="${plugins}" var="plugin">
            <g:if test="${((plugin.pluginType == NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE) && (ecommerce == 'true'))}">
                <%countUsed++%>
                <tr>
                    <td><g:message code="${plugin.name}"/></td>
                </tr>
            </g:if>
        </g:each>
    </table>
    <div class="header">
        <span class="title">Widgets</span>
    </div>
    <table class="content widget-table">
        <colgroup>
            <col class="name-column">
            <col class="used-column">
        </colgroup>
        <tr>
            <th><g:message code="name"/></th>
            <th><g:message code="used"/></th>
        </tr>
        <g:each in="${widgets}" var="widget">
            <tr>
                <td><g:message code="${widget.name}"/></td>
                <td>${widget.used}</td>
            </tr>
        </g:each>
    </table>
</div >
<div class="button-line">
    <button type="submit" class="submit-button ecommerce-popup-confirm" ${countUsed ? "disabled=\"disabled\"" : ""}>Confirm</button>
    <button type="button" class="cancel-button">Cancel</button>
</div>