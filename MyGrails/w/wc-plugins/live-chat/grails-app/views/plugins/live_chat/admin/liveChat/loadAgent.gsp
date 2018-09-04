<%@ page import="com.webcommander.plugin.live_chat.ChatDepartment; com.webcommander.admin.Operator" %>
<div class="toolbar-share">
    <div class="toolbar toolbar-right">
         <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="app-tab-content-container">
    <table class="content">
        <colgroup>
            <col>
            <col>
            <col>
            <col>
        </colgroup>
        <tr>
            <th><g:message code="name"/></th>
            <th><g:message code="login.time"/></th>
            <th><g:message code="department(s)"/></th>
            <th><g:message code="chat"/></th>
        </tr>
        <g:each in="${agents}" var="agent" status="i">
            <tr entity-id="${agent.id}">
                <td>${Operator.get(agent.id).fullName.encodeAsBMHTML()}</td>
                <td>${agent.loginTime.toZone(session.timezone)}</td>
                <td>
                    <g:set var="departments" value="${ChatDepartment.createCriteria().list{operators{ eq("id", agent.id)}}}"></g:set>
                    ${departments.name.join(", ")}
                 </td>
                <td>
                    <g:if test="${agent.id != myOperatorId}">
                        <button type="button" class="submit-button chat-with-operator" data-agentId="${agent.id}"><g:message code="chat"/></button>
                    </g:if>
                </td>
            </tr>
        </g:each>
    </table>
</div>
<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>
