<div class="analytics-report-keyword">
    <div class="report-chart-header">
        <g:message code="visits"/>, <g:message code="keywords"/>
    </div>
    <table class="tabular-data">
        <tr>
            <td><g:message code="n.total.visits" args="${[keywordData.visits]}"/></td>
            <td><g:message code="n.unique.visits" args="${[keywordData.unique_visit]}"/></td>
        </tr>
        <tr>
            <td><g:message code="n.total.page.view" args="${[keywordData.page_view]}"/></td>
            <td><g:message code="n.page.per.visit" args="${[keywordData.page_per_visit.toDouble().toFixed(2)]}"/></td>
        </tr>
        <tr>
            <td><g:message code="n.avg.visit.duration" args="${[keywordData.avg_visit_duration.toDouble().toFixed(2)]}"/></td>
            <td><g:message code="n.bounce.rate" args="${[keywordData.bounce_rate.toDouble().toFixed(2)]}"/></td>
        </tr>
        <tr>
            <td><g:message code="n1(n2).new.visit" args="${[keywordData.percentage_new_visitor.toDouble().toFixed(2), keywordData.new_visitor]}"/></td>
            <td><g:message code="n1(n2).recurring.visit" args="${[keywordData.percentage_recurring_visitor.toDouble().toFixed(2), keywordData.recurring_visitor]}"/></td>
        </tr>
    </table>
    <google:keyword_chart type="${params.chartType ?: 'line'}"/>
    <div class="toolbar-button-group tool-group">
        <span class="btn analytics-type all" value="all" disabled="disabled"><g:message code="all"/></span>
        <span class="btn analytics-type organic" value="organic"><g:message code="organic"/></span>
        <span class="btn analytics-type paid" value="paid"><g:message code="paid"/></span>
    </div>
    <g:include controller="googleAnalytics" action="loadDataForKeyword" />
    <div class="report-chart-header">
        <g:message code="landing.pages"/>, <g:message code="from.search.traffic"/>
    </div>
    <div class="toolbar-button-group tool-group">
        <span class="btn analytics-type-key all" value="all" disabled="disabled"><g:message code="all"/></span>
        <span class="btn analytics-type-key search-all" value="oraganic"><g:message code="organic"/></span>
        <span class="btn analytics-type-key search-organic" value="paid"><g:message code="paid"/></span>
    </div>
    <g:include controller="googleAnalytics" action="loadDataForLandingKeyword" />
</div>