<%@ page import="com.webcommander.plugin.blog.constants.DomainConstants" %>
<table class="inner-table">
    <g:if test="${comment.replies}">
        <tr>
            <td class="table-header"><g:message code="writer.name"/></td>
            <td class="table-header"><g:message code="writer.email"/></td>
            <td class="table-header"><g:message code="post.title"/></td>
            <td class="table-header"><g:message code="comment"/></td>
            <td class="table-header"><g:message code="like"/></td>
            <td class="table-header"><g:message code="status"/></td>
            <td class="table-header"><g:message code="spam"/></td>
            <td><g:message code="created"/></td>
            <td class="table-header actions-column"><g:message code="actions"/></td>
        </tr>
        <g:each in="${comment.replies}" var="reply">
            <tr ${reply.isSpam ? "class='spam'" : ""}>
                <td>${reply.name.encodeAsBMHTML()}</td>
                <td>${reply.email.encodeAsBMHTML()}</td>
                <td>${reply.post.name.encodeAsBMHTML()}</td>
                <td>${reply.content.encodeAsBMHTML().truncate(160)}</td>
                <td>${reply.reactions.findAll {it.type == DomainConstants.BLOG_REACTION.LIKE}.size()}</td>
                <td class="status-column"><span class="status ${reply.status == 'approved' ? 'positive' : reply.status == 'rejected' ? 'negative' : 'diplomatic'}" title="${g.message(code: reply.status)}"></span></td>
                <td class="status-column"><span class="status ${reply.isSpam ? 'positive' : 'negative'}" title="${g.message(code: reply.isSpam ? "spam" : 'not.spam')}"></span></td>
                <td>${reply.created.toAdminFormat(true, false, session.timezone)}</td>
                <td class="actions-column">
                    <span class="action-navigator collapsed ${reply.status}${reply.isSpam ? " spam" : ""}" entity-id="${reply.id}"></span>
                </td>
            </tr>
        </g:each>
    </g:if>
    <g:else>
        <tr class="table-no-entry-row">
            <td colspan="11"><g:message code="no.blog.comment.reply.found"/></td>
        </tr>
    </g:else>
 </table>
