<g:applyLayout name="_widgetShortConfig">
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="filter.group"/></div>
        <g:select name="filterGroupId" class="sidebar-input" from="${filterGroups}" optionValue="name" optionKey="id" value="${config.filterGroupId}"/>
    </div>
</g:applyLayout>