<div class="header">
    <g:if test="${location}">
        <div class="toolbar selector location-selector-wrapper">
            <label><g:message code="select.venue.location"/></label>
            <g:select from="${locations}" optionKey="id" optionValue="name" name="locationId" class="medium location-selector" value="${location.id}"/>
        </div>
    </g:if>
    <div class="toolbar selector section-selector-wrapper" style="margin-left: 20px">
        <g:if test="${section}">
            <label><g:message code="select.section"/></label>
            <g:select from="${location.sections}" optionKey="id" optionValue="name" name="locationId" class="medium section-selector" value="${section.id}"/>
        </g:if>
    </div>

    <div class="toolbar toolbar-right">
        <div class="tool-group">
            <span class="toolbar-item switch-menu collapsed"><i></i></span>
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<g:if test="${location}">
    <div class="app-tab-content-container">
        <g:if test="${section}">
            <g:include controller="event" action="seatMap" params="[section: section]"/>
        </g:if>
        <g:else>
            <g:message code="no.sections.created.yet"/>
        </g:else>
    </div>
</g:if>
<g:else>
    <g:message code="no.locations.created.yet"/>
</g:else>