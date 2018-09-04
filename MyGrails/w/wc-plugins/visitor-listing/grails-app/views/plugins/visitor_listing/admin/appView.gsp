<%@ page import="com.webcommander.admin.Customer; com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<%--
Created by IntelliJ IDEA.
User: sajedur
Date: 21/07/2014
Time: 12:04 PM
--%>
<div class="header">
    <span class="item-group entity-count title">
        <g:message code="visitors"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <label><g:message code="auto.refresh"/></label>
        <div class="tool-group">
            <g:select name="autoReload" from="${["10": "10s", "20" : "20s", "30": "30s", off: "Off"]}" optionKey="key" optionValue="value"/>
        </div>
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>

<div class="app-tab-content-container">
    <table class="content">
        <colgroup>
            <col style="width: 10%">
            <col style="width: 20%">
            <col style="width: 40%">
            <col style="width: 20%">
            <col style="width: 10%">
        </colgroup>
        <tr>
            <th><g:message code="ip.address"/></th>
            <th><g:message code="customer.name" args="${[(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customer":"Member"]}"/></th>
            <th><g:message code="last.accessed.url"/></th>
            <th><g:message code="start.time"/></th>
            <th><g:message code="idle.time"/></th>
        </tr>
        <g:if test="${visitors}">
            <g:each in="${visitors}" var="visitor">
                <tr>
                    <td>${visitor.ip_address}</td>
                    <g:set var="customer" value="${visitor.customer ? Customer.read(visitor.customer) : null}"/>
                    <td>${customer ? (customer.firstName + (customer.lastName ?: "")).encodeAsBMHTML() : "N/A"}</td>
                    <td>${visitor.last_accessed_url}</td>
                    <td>${new Date(visitor.creationTime).gmt().toAdminFormat(true, false, session.timezone)}</td>
                    <td><visitor:toDuration millis="${System.currentTimeMillis() - visitor.lastAccessedTime}"/></td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="5"><g:message code="no.visitor.found"/> </td>
            </tr>
        </g:else>
    </table>
</div>

<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>