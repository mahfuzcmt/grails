<%@ page import="com.webcommander.converter.json.JSON" %>
<div class="custom-proeject-overview">

    <div class="header">
        <span class="heading-title"><g:message code="milestone"/></span>
        <span class="date to"><label><g:message code="today"/>: </label>${new Date().toAdminFormat(false, false, session.timezone)}</span>
    </div>
    <div class="body">
        <table>
            <colgroup>
                <col class="name-column"/>
                <col class="start-date-column"/>
                <col class="end-date-column"/>
                <col class="status-column"/>
                <col class="action-column"/>
            </colgroup>
            <tr>
                <th><g:message code="milestone" /></th>
                <th><g:message code="start.date" /></th>
                <th><g:message code="end.date" /></th>
                <th><g:message code="status" /></th>
                <th><g:message code="action" /></th>
            </tr>
            <g:each in="${milestones}" var="milestone">
                <tr>
                    <td>${milestone.name}</td>
                    <td>${JSON.dateFormatter.parse(milestone.startDate).toAdminFormat(false, false, session.timezone)}</td>
                    <td>${JSON.dateFormatter.parse(milestone.endDate).toAdminFormat(false, false, session.timezone)}</td>
                    <td>${milestone.status}</td>
                    <td class="action-column">
                        <span class="tool-icon ${milestone.isApprovedByCustomer ? "approved" : "approve"}" entity-id="${milestone.id}"></span>
                    </td>
                </tr>
            </g:each>
        </table>
    </div>
</div>