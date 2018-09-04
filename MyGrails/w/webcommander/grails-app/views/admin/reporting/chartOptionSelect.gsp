<g:form action="${app.relativeBaseUrl()}commaderReporting/save" method="post" class="edit-popup-form">
    <div class="multi-column three-column">
        <div class="columns first-column x-axis-options">
            <div class="title"><g:message code="x.axis"/></div>
            <div class="options-list">
                <g:each in="${chartXOptionList}" var="option">
                    <span class="radio-label-pack">
                        <input type="radio" name="xaxis" value="${option}" ${option == xAxis ? 'checked' : ''}>
                        <label><g:message code="${option}"/></label>
                    </span>
                </g:each>
            </div>
        </div><div class="columns">
            <div class="title"><g:message code="y.axis"/></div>
            <div class="options-list">
                <g:each in="${chartYOptionList}" var="option">
                    <span class="radio-label-pack">
                        <input type="radio" name="yaxis" value="${option}" ${option == yAxis ? 'checked' : ''}>
                        <label><g:message code="${option}"/></label>
                    </span>
                </g:each>
            </div>
        </div><div class="columns last-column">
            <div class="title"><g:message code="chart.type"/></div>
            <div class="options-list">
                <g:each in="${['line', 'bar', 'radar', 'pie', 'doughnut']}" var="type">
                    <span class="radio-label-pack">
                        <input type="radio" name="chartType" value="${type}" ${type == chartType ? 'checked' : ''}>
                        <label><g:message code="${type}"/></label>
                    </span>
                </g:each>
            </div>
        </div>
    </div>
</g:form>