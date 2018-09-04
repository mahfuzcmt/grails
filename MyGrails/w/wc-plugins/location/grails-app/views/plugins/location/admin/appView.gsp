<%@ page import="com.webcommander.constants.DomainConstants" %>
<div class="header">
    <span class="item-group entity-count title">
        <g:message code="location"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <form class="search-form tool-group">
            <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
        </form>
        <div class="tool-group">
            <span class="toolbar-item add-filter" title="<g:message code="advance.search"/>"><i></i></span>
            <span class="toolbar-item remove-filter disabled" title="<g:message code="remove.search"/>"><i></i></span>
        </div>
        <div class="tool-group toolbar-btn create"><i></i><g:message code="create"/></div>
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>

<div class="app-tab-content-container">
    <table class="content">
        <colgroup>
            <col class="location-name-column">
            <col class="address-column">
            <col class="contact-email-column">
            <col class="contact-no-column">
            <col class="actions-column">
        </colgroup>
        <tr>
            <th><g:message code="location.name"/></th>
            <th><g:message code="address"/></th>
            <th><g:message code="contact.email"/></th>
            <th><g:message code="contact.no"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:if test="${locations}">
            <g:each in="${locations}" var="location">
                <tr>
                    <td>${(location.locationHeadingName ?: location.name).encodeAsBMHTML()}</td>
                    <td>${location.locationAddress}</td>
                    <td>${location.contactEmail}</td>
                    <td>${location.phoneNumber}</td>
                    <td class="actions-column">
                        <span class="action-navigator collapsed" entity-id="${location.id}" entity-name="${location.name.encodeAsBMHTML()}"></span>
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="11"><g:message code="no.location.created"/></td>
            </tr>
        </g:else>
    </table>
</div>
<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>