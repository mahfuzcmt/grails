<div class="header">
    <span class="item-group title">
        <g:message code="report"/>
    </span>
    <div class="toolbar toolbar-right">
        <g:if test="${reportTypes}">
            <div class="tool-group tool-group-label"><g:message code="select.report"/></div>
            <g:select class="report-type" name="non-realtime-report-type" from="${reportTypes.collectEntries({[(it): g.message(code: it)]})}" optionKey="${{it.key}}" optionValue="${{it.value}}" value="${params.reportCode}"/>
        </g:if>
        <g:select class="tool-group date-filter" name="non-realtime-report-filter" from="${['today', 'yesterday', 'last.24.hours', 'last.seven.days', 'last.thirty.days', 'this.month', 'last.month', 'custom.date.range'].collectEntries({[(it): g.message(code: it)]})}" optionKey="${{it.key}}" optionValue="${{it.value}}" value="${params.duration}"/>
        <div class="datefield-between tool-group" ${params.duration == 'custom.date.range' ? '' : 'style="display: none"'}>
            <input type="text" class="datefield-from" value="${params.start}">
            <span class="date-field-separator">-</span>
            <input type="text" class="datefield-to" value="${params.end}">
            <div class="tool-group toolbar-btn date-range-apply save"><g:message code="submit"/></div>
        </div>
        <div class="tool-group">
            <span class="toolbar-item add-favourite" title="<g:message code="add.to.favourite"/>"><i></i></span>
            <span class="toolbar-item favourite" title="<g:message code="favourite"/>"><i></i></span>
        </div>
        <div class="tool-group">
            <span class="toolbar-item switch-menu collapsed"><i></i></span>
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="app-tab-content-container">
    <div class="inside-header">
        <span class="title">
            <g:message code="${title}"/>
        </span>
        <div class="toolbar toolbar-right">
            <span class="tool-group chart-option action-menu action-tool">
                <span class="tool-title"><g:message code="${"select.chart.option"}"/></span>
                <span class="action-dropper collapsed"></span>
            </span>
        </div>
    </div>
    <div class="report-contents">
        <div class="chart">
            ${chartRenderer()}
        </div>
        <div class="inside-header">
            <div class="toolbar toolbar-right">
                <span class="tool-group action-menu table-filter action-tool">
                    <span class="tool-title"><g:message code="filter"/></span>
                    <span class="action-dropper collapsed"></span>
                </span>
            </div>
        </div>
        <div class="tabular-data">
            <g:include controller="commanderReporting" action="${tabularRendererAction}"/>
        </div>
    </div>
</div>