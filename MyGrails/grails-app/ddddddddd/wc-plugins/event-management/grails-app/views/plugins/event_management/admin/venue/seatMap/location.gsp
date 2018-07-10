<%@ page import="com.webcommander.constants.DomainConstants" %>
<div class="table-view">
    <div class="header">
        <span class="item-group entity-count title">
            <g:message code="venue.location.seat.map"/>
        </span>
        <span class="toolbar selector">
            <label><g:message code="location"/></label>
            <g:select from="${locations}" optionKey="id" optionValue="name" name="location" class="medium location-selector"/>
        </span>
        <div class="toolbar toolbar-right">
            <div class="tool-group action-header" style="display: none">
                <g:select class="action-on-selection" name="action" from="${[ g.message(code: "with.selected"), g.message(code: "remove")]}" keys="['', 'remove']"/>
            </div>
            <div class="tool-group">
                <span class="toolbar-item view-switch location" view-type="location" title="<g:message code="venue.locations"/>"><i></i></span>
            </div>
            <div class="tool-group">
                <span class="toolbar-item switch-menu collapsed"><i></i></span>
                <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
            </div>
        </div>
    </div>
    <div class="body">
        <div class="location-wrap">
            <g:each in="${sections}" var="section">
                <g:include view="plugins/event_management/admin/venue/seatMap/section.gsp" model="[section: section]"/>
            </g:each>
        </div>
    </div>
</div>

<style type="text/css">

</style>