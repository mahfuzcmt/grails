<%@ page import="com.webcommander.constants.DomainConstants" %>
<div class="header">
    <span class="item-group entity-count title">
        <g:message code="venue.location.sections"/> (<span class="count">${count}</span>)
    </span>
    &nbsp;&nbsp;
    <div class="toolbar toolbar-right">
        <span class="toolbar-selector tool-group">
            <label><g:message code="location"/></label>
            <g:select from="${locations.name}" keys="${locations.id}" name="location" class="medium location-selector"/>
        </span>
        <div class="tool-group action-header" style="display: none">
            <g:select class="action-on-selection" name="action" from="${[ g.message(code: "with.selected"), g.message(code: "remove")]}" keys="['', 'remove']"/>
        </div>
        <form class="search-form tool-group">
            <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
        </form>
        <div class="tool-group toolbar-btn create"><i></i><g:message code="create"/></div>
        <div class="tool-group">
            <span class="toolbar-item switch-menu collapsed"><i></i></span>
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="app-tab-content-container body">
    <table class="content">
        <colgroup>
            <col style="width: 5%">
            <col style="width: 30%">
            <col style="width: 30">
            <col style="width: 25%">
            <col style="width: 10%">
        </colgroup>
        <tr>
            <th class="select-column"><input type="checkbox" class="check-all multiple"></th>
            <th><g:message code="name"/></th>
            <th><g:message code="number.of.seats"/></th>
            <th><g:message code="ticket.price"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:if test="${sections}">
            <g:each in="${sections}" var="section">
                <tr>
                    <td class="select-column"><input entity-id="${section.id}" type="checkbox" class="multiple"></td>
                    <td>${section?.name?.encodeAsBMHTML()}</td>
                    <td>${section.rowCount * section.columnCount}</td>
                    <td>${section?.ticketPrice.toPrice()}</td>
                    <td class="actions-column">
                        <span class="action-navigator collapsed" entity-id="${section.id}" entity-name ="${section.name}" entity-price="${section.ticketPrice}"></span>
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="5"><g:message code="no.venue.location.found"/> </td>
            </tr>
        </g:else>
    </table>
</div>
<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>