<%@ page import="com.webcommander.webcommerce.Product; com.webcommander.constants.NamedConstants; com.webcommander.util.AppUtil" %>
<div class="header">
    <span class="item-group entity-count title">
        <g:message code="real.time"/>
    </span>
    <div class="toolbar toolbar-right">
        <span class="tool-group tool-group-label"><g:message code="time.frame"/></span>
        <select class="tool-group filter-report">
            <g:each in="${[1, 4, 8, 12, 24]}" var="hour">
                <option value="${hour}" ${hour.toString() == params.hour ? "selected" : ""}><g:message code="last.n.hour" args="${[hour]}"/></option>
            </g:each>
        </select>
        <span class="tool-group">
            <span class="toolbar-item switch-menu collapsed"><i></i></span>
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </span>
    </div>
</div>
<div class="app-tab-content-container realtime-report-block">
    <div class="quick-report-bar">
        <g:each in="${quicks}" var="component"><div class="quick-report-column ${component.class}">
            <div class="column-content">
                <span class="icon"></span>
                <span class="value">${component.isCurrency ? ("${AppUtil.baseCurrency.symbol}" + "${component.value.toAdminPrice()}") : component.value}</span>
                <span class="title"><g:message code="${component.title}"/></span>
            </div>
        </div></g:each>
    </div>
    <div class="multi-column two-column">
        <div class="columns first-column">
            <div class="column-content latest-report-block">
                <div class="report-title">
                    <g:message code="latest.products.sold"/>
                </div>
                <g:include view="admin/reporting/latestProductSold.gsp" model="${[latestSolds: latestSolds]}"/>
            </div>
        </div><div class="columns last-column">
            <div class="column-content">
                <div class="report-title">
                    <g:message code="latest.activities"/>
                </div>
                <g:include view="admin/reporting/latestActivity.gsp" model="${[latestActivities: latestActivities]}"/>
            </div>
        </div>
    </div>
</div>