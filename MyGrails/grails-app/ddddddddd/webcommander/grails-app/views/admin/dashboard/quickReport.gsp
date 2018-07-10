<%@ page import="com.webcommander.util.AppUtil" %>
<div class="dashlet-quick-report">
    <g:each in="${components}" var="component"><div class="quick-report-column ${component.class}">
            <div class="column-content">
                <span class="icon"></span>
                <span class="value">${component.isCurrency ? ("${AppUtil.baseCurrency.symbol}" + "${component.value.toFixed(0)}") : component.value}</span>
                <span class="title"><g:message code="${component.title}"/></span>
            </div>
        </div></g:each>
</div>
