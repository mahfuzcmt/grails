<div class="analytics-report-location">
    <div class="report-chart-header">
        <g:message code="visits"/>, <g:message code="location"/>
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
    <div class="toolbar-button-group tool-group">
        <span class="btn analytics-type all" value="all" disabled="disabled"><g:message code="all"/></span>
        <span class="btn analytics-type search-all" value="searchAll"><g:message code="search.all"/></span>
        <span class="btn analytics-type search-organic" value="searchOrganic"><g:message code="search.organic"/></span>
        <span class="btn analytics-type search-paid" value="searchPaid"><g:message code="search.paid"/></span>
        <span class="btn analytics-type direct" value="direct"><g:message code="direct"/></span>
        <span class="btn analytics-type referral" value="referral"><g:message code="referral"/></span>
        <span class="btn analytics-type social" value="social"><g:message code="social"/></span>
    </div>
    <g:include controller="googleAnalytics" action="loadDataForLocation" />
</div>