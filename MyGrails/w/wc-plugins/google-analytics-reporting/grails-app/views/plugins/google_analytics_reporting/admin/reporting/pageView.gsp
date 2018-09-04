<div class="analytics-report">
    <div class="report-chart-header">
        <g:message code="page.view"/>s
    </div>
    <table class="tabular-data">
        <tr>
            <td><g:message code="n.page.view" args="${[pageView.page_view]}"/></td>
            <td><g:message code="n.unique.page.view" args="${[pageView.unique_page_view]}"/></td>
        </tr>
        <tr>
            <td><g:message code="n.avg.page.view.duration" args="${[pageView.avg_page_view_duration.toDouble().toFixed(2)]}"/></td>
            <td><g:message code="n.bounce.rate" args="${[pageView.bounce_rate.toDouble().toFixed(2)]}"/></td>
        </tr>
    </table>
    <google:page_views_chart type="${params.chartType ?: 'line'}"/>
    <div class="report-chart-header">
        <g:message code="Page.for.all.search"/>
    </div>
    <table class="tabular-data">
        <thead>
            <tr>
                <th><g:message code="page"/></th>
                <th><g:message code="page.views"/></th>
                <th><g:message code="unique.page.view"/></th>
                <th><g:message code="avg.page.view.duration"/></th>
                <th><g:message code="bounce.rate"/></th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${dataForPageList}" var="data">
                <tr>
                    <td>${data.page.encodeAsBMHTML()}</td>
                    <td>${data.page_view.encodeAsBMHTML()}</td>
                    <td>${data.unique_page_view.encodeAsBMHTML()}</td>
                    <td>${data.avg_page_view_duration.toDouble().toFixed(2).encodeAsBMHTML()}</td>
                    <td>${data.bounce_rate.toDouble().toFixed(2).encodeAsBMHTML()}</td>
                </tr>
            </g:each>
        </tbody>
    </table>
</div>