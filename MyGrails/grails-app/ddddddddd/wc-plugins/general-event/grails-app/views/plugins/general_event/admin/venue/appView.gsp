<div class="header">
    <span class="item-group entity-count title">
        <g:message code="venues"/> (<span class="count">${0}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group">
            <span class="toolbar-item switch-menu collapsed equipment-tool"><i></i></span>
        </div>
        <div class="tool-group toolbar-btn save event-venue"><g:message code="save"/></div>
    </div>
</div>
<div class="app-tab-content-container two-panel-resizable without-resize-bar">
    <div class="left-panel  venue-left-panel modern-list-left-panel">
        <div class="body venue-list scrollable-blocktype-list${!venues ? ' empty' : ''}">
            <g:include view="/plugins/general_event/admin/venue/leftPanel.gsp" model="[venues: venues, selected: selected]"/>
        </div>
        <div class="left-panel-btn"><button class="submit-button" type="button"><g:message code="add.venue"/></button></div>
        <div class="item-navigation venue-navigation">
            <div class="navigation-button location-button" item-type="location">
                <span class="icon"></span><span class="button-text"><g:message code="location"/></span>
            </div>
            <div class="navigation-button section-button" item-type="section">
                <span class="icon"></span><span class="button-text"><g:message code="section"/></span>
            </div>
            <div class="navigation-button sitmap-button" item-type="seatmap">
                <span class="icon"></span><span class="button-text"><g:message code="seat.map"/></span>
            </div>
        </div>
    </div>
    <div class="right-panel">
        <div class="body">
            <g:if test="${selected}">
                <g:include view="/plugins/general_event/admin/venue/rightPanel.gsp" model="[venue: venues.find {it.id == selected}]"/>
            </g:if>
        </div>
    </div>
</div>