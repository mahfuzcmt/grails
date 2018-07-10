<g:applyLayout name="_widgetShortConfig">
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="form"/></div>
        <g:select name="form" class="sidebar-input" from="${forms}" optionKey="id" optionValue="name" value="${form}"/>
    </div>
</g:applyLayout>