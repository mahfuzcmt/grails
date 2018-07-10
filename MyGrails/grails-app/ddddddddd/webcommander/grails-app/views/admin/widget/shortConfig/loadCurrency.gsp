<g:applyLayout name="_widgetShortConfig">
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="label"/></div>
        <input type="text" class="sidebar-input" name="label" value="${config.label ?: 's:Currency'}">
    </div>
    <div class="sidebar-group">
        <g:set var="displayOptions" value="${["name", "code"]}"/>
        <div class="sidebar-group-label"><g:message code="display.option"/>:</div>
        <g:select class="sidebar-input" name="displayOption" from="${displayOptions.collect {g.message(code: it)}}" keys="${displayOptions}" value="${config.displayOption}"/>
    </div>
</g:applyLayout>