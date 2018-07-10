<%@ page import="com.webcommander.admin.Operator; com.webcommander.plugin.live_chat.constants.NamedConstants; com.webcommander.plugin.live_chat.ChatTag; com.webcommander.admin.Customer" %>
<div class="toolbar-share">
    <div class="toolbar toolbar-right">
        <div class="tool-group">
            <span><g:message code="chat.related"/></span>
            <ui:domainSelect name="tag" domain="${ChatTag}" class="tag-selector" prepend="${["": g.message(code: "none")]}"/>
            <span><g:message code="filter.report.by"/>:</span>
            <ui:namedSelect name="quickFilter" prepend="${[none: "none"]}" key="${NamedConstants.QUICK_FILTER}"/>
        </div>
        <div class="tool-group">
            <span class="toolbar-item add-filter-tool" title="<g:message code="advance.search"/>"><i></i></span>
            <span class="toolbar-item remove-filter-tool disabled" title="<g:message code="remove.search"/>"><i></i></span>
        </div>
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="app-tab-content-container">
    <table class="content">
        <colgroup>
            <col class="name-column">
            <col class="email-column">
            <col class="phone-column">
            <col class="start-time-column">
            <col class="end-time-column">
            <col class="supporter-name-column">
            <col class="chat-related-column">
            <col class="rating-column">
        </colgroup>
        <tr>
            <th><g:message code="visitor.name"/></th>
            <th><g:message code="email"/></th>
            <th><g:message code="phone"/></th>
            <th><g:message code="start.time"/></th>
            <th><g:message code="end.time"/></th>
            <th><g:message code="supporter.name"/></th>
            <th><g:message code="chat.related"/></th>
            <th><g:message code="rating"/></th>
        </tr>
        <g:if test="${chats}">
            <g:each in="${chats}" var="chat" status="i">
                <tr>
                    <td>${chat.name.encodeAsBMHTML()}</td>
                    <td>${chat.email}</td>
                    <td>${chat.phone}</td>
                    <td>${chat.created.toAdminFormat(true, false, session.timezone)}</td>
                    <td>${chat.updated.toAdminFormat(true, false, session.timezone)}</td>
                    <td>${com.webcommander.admin.Operator.get(chat.agentId)?.fullName.encodeAsBMHTML() ?: ""}</td>
                    <td>${chat.tags.name.join(", ")}</td>
                    <td><span class="chat-rating ${chat.rating}"></span></td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="8"><g:message code="no.chat.created"/></td>
            </tr>
        </g:else>
    </table>
</div>
<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>
