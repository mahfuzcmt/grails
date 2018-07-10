<div class="analytics-report">
    <div class="report-chart-header">
        <g:message code="visits"/>, <g:message code="social.media.traffic"/>
    </div>
    <table class="tabular-data">
        <tr>
            <td><g:message code="n.total.visits" args="${[socialData.visits]}"/></td>
            <td><g:message code="n.unique.visits" args="${[socialData.unique_visit]}"/></td>
        </tr>
        <tr>
            <td><g:message code="n.total.page.view" args="${[socialData.page_view]}"/></td>
            <td><g:message code="n.page.per.visit" args="${[socialData.page_per_visit.toDouble().toFixed(2)]}"/></td>
        </tr>
        <tr>
            <td><g:message code="n.avg.visit.duration" args="${[socialData.avg_visit_duration.toDouble().toFixed(2)]}"/></td>
            <td><g:message code="n.bounce.rate" args="${[socialData.bounce_rate.toDouble().toFixed(2)]}"/></td>
        </tr>
        <tr>
            <td><g:message code="n1(n2).new.visit" args="${[socialData.percentage_new_visitor.toDouble().toFixed(2), socialData.new_visitor]}"/></td>
            <td><g:message code="n1(n2).recurring.visit" args="${[socialData.percentage_recurring_visitor.toDouble().toFixed(2), socialData.recurring_visitor]}"/></td>
        </tr>
    </table>
</div>