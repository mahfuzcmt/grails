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
    <div class="report-chart-header">
        <g:message code="visits"/>, <g:message code="direct.traffic"/>
    </div>
    <table class="tabular-data">
        <tr>
            <td><g:message code="n.total.visits" args="${[directData.visits]}"/></td>
            <td><g:message code="n.unique.visits" args="${[directData.unique_visit]}"/></td>
        </tr>
        <tr>
            <td><g:message code="n.total.page.view" args="${[directData.page_view]}"/></td>
            <td><g:message code="n.page.per.visit" args="${[directData.page_per_visit.toDouble().toFixed(2)]}"/></td>
        </tr>
        <tr>
            <td><g:message code="n.avg.visit.duration" args="${[directData.avg_visit_duration.toDouble().toFixed(2)]}"/></td>
            <td><g:message code="n.bounce.rate" args="${[directData.bounce_rate.toDouble().toFixed(2)]}"/></td>
        </tr>
        <tr>
            <td><g:message code="n1(n2).new.visit" args="${[directData.percentage_new_visitor.toDouble().toFixed(2), directData.new_visitor]}"/></td>
            <td><g:message code="n1(n2).recurring.visit" args="${[directData.percentage_recurring_visitor.toDouble().toFixed(2), directData.recurring_visitor]}"/></td>
        </tr>
    </table>
    <google:direct_traffic_chart type="${params.chartType ?: 'line'}"/>
    <div class="report-chart-header">
        <g:message code="visits"/>, <g:message code="referral.traffic"/>
    </div>
    <table class="tabular-data">
        <tr>
            <td><g:message code="n.total.visits" args="${[referralData.visits]}"/></td>
            <td><g:message code="n.unique.visits" args="${[referralData.unique_visit]}"/></td>
        </tr>
        <tr>
            <td><g:message code="n.total.page.view" args="${[referralData.page_view]}"/></td>
            <td><g:message code="n.page.per.visit" args="${[referralData.page_per_visit.toDouble().toFixed(2)]}"/></td>
        </tr>
        <tr>
            <td><g:message code="n.avg.visit.duration" args="${[referralData.avg_visit_duration.toDouble().toFixed(2)]}"/></td>
            <td><g:message code="n.bounce.rate" args="${[referralData.bounce_rate.toDouble().toFixed(2)]}"/></td>
        </tr>
        <tr>
            <td><g:message code="n1(n2).new.visit" args="${[referralData.percentage_new_visitor.toDouble().toFixed(2), referralData.new_visitor]}"/></td>
            <td><g:message code="n1(n2).recurring.visit" args="${[referralData.percentage_recurring_visitor.toDouble().toFixed(2), referralData.recurring_visitor]}"/></td>
        </tr>
    </table>
    <google:referral_traffic_chart type="${params.chartType ?: 'line'}"/>
    <div class="report-chart-header">
        <g:message code="visits"/>, <g:message code="organic.search.traffic"/>
    </div>
    <table class="tabular-data">
        <tr>
            <td><g:message code="n.total.visits" args="${[organicData.visits]}"/></td>
            <td><g:message code="n.unique.visits" args="${[organicData.unique_visit]}"/></td>
        </tr>
        <tr>
            <td><g:message code="n.total.page.view" args="${[organicData.page_view]}"/></td>
            <td><g:message code="n.page.per.visit" args="${[organicData.page_per_visit.toDouble().toFixed(2)]}"/></td>
        </tr>
        <tr>
            <td><g:message code="n.avg.visit.duration" args="${[organicData.avg_visit_duration.toDouble().toFixed(2)]}"/></td>
            <td><g:message code="n.bounce.rate" args="${[organicData.bounce_rate.toDouble().toFixed(2)]}"/></td>
        </tr>
        <tr>
            <td><g:message code="n1(n2).new.visit" args="${[organicData.percentage_new_visitor.toDouble().toFixed(2), organicData.new_visitor]}"/></td>
            <td><g:message code="n1(n2).recurring.visit" args="${[organicData.percentage_recurring_visitor.toDouble().toFixed(2), organicData.recurring_visitor]}"/></td>
        </tr>
    </table>
    <google:organic_traffic_chart type="${params.chartType ?: 'line'}"/>
    <div class="report-chart-header">
        <g:message code="visits"/>, <g:message code="paid.traffic"/>
    </div>
    <table class="tabular-data">
        <tr>
            <td><g:message code="n.total.visits" args="${[paidData.visits]}"/></td>
            <td><g:message code="n.unique.visits" args="${[paidData.unique_visit]}"/></td>
        </tr>
        <tr>
            <td><g:message code="n.total.page.view" args="${[paidData.page_view]}"/></td>
            <td><g:message code="n.page.per.visit" args="${[paidData.page_per_visit.toDouble().toFixed(2)]}"/></td>
        </tr>
        <tr>
            <td><g:message code="n.avg.visit.duration" args="${[paidData.avg_visit_duration.toDouble().toFixed(2)]}"/></td>
            <td><g:message code="n.bounce.rate" args="${[paidData.bounce_rate.toDouble().toFixed(2)]}"/></td>
        </tr>
        <tr>
            <td><g:message code="n1(n2).new.visit" args="${[paidData.percentage_new_visitor.toDouble().toFixed(2), paidData.new_visitor]}"/></td>
            <td><g:message code="n1(n2).recurring.visit" args="${[paidData.percentage_recurring_visitor.toDouble().toFixed(2), paidData.recurring_visitor]}"/></td>
        </tr>
    </table>
    <google:paid_traffic_chart type="${params.chartType ?: 'line'}"/>
</div>