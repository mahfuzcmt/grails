<%@ page import="com.webcommander.plugin.live_chat.ChatTag; com.webcommander.admin.Customer" %>
<div class="toolbar-share">
    <span class="item-group entity-count title toolbar-left">
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group">
            <span><g:message code="chat.related"/></span>
            <ui:domainSelect name="tag" domain="${ChatTag}" class="tag-selector" prepend="${["": g.message(code: "none")]}"/>
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
        </colgroup>
        <tr>
            <th><g:message code="visitor.name"/></th>
            <th><g:message code="chat.related"/></th>
            <th><g:message code="rating"/></th>
            <th><g:message code="date"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:if test="${chats}">
            <g:each in="${chats}" var="chat" status="i">
                <tr>
                    <td>${chat.name.encodeAsBMHTML()}</td>
                    <td>${chat.tags.name.join(", ")}</td>
                    <td><span class="chat-rating ${chat.rating}"></span></td>
                    <td>${chat.created.toAdminFormat(true, false, session.timezone)}</td>
                    <td class="actions-column">
                        <span class="action-navigator collapsed" entity-id="${chat.id}"></span>
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="5"><g:message code="no.chat.created"/></td>
            </tr>
        </g:else>
    </table>
</div>
<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>
