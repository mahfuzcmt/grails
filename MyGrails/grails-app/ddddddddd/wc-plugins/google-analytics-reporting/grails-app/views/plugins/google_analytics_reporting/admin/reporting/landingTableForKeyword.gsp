<table class="tabular-data-landing-keyword">
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