<%@ page import="com.webcommander.constants.DomainConstants" %>
<div class="header">
    <span class="item-group entity-count title">
        <g:message code="blog.post"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group action-header" style="display: none">
            <g:select class="action-on-selection" name="action" from="${[ g.message(code: "with.selected"), g.message(code: "status"), g.message(code: "visibility"), g.message(code: "remove")]}" keys="['', 'status', 'visibility', 'remove']"/>
        </div>
        <form class="search-form tool-group">
            <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
        </form>
        <div class="tool-group">
            <span class="toolbar-item add-filter" title="<g:message code="advance.search"/>"><i></i></span>
            <span class="toolbar-item remove-filter disabled" title="<g:message code="remove.search"/>"><i></i></span>
        </div>
        <div class="tool-group toolbar-btn create"><i></i><g:message code="create"/></div>
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
            <col class="status-column">
            <col class="title-column">
            <col class="catagory-column">
            <col class="date-column">
            <col class="comments-column">
            <col class="post-status-column">
            <col class="created-column">
            <col class="updated-column">
            <col class="created-by-column">
            <col class="actions-column">
        </colgroup>
        <tr>
            <th class="select-column"><input class="check-all multiple" type="checkbox"></th>
            <th class="status-column"><g:message code="visibility"/></th>
            <th><g:message code="title"/></th>
            <th><g:message code="category"/></th>
            <th><g:message code="date"/></th>
            <th><g:message code="no.of.comments"/></th>
            <th class="status-column"><g:message code="status"/></th>
            <th><g:message code="created"/></th>
            <th><g:message code="updated"/></th>
            <th><g:message code="created.by"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:set var="status" value="${[open: 'positive', hidden: 'negative', restricted: 'diplomatic']}"/>
        <g:set var="tooltipNotification" value="${[open: 'Open', hidden: 'Hidden', restricted: 'Restricted']}"/>
            <g:if test="${posts}">
                <g:each in="${posts}" var="post">
                    <tr>
                        <td class="select-column"><input entity-id="${post.id}" type="checkbox" class="multiple"></td>
                        <td class="status-column"><span class="status ${status[post.visibility]}" title="${tooltipNotification[post.visibility]}"></span></td>
                        <td>${post.name.encodeAsBMHTML()}</td>
                        <td>${post.categories ? post.categories.collect{return it.name}.join(", ") : g.message(code: "uncategorized")}</td>
                        <td>${post.date.toAdminFormat(false, false, session.timezone)}</td>
                        <td>${post.comments.size()}</td>
                        <td class="status-column">
                            <g:if test="${post.isPublished}">
                                <span class="status ${DomainConstants.STATUS.POSITIVE}" title="${g.message(code: 'published')}">
                                </span>
                            </g:if>
                            <g:else>
                                <span class="status ${DomainConstants.STATUS.NEGATIVE}" title="${g.message(code: 'unpublished')}">
                                </span>
                            </g:else>
                        </td>
                        <td>${post.created.toAdminFormat(false, false, session.timezone)}</td>
                        <td>${post.updated.toAdminFormat(false, false, session.timezone)}</td>
                        <td>${post.author?.fullName}</td>
                        <td class="actions-column">
                            <span class="action-navigator collapsed" entity-id="${post.id}" entity-name="${post.name.encodeAsBMHTML()}" entity-url="${post.url.encodeAsBMHTML()}"></span>
                        </td>
                    </tr>
                </g:each>
            </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="11"><g:message code="no.blog.post.created"/></td>
            </tr>
        </g:else>
    </table>
</div>
<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>