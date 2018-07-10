<div class="header">
    <span class="item-group entity-count title">
        <g:message code="blog.comments"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group action-header" style="display: none">
            <g:select class="action-on-selection" name="action" from="${[ g.message(code: "with.selected"), g.message(code: "spam"), g.message(code: "status"), g.message(code: "remove")]}" keys="['', 'spam', 'status', 'remove']"/>
        </div>
        <form class="search-form tool-group">
            <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
        </form>
        <div class="tool-group">
            <span class="toolbar-item add-filter" title="<g:message code="advance.search"/>"><i></i></span>
            <span class="toolbar-item remove-filter disabled" title="<g:message code="remove.search"/>"><i></i></span>
        </div>
        <div class="tool-group">
            <span class="toolbar-item switch-menu collapsed"><i></i></span>
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="app-tab-content-container">
    <table class="content">
        <colgroup>
            <col class="select-column">
            <col class="writer-name-column">
            <col class="writer-email-column" >
            <col class="poprty-title-column">
            <col class="comment-column">
            <col class="created-column">
            <col class="status-column">
            <col class="status-column">
            <col class="actions-column">
        </colgroup>
        <tr>
            <th class="select-column"><input class="check-all multiple" type="checkbox"></th>
            <th><g:message code="writer.name"/></th>
            <th><g:message code="writer.email"/></th>
            <th><g:message code="post.title"/></th>
            <th><g:message code="comment"/></th>
            <th class="status-column"><g:message code="status"/></th>
            <th class="status-column"><g:message code="spam"/></th>
            <th><g:message code="created"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:if test="${comments}">
            <g:each in="${comments}" var="comment">
                <tr ${comment.isSpam ? "class='spam'" : ""}>
                    <td class="select-column"><input entity-id="${comment.id}" type="checkbox" class="multiple"></td>
                    <td>${comment.name.encodeAsBMHTML()}</td>
                    <td>${comment.email.encodeAsBMHTML()}</td>
                    <td>${comment.post.name.encodeAsBMHTML()}</td>
                    <td>${comment.content.encodeAsBMHTML().truncate(160)}</td>
                    <td class="status-column"><span class="status ${comment.status == 'approved' ? 'positive' : comment.status == 'rejected' ? 'negative' : 'diplomatic'}" title="${g.message(code: comment.status)}"></span></td>
                    <td class="status-column"><span class="status ${comment.isSpam ? 'positive' : 'negative'}" title="${g.message(code: comment.isSpam ? "spam" : 'not.spam')}"></span></td>
                    <td>${comment.created.toAdminFormat(true, false, session.timezone)}</td>
                    <td class="actions-column">
                        <span class="action-navigator collapsed ${comment.status}${comment.isSpam ? " spam" : ""}" entity-id="${comment.id}"></span>
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="9"><g:message code="no.blog.comment.found"/></td>
            </tr>
        </g:else>
    </table>
</div>
<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>