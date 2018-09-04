<div class="analytics-report">
    <div class="report-chart-header">
    <g:message code="visits"/>, <g:message code="total.traffic"/>
    </div>
    <table class="tabular-data">
        <tr>
            <td><g:message code="n.total.visits" args="${[data.visits]}"/></td>
            <td><g:message code="n.unique.visits" args="${[data.unique_visit]}"/></td>
        </tr>
        <tr>
            <td><g:message code="n.total.page.view" args="${[data.page_view]}"/></td>
            <td><g:message code="n.page.per.visit" args="${[data.page_per_visit.toDouble().toFixed(2)]}"/></td>
        </tr>
        <tr>
            <td><g:message code="n.avg.visit.duration" args="${[data.avg_visit_duration.toDouble().toFixed(2)]}"/></td>
            <td><g:message code="n.bounce.rate" args="${[data.bounce_rate.toDouble().toFixed(2)]}"/></td>
        </tr>
        <tr>
            <td><g:message code="n1(n2).new.visit" args="${[data.percentage_new_visitor.toDouble().toFixed(2), data.new_visitor]}"/></td>
            <td><g:message code="n1(n2).recurring.visit" args="${[data.percentage_recurring_visitor.toDouble().toFixed(2), data.recurring_visitor]}"/></td>
        </tr>
    </table>
    <google:all_traffic_chart type="${params.chartType ?: 'line'}"/>
    <div class="report-chart-header">
        <g:message code="unique.visitor"/>
    </div>
    <google:unique_visitor_chart type="${params.chartType ?: 'line'}"/>
    <div class="report-chart-header">
        <g:message code="page.views"/>
    </div>
    <google:page_views_chart type="${params.chartType ?: 'line'}"/>
    <div class="report-chart-header">
        <g:message code="pages.per.visit"/>
    </div>
    <google:pages_per_visit_chart type="${params.chartType ?: 'line'}"/>
    <div class="report-chart-header">
        <g:message code="average.visit.duration"/>
    </div>
    <google:average_visit_duration_chart type="${params.chartType ?: 'line'}"/>
    <div class="report-chart-header">
        <g:message code="bounce.rate"/>
    </div>
    <google:bounce_rate_chart type="${params.chartType ?: 'line'}"/>
</div>