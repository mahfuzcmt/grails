<div class="analytics-report">
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
        <g:message code="keywords"/>, <g:message code="from.organic.traffic"/>
    </div>
    <div class="toolbar-button-group tool-group">
        <span class="btn analytics-type-keyword" value="all"><g:message code="all"/></span>
        <span class="btn analytics-type-keyword" value="google"><g:message code="google"/></span>
        <span class="btn analytics-type-keyword" value="yahoo"><g:message code="yahoo"/></span>
        <span class="btn analytics-type-keyword" value="bing"><g:message code="bing"/></span>
        <span class="btn analytics-type-keyword" value="ask"><g:message code="ask"/></span>
        <span class="btn analytics-type-keyword" value="so"><g:message code="so"/></span>
    </div>
    <table class="tabular-data">
        <thead>
        <tr>
            <th><g:message code="keywords"/></th>
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
        <g:each in="${dataForKeywordList}" var="data">
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
        <g:message code="landing.pages"/>, <g:message code="from.organic.traffic"/>
    </div>
    <div class="toolbar-button-group tool-group">
        <span class="btn analytics-type-landing" value="all"><g:message code="all"/></span>
        <span class="btn analytics-type-landing" value="google"><g:message code="google"/></span>
        <span class="btn analytics-type-landing" value="yahoo"><g:message code="yahoo"/></span>
        <span class="btn analytics-type-landing" value="bing"><g:message code="bing"/></span>
        <span class="btn analytics-type-landing" value="ask"><g:message code="ask"/></span>
        <span class="btn analytics-type-landing" value="so"><g:message code="so"/></span>
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