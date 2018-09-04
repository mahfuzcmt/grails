<div class="analytics-report">
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
        <g:message code="sources"/>, <g:message code="from.referral.traffic"/>
    </div>
    <table class="tabular-data">
        <thead>
            <tr>
                <th><g:message code="sources"/></th>
                <th><g:message code="visits"/></th>
                <th><g:message code="unique.visitor"/></th>
                <th><g:message code="page.view"/></th>
                <th><g:message code="pages.per.visit"/></th>
                <th><g:message code="average.visit.duration"/></th>
                <th><g:message code="bounce.rate"/></th>
                <th><g:message code="new.visitor"/></th>
                <th><g:message code="recurring.visitor" /></th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${dataForSourceList}" var="data">
                <tr>
                    <td>${data.source.encodeAsBMHTML()}</td>
                    <td>${data.visits.encodeAsBMHTML()}</td>
                    <td>${data.unique_visit.encodeAsBMHTML()}</td>
                    <td>${data.page_view.encodeAsBMHTML()}</td>
                    <td>${data.page_per_visit.toDouble().toFixed(2).encodeAsBMHTML()}</td>
                    <td>${data.avg_visit_duration.toDouble().toFixed(2).encodeAsBMHTML()}</td>
                    <td>${data.bounce_rate.toDouble().toFixed(2).encodeAsBMHTML()}</td>
                    <td>${data.new_visitor.encodeAsBMHTML()}</td>
                    <td>${(data.visits-data.new_visitor).encodeAsBMHTML()}</td>
                </tr>
            </g:each>
        </tbody>
    </table>
    <div class="report-chart-header">
        <g:message code="landing.pages"/>, <g:message code="from.referral.traffic"/>
    </div>
    <table class="tabular-data">
        <thead>
            <tr>
                <th><g:message code="landing.pages"/></th>
                <th><g:message code="visits"/></th>
                <th><g:message code="unique.visitor"/></th>
                <th><g:message code="page.view"/></th>
                <th><g:message code="pages.per.visit"/></th>
                <th><g:message code="average.visit.duration"/></th>
                <th><g:message code="bounce.rate"/></th>
                <th><g:message code="new.visitor"/></th>
                <th><g:message code="recurring.visitor" /></th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${dataForLandingPageList}" var="data">
                <tr>
                    <td>${data.source.encodeAsBMHTML()}</td>
                    <td>${data.visits.encodeAsBMHTML()}</td>
                    <td>${data.unique_visit.encodeAsBMHTML()}</td>
                    <td>${data.page_view.encodeAsBMHTML()}</td>
                    <td>${data.page_per_visit.toDouble().toFixed(2).encodeAsBMHTML()}</td>
                    <td>${data.avg_visit_duration.toDouble().toFixed(2).encodeAsBMHTML()}</td>
                    <td>${data.bounce_rate.toDouble().toFixed(2).encodeAsBMHTML()}</td>
                    <td>${data.new_visitor.encodeAsBMHTML()}</td>
                    <td>${(data.visits-data.new_visitor).encodeAsBMHTML()}</td>
                </tr>
            </g:each>
        </tbody>
    </table>
</div>