<div class="header">
    <div class="toolbar selector section-selector-wrapper" style="margin-left: 20px">
        <label><g:message code="select.section"/></label>
        <g:select from="${location.sections}" optionKey="id" optionValue="name" name="locationId" class="medium section-selector" value="${section.id}"/>
    </div>
    <div class="toolbar toolbar-right">
        <div class="tool-group toolbar-btn create create-ticket"><i></i><g:message code="create"/></div>
        <div class="tool-group">
            <span class="toolbar-item switch-menu collapsed"><i></i></span>
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="app-tab-content-container">
    <g:include controller="event" action="seatMap" params="[section: section]"/>
</div>