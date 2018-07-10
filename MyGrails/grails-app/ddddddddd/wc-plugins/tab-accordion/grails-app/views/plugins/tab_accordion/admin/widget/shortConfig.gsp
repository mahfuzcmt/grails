<g:applyLayout name="_widgetShortConfig">
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="type"/></div>
        <g:select class="sidebar-input" name="type" from="${[g.message(code: "tab"), g.message(code: "accordion")]}" keys="${['tab', 'accordion']}" toggle-target="transition" value="${config.type}"/>
    </div>
    <div class="sidebar-group transition-tab">
        <g:set var="horizontal" value="${config.axis == "h" || config.axis == null}"/>
        <input type="radio" name="axis" ${horizontal ? "checked='true'" : ""} value="h">
        <span><g:message code="horizontal"/></span><br>
        <input type="radio" name="axis" ${!horizontal ? "checked='true'" : ""} value="v">
        <span><g:message code="vertical"/></span>
    </div>
</g:applyLayout>