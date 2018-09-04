<%--
  Created by IntelliJ IDEA.
  User: sajedur
  Date: 23-02-2015
  Time: 16:04
--%>

<div class="toolbar-share">
    <div class="toolbar toolbar-right ">
        <div class="tool-group toolbar-btn create"><i></i><g:message code="create"/></div>
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="app-tab-content-container">
    <table class="content">
        <colgroup>
            <col class="status-column">
            <col class="name-column">
            <col class="description-column">
            <col class="client-id-column">
            <col class="actions-column">
        </colgroup>
        <tr>
            <th class="status-column"><g:message code="status"/></th>
            <th><g:message code="name"/></th>
            <th><g:message code="description"/></th>
            <th><g:message code="client.id"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:each in="${clients}" var="client" status="i">
            <tr>
                <td class="status-column"><span class="status ${clients.enabled ? "positive": "negative"}"></span></td>
                <td>${client.displayName.encodeAsBMHTML()}</td>
                <td>${client.description.encodeAsBMHTML()}</td>
                <td>${client.clientId}</td>
                <td class="actions-column"><span class="action-navigator collapsed" entity-id="${client.id}" entity-name="${client.name.encodeAsBMHTML()}"></span></td>
            </tr>
        </g:each>
    </table>
</div>
<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>
